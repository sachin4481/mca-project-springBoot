package com.example.LoginDemo.Controller;


import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.PropertyRepository;
import com.example.LoginDemo.Services.PropertyServices;
import com.example.LoginDemo.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PropertyController {

    @Autowired
    private PropertyServices propertyServices;

    @Autowired
    private UserServices userServices;

    @Autowired
    private PropertyRepository propertyRepository;


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



}
