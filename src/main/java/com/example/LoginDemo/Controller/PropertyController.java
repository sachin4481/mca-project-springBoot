package com.example.LoginDemo.Controller;


import com.example.LoginDemo.Entity.*;
import com.example.LoginDemo.Repository.*;
import com.example.LoginDemo.Services.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PropertyController {

    @Autowired
    private PropertyServices propertyServices;


    @Autowired
    private PropertyInfoService propertyInfoService;  // Add this

    @Autowired
    private PropertyDetailsService propertyDetailsService;  // Add this

    @Autowired
    private UserServices userServices;

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private PropertyRepository propertyRepository;

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


    @GetMapping("/properties")
    public String showProperties(Model model) {
        List<PropertyCat> categories = propertyCatRepository.findAll();
        model.addAttribute("categories", categories);

        List<String> locations = propertyInfoRepository.findDistinctLocations();
        model.addAttribute("locations", locations);

        // Fetch the 4 most recent listings
        Pageable pageable = PageRequest.of(0, 4);
        List<PropertyInfo> recentListings = propertyInfoRepository.findTop4RecentProperties(pageable);

        // Debugging: Print properties
        System.out.println("Fetched Recent Listings: " + recentListings);

        model.addAttribute("recentListings", recentListings);
        return "properties";
    }

@PostMapping("/search")
public String searchProperties(@RequestParam(required = false) Long category,//new added code
                               @RequestParam(required = false) String area,
                               @RequestParam(required = false) Double price,
                               @RequestParam(required = false) String location,
                               RedirectAttributes redirectAttributes) {
    // Retrieve all properties
//    List<PropertyEntity> properties = propertyRepository.findAll();
    List<PropertyInfo> properties = propertyInfoRepository.findAll();//new added code
    // Filter by Property Category
    if (category != null) {//new added code
        properties = properties.stream()//new added code
                .filter(p -> p.getPropertyCategory().getId().equals(category))//new added code
                .collect(Collectors.toList());//new added code
    }//new added code

    //  Filter by Area (Fix Applied)
    if (area != null && !area.trim().isEmpty()) {
        try {
            int areaValue = Integer.parseInt(area);
            properties = properties.stream()
//                    .filter(p -> p.getArea() != null && p.getArea() <= areaValue)
                    .filter(p -> p.getArea() != null && Integer.parseInt(p.getArea()) <= areaValue)//new added code
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
    redirectAttributes.addFlashAttribute("selectedCategory", category);

    return "redirect:/properties";
}

    @GetMapping("/properties/{id}")
    public String getPropertyDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Fetch propertyInfo using repository
        PropertyInfo propertyInfo = propertyInfoRepository.findById(id).orElse(null);

        // Check if propertyInfo exists before proceeding
        if (propertyInfo == null) {
            model.addAttribute("errorMessage", "Property not found.");
            return "error-page"; // Your error page template
        }

        // Fetch property details based on propertyInfo's ID
        PropertyDetails propertyDetails = propertyDetailsRepository.findByPropertyInfo_PropId(id).orElse(null);

        model.addAttribute("propertyInfo", propertyInfo);
        model.addAttribute("propertyDetails", propertyDetails);

        if (propertyDetails == null) {
            model.addAttribute("noDetailsMessage", "Owner did not provide other details.");
        }

        return "property-details"; // Your HTML file name
    }

    // Edit Property Form
    @GetMapping("/properties/edit/{id}")
    public String showEditPropertyForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {

        PropertyInfo propertyInfo = propertyInfoService.getPropertyById(id);
        PropertyDetails propertyDetails = propertyDetailsService.getDetailsByPropertyId(id);
        if (propertyDetails == null) {
            propertyDetails = new PropertyDetails();
        }
        String username = userDetails.getUsername();
        UserEntity currentUser = userServices.findByUsername(username);

        if (!propertyInfo.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/properties"; // Redirect if unauthorized
        }

        model.addAttribute("propertyInfo", propertyInfo);
        model.addAttribute("propertyDetails", propertyDetails);

        return "edit-property";
    }

    // Update Property
    @PostMapping("/properties/edit/{id}")
    public String updateProperty(@PathVariable Long id,
                                 @ModelAttribute PropertyInfo propertyInfo,
                                 @RequestParam("images") MultipartFile[] images,
                                 @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        String username = userDetails.getUsername();
        UserEntity currentUser = userServices.findByUsername(username); // Fetch from service

        propertyServices.updateProperty(id, propertyInfo, images, currentUser);
        return "redirect:/edit-property-details/{id}";
    }

//    @GetMapping("/edit-property-details/{id}")
//    public String editPropertyDetails(@PathVariable Long id, Model model) {
//    // Check if property details exist for the given property ID
//    Optional<PropertyDetails> propertyDetails = propertyDetailsRepository.findByPropertyInfo_PropId(id);
//
//    if (propertyDetails.isEmpty()) {
//        // If no property details exist, redirect to /properties/{id}
//        return "redirect:/user/add-property-details?propId=" + id; // Redirect if details do not exist
//    }
//
//    model.addAttribute("propertyId", id);
//    return "edit-property-details";
//}

    @GetMapping("/edit-property-details/{id}")
    public String editPropertyDetails(@PathVariable Long id, Model model) {
        Optional<PropertyDetails> propertyDetails = propertyDetailsRepository.findByPropertyInfo_PropId(id);

        if (propertyDetails.isEmpty()) {
            return "redirect:/user/add-property-details?propId=" + id;
        }

        PropertyDetails details = propertyDetails.get();
        String categoryName = details.getPropertyInfo().getPropertyCategory().getName(); // Assuming there's a getCatName()

        model.addAttribute("propertyDetails", details);
        model.addAttribute("propertyId", id);
        model.addAttribute("category", categoryName);
        return "edit-property-details";
    }


    @PostMapping("/edit-property-details")
    public String updatePropertyDetails(@ModelAttribute PropertyDetails propertyDetails, @RequestParam("propId") Long propId) {
        // Fetch the associated PropertyInfo
        Optional<PropertyInfo> propertyInfoOpt = propertyInfoRepository.findById(propId);
        if (propertyInfoOpt.isEmpty()) {
            return "redirect:/error"; // or handle gracefully
        }

        propertyDetails.setPropertyInfo(propertyInfoOpt.get());
        propertyDetailsRepository.save(propertyDetails);

        return "redirect:/properties/" + propId;
    }




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

    @Transactional
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
            System.out.println("Property ID not found: " + propId);
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

        return "redirect:/properties/" + propId;

    }

}
