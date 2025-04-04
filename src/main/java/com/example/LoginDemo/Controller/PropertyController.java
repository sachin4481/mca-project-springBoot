package com.example.LoginDemo.Controller;


import com.example.LoginDemo.Entity.*;
import com.example.LoginDemo.Repository.*;
import com.example.LoginDemo.Services.PropertyServices;
import com.example.LoginDemo.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PropertyController {

    @Autowired
    private PropertyServices propertyServices;


    @Autowired
    private UserServices userServices;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyCatRepository propertyCatRepository;

    @Autowired
    private PropertyInfoRepository propertyInfoRepository;

    @Autowired
    private PropertyDetailsRepository propertyDetailsRepository;

    @GetMapping("/user/properties")
    public String showUserProperties(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        UserEntity currentUser = userServices.findByUsername(username);
        List<PropertyEntity> properties = propertyServices.getPropertiesByUser(currentUser);
        model.addAttribute("properties", properties);
        model.addAttribute("currentUser", currentUser);
        return "user-properties";
    }

//
//    @GetMapping("/properties")
//    public String showProperties(Model model) {
//        List<String> locations = propertyRepository.findDistinctLocations();
//        model.addAttribute("locations", locations);
//        model.addAttribute("selectedLocation", ""); // Initialize with an empty string or default value
//        return "properties"; // Your HTML page name
//    }

@GetMapping("/properties")
public String showProperties(Model model) {

    List<String> locations = propertyRepository.findDistinctLocations();
    model.addAttribute("locations", locations);

    // Fetch the 4 most recent listings
    Pageable pageable = PageRequest.of(0, 4);
    List<PropertyEntity> recentListings = propertyServices.getRecentListings(pageable);

    // Add to model
    model.addAttribute("recentListings", recentListings);


    return "properties"; // Your HTML page name
}




//    @PostMapping("/search")
//    public String searchProperties(@RequestParam(required = false) String area,
//                                   @RequestParam(required = false) Double price,
//                                   @RequestParam(required = false) String location,
//                                   Model model) {
//
//        List<PropertyEntity> properties = propertyRepository.findAll();
//
//        if (location != null && !location.trim().isEmpty()) {
//            properties = properties.stream()
//                    .filter(p -> p.getLocation().toLowerCase().contains(location.toLowerCase()))
//                    .collect(Collectors.toList());
//        }
//
//        if (price != null) {
//            properties = properties.stream()
//                    .filter(p -> p.getPrice() <= price)
//                    .collect(Collectors.toList());
//        }
//
//        model.addAttribute("properties", properties);
//        model.addAttribute("locations", propertyRepository.findDistinctLocations());
//        model.addAttribute("searchArea", area);
//        model.addAttribute("searchPrice", price);
//        model.addAttribute("selectedLocation", location);
//
//        return "properties";
//    }
@PostMapping("/search")
public String searchProperties(@RequestParam(required = false) String area,
                               @RequestParam(required = false) Double price,
                               @RequestParam(required = false) String location,
                               RedirectAttributes redirectAttributes) {
    // Retrieve all properties
    List<PropertyEntity> properties = propertyRepository.findAll();

    //  Filter by Area (Fix Applied)
    if (area != null && !area.trim().isEmpty()) {
        try {
            int areaValue = Integer.parseInt(area);
            properties = properties.stream()
                    .filter(p -> p.getArea() != null && p.getArea() <= areaValue)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid area value");
            return "redirect:/properties";
        }
    }

    // Filter by Price
    if (price != null) {
        properties = properties.stream()
                .filter(p -> p.getPrice() <= price)
                .collect(Collectors.toList());
    }

    // Filter by Location
    if (location != null && !location.trim().isEmpty()) {
        properties = properties.stream()
                .filter(p -> p.getLocation().toLowerCase().contains(location.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Add attributes for redirection
    redirectAttributes.addFlashAttribute("properties", properties);
    redirectAttributes.addFlashAttribute("searchArea", area);
    redirectAttributes.addFlashAttribute("searchPrice", price);
    redirectAttributes.addFlashAttribute("selectedLocation", location);

    return "redirect:/properties";
}


    //  Show Add Property Form
    @GetMapping("/user/add")
    public String showAddPropertyForm(Model model) {
        model.addAttribute("categories", propertyCatRepository.findAll());
        return "add-property"; // Return the form page
    }


    @PostMapping("/user/add")
    public String addProperty(@RequestParam("propTitle") String propTitle,
                              @RequestParam("propCatId") Long propCatId,
                              @RequestParam("location") String location,
                              @RequestParam("pinCode") String pinCode,
                              @RequestParam("area") String area,
                              @RequestParam("facing") String facing,
                              @RequestParam("price") Double price,
                              @RequestParam("img1") MultipartFile img1,
                              @RequestParam(value = "img2", required = false) MultipartFile img2,
                              @RequestParam(value = "img3", required = false) MultipartFile img3,
                              @RequestParam(value = "img4", required = false) MultipartFile img4,
                              @RequestParam(value = "img5", required = false) MultipartFile img5,
                              Model model,
                              RedirectAttributes redirectAttributes) throws IOException {

        // Get logged-in user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            return "redirect:/login";
        }
        UserEntity user = userOptional.get();

        // Get property category
        Optional<PropertyCat> categoryOptional = propertyCatRepository.findById(propCatId);
        if (!categoryOptional.isPresent()) {
            model.addAttribute("error", "Invalid category selected.");
            return "addProperty";
        }
        PropertyCat category = categoryOptional.get();

        // Save images
        String img1Path = saveFile(img1);
        String img2Path = (img2 != null && !img2.isEmpty()) ? saveFile(img2) : null;
        String img3Path = (img3 != null && !img3.isEmpty()) ? saveFile(img3) : null;
        String img4Path = (img4 != null && !img4.isEmpty()) ? saveFile(img4) : null;
        String img5Path = (img5 != null && !img5.isEmpty()) ? saveFile(img5) : null;

        // Create PropertyInfo object
        PropertyInfo property = new PropertyInfo();
        property.setUser(user);
        property.setPropertyCategory(category);
        property.setPropTitle(propTitle);
        property.setLocation(location);
        property.setPinCode(pinCode);
        property.setArea(area);
        property.setFacing(facing);
        property.setPrice(price);
        property.setImg1(img1Path);
        property.setImg2(img2Path);
        property.setImg3(img3Path);
        property.setImg4(img4Path);
        property.setImg5(img5Path);
        property.setListingDate(new Date());
        property.setStatus("unsold");

        // Save property
        propertyInfoRepository.save(property);

        // Pass data to the next page
        redirectAttributes.addFlashAttribute("property", property);

//        return "redirect:/user/add-property-details";
        return "redirect:/user/add-property-details?propId=" + property.getPropId();

    }
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";
    private String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + fileName);
        Files.write(path, file.getBytes());
        return "/uploads/" + fileName;
    }

    @GetMapping("/user/add-property-details")
    public String showPropertyDetailsForm(@RequestParam("propId") Long propId, Model model) {
        Optional<PropertyInfo> propertyOptional = propertyInfoRepository.findById(propId);
        if (!propertyOptional.isPresent()) {
            return "redirect:/properties";
        }

        PropertyInfo property = propertyOptional.get();
        model.addAttribute("property", property);
        return "add-property-details";
    }


    @PostMapping("/user/add-property-details")
    public String addPropertyDetails(@RequestParam("propId") Long propId,
                                     @RequestParam(required = false) Integer bhk,
                                     @RequestParam(required = false) Integer floors,
                                     @RequestParam(required = false) Boolean corner,
                                     @RequestParam(required = false) Integer floor,
                                     @RequestParam(required = false) Boolean furnished,
                                     @RequestParam(required = false) Integer washroom,
                                     @RequestParam(required = false) Boolean parking,
                                     @RequestParam(required = false) String schemeName,
                                     @RequestParam(required = false) String propDetails,
                                     RedirectAttributes redirectAttributes) {

        System.out.println("Received propId: " + propId);
        System.out.println("Received BHK: " + bhk);
        System.out.println("Received Floors: " + floors);
        System.out.println("Received Corner: " + corner);
        System.out.println("Received Floor: " + floor);
        System.out.println("Received Furnished: " + furnished);
        System.out.println("Received Washroom: " + washroom);
        System.out.println("Received Parking: " + parking);
        System.out.println("Received Scheme Name: " + schemeName);
        System.out.println("Received Property Details: " + propDetails);

        Optional<PropertyInfo> propertyOptional = propertyInfoRepository.findById(propId);
        if (!propertyOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Invalid property ID.");
            return "redirect:/properties";
        }

        PropertyInfo property = propertyOptional.get();
        PropertyDetails details = new PropertyDetails();

        details.setPropertyInfo(property);
        details.setBhk(bhk);
        details.setFloors(floors);
        details.setCorner(corner);
        details.setFloor(floor);
        details.setFurnished(furnished);
        details.setWashroom(washroom);
        details.setParking(parking);
        details.setSchemeName(schemeName);
        details.setPropDetails(propDetails);

        propertyDetailsRepository.save(details);

        System.out.println("Property details saved successfully!");

        return "redirect:/properties";
    }




}
