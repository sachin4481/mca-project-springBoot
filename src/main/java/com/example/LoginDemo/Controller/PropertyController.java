package com.example.LoginDemo.Controller;


import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.PropertyRepository;
import com.example.LoginDemo.Services.PropertyServices;
import com.example.LoginDemo.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PropertyController {

    @Autowired
    private PropertyServices propertyServices;

    @Autowired
    private UserServices userServices;

    @GetMapping("/user/properties")
    public String showUserProperties(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        UserEntity currentUser = userServices.findByUsername(username);
        List<PropertyEntity> properties = propertyServices.getPropertiesByUser(currentUser);
        model.addAttribute("properties", properties);
        model.addAttribute("currentUser", currentUser);
        return "user-properties";
    }

    // Properties listing with search
    // Search results page
//    @GetMapping("/search")
//    public String searchProperties(@RequestParam(value = "location", required = false) String location, Model model) {
//        List<PropertyEntity> properties = propertyServices.searchPropertiesByArea(location);
//        model.addAttribute("properties", properties);
//        model.addAttribute("searchArea", location); // Pre-fill search input
//        return "searchproperty";
//    }
    @GetMapping("/properties")
    public String showProperties(Model model) {
        List<String> locations = propertyRepository.findDistinctLocations();
        model.addAttribute("locations", locations);
        model.addAttribute("selectedLocation", ""); // Initialize with an empty string or default value
        return "properties"; // Your HTML page name
    }
    @Autowired
    private PropertyRepository propertyRepository;




    @PostMapping("/search")
    public String searchProperties(@RequestParam(required = false) String area,
                                   @RequestParam(required = false) Double price,
                                   @RequestParam(required = false) String location,
                                   Model model) {

        List<PropertyEntity> properties = propertyRepository.findAll();

        if (location != null && !location.trim().isEmpty()) {
            properties = properties.stream()
                    .filter(p -> p.getLocation().toLowerCase().contains(location.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (price != null) {
            properties = properties.stream()
                    .filter(p -> p.getPrice() <= price)
                    .collect(Collectors.toList());
        }

        model.addAttribute("properties", properties);
        model.addAttribute("locations", propertyRepository.findDistinctLocations());
        model.addAttribute("searchArea", area);
        model.addAttribute("searchPrice", price);
        model.addAttribute("selectedLocation", location);

        return "properties";
    }


}
