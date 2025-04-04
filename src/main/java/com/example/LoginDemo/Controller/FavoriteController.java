//package com.example.LoginDemo.Controller;
//
//import com.example.LoginDemo.Entity.Favorite;
//import com.example.LoginDemo.Entity.PropertyEntity;
//import com.example.LoginDemo.Entity.UserEntity;
//import com.example.LoginDemo.Repository.UserRepository;
//import com.example.LoginDemo.Services.FavoriteService;
//import com.example.LoginDemo.Services.UserServices;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/favorites")
//public class FavoriteController {
//
//    @Autowired
//    private FavoriteService favoriteService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private UserServices userServices;
//
////    @GetMapping("/{userId}")
////    public List<Favorite> getUserFavorites(@PathVariable Long userId) {
////        return favoriteService.getUserFavorites(userId);
////    }
////
////    @PostMapping("/add")
////    public ResponseEntity<String> addFavorite(@RequestParam Long userId, @RequestParam Long propertyId) {
////        favoriteService.addFavorite(userId, propertyId);
////        return ResponseEntity.ok("Property added to favorites");
////    }
////
////    @DeleteMapping("/remove")
////    public ResponseEntity<String> removeFavorite(@RequestParam Long userId, @RequestParam Long propertyId) {
////        favoriteService.removeFavorite(userId, propertyId);
////        return ResponseEntity.ok("Property removed from favorites");
////    }
////
////    @GetMapping("/view")
////    public String viewFavorites(Model model, @AuthenticationPrincipal UserDetails userDetails) {
////        UserEntity user = userRepository.findByUsername(userDetails.getUsername())
////                .orElseThrow(() -> new RuntimeException("User not found"));
////        model.addAttribute("userId", user.getId());
////        return "favorites";
////    }
//
//
//
//    @PostMapping("/properties/favorite/add")
//    public String addFavorite(@RequestParam("propertyId") Long propertyId,
//                              @AuthenticationPrincipal UserDetails userDetails) {
//        UserEntity user = userServices.findByUsername(userDetails.getUsername());
//        favoriteService.addFavorite(user.getId(), propertyId);
//        return "redirect:/properties/" + propertyId; // Redirect back to property details
//    }
//
//    @PostMapping("/properties/favorite/remove")
//    public String removeFavorite(@RequestParam("propertyId") Long propertyId,
//                                 @AuthenticationPrincipal UserDetails userDetails) {
//        UserEntity user = userServices.findByUsername(userDetails.getUsername());
//        favoriteService.removeFavorite(user.getId(), propertyId);
//        return "redirect:/properties/" + propertyId; // Redirect back to property details
//    }
//
//    @GetMapping("/user/favorites")
//    public String viewFavorites(Model model, @AuthenticationPrincipal UserDetails userDetails) {
//        UserEntity user = userServices.findByUsername(userDetails.getUsername());
//        List<PropertyEntity> favorites = favoriteService.getUserFavorites(user.getId());
//        model.addAttribute("favorites", favorites);
//        return "user-favorites"; // New template for favorites page
//    }
//
//
//
//
//}


package com.example.LoginDemo.Controller;

import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.UserRepository;
import com.example.LoginDemo.Services.FavoriteService; // Assuming you created this
import com.example.LoginDemo.Services.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class FavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserServices userServices;

    @PostMapping("/properties/favorite/add")
    public String addFavorite(@RequestParam("propertyId") Long propertyId,
                              @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Adding favorite for propertyId: {} by user: {}", propertyId, userDetails.getUsername());
        UserEntity user = userServices.findByUsername(userDetails.getUsername()); // Assuming this method exists
        favoriteService.addFavorite(user.getId(), propertyId);
        return "redirect:/properties/" + propertyId; // Redirect back to property details
    }

    @PostMapping("/properties/favorite/remove")
    public String removeFavorite(@RequestParam("propertyId") Long propertyId,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("Removing favorite for propertyId: {} by user: {}", propertyId, userDetails.getUsername());
        UserEntity user = userServices.findByUsername(userDetails.getUsername());
        favoriteService.removeFavorite(user.getId(), propertyId);
        return "redirect:/properties/" + propertyId; // Redirect back to property details
    }

    @GetMapping("/user/favorites")
    public String viewFavorites(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            logger.warn("Unauthenticated attempt to access /user/favorites");
            return "redirect:/login";
        }
        logger.debug("Viewing favorites for user: {}", userDetails.getUsername());
        UserEntity user = userServices.findByUsername(userDetails.getUsername());
        List<PropertyEntity> favorites = favoriteService.getUserFavorites(user.getId());
        model.addAttribute("favorites", favorites);
        return "user-favorites"; // New template for favorites page
    }
}