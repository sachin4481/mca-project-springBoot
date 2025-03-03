package com.example.LoginDemo.Controller;

import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private UserServices userServices;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/Registration")
    public String registration() {
        return "Registration";
    }


//    @PostMapping("/register")
//    public String registerUser(
//            @RequestParam String username,
//            @RequestParam String password) {
//
//        UserEntity user = new UserEntity();
//        user.setUsername(username);
//        user.setPassword(password);
//        System.out.println("Registering user: " + username);
//        userServices.registerUser(user);
//        return "redirect:/login";
//    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String gender,
            @RequestParam String address,
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        try {
            UserEntity user = new UserEntity();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setGender(gender);
            user.setAddress(address);
            user.setUsername(username);
            user.setPassword(password);

            userServices.registerUser(user);
            return "redirect:/login"; // Redirect to the login page after registration
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage()); // Add error message to the model
            return "Registration"; // Return to the registration page with an error message
        }
    }


    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }
}