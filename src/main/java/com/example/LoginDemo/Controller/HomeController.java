package com.example.LoginDemo.Controller;

import com.example.LoginDemo.Entity.ComplaintEntity;
import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.PropertyRepository;
import com.example.LoginDemo.Services.ComplaintServices;
import com.example.LoginDemo.Services.FavoriteService;
import com.example.LoginDemo.Services.PropertyServices;
import com.example.LoginDemo.Services.UserServices;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserServices userServices;

    @Autowired
    private PropertyServices propertyServices;



    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private  FavoriteService favoriteService;



    @Autowired
    private ComplaintServices complaintServices;

    //login page mapping
    @GetMapping("/login")
    public String login() {
        return "login";
    }


    //registration page mapping
    @GetMapping("/Registration")
    public String registration() {

        return "Registration";
    }

// "/" home page maping
    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }

    //property list page maping
    @GetMapping("/properties/list")
    public String showListPropertyForm(Model model) {
        model.addAttribute("property", new PropertyEntity());
        return "list-property";
    }

//registration logic
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
            return "redirect:/verify-otp-email?email=" + email; // Redirect to OTP verification page
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "registration";
        }
    }

    // verification page after otp sent maping
    @GetMapping("/verify-otp-email")
    public String showOtpVerificationPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verify-otp-email";
    }

    // OTP verification logic
    @PostMapping("/auth/verify-otp-email")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, RedirectAttributes redirectAttributes) {
        if (userServices.verifyOtp(email, otp)) {
            System.out.println("OTP verified successfully for: " + email);
            return "redirect:/login?verified=true";
        } else {
            System.out.println("Invalid or expired OTP for: " + email);
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP. Please try again.");
            return "redirect:/verify-otp-email?email=" + email;
        }
    }



    // home page mapping for "/home"
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails userDetails, HttpSession session) {
        if (userDetails != null) {
            String username = userDetails.getUsername();
            session.setAttribute("username", username); // Store in session


            String role = userServices.getUserRole(username);
            logger.info("User {} authenticated with role {}", username, role);

            if ("ADMIN".equalsIgnoreCase(role)) {
                return "redirect:/admin/dashboard";
            }

            else if ("USER".equalsIgnoreCase(role)) {
                return "redirect:/properties";
            }else {
                logger.warn("Unknown role {} for user {}, redirecting to login", role, username);
                return "redirect:/login";
            }
        }

        // Show home page for unauthenticated users
        return "home";
    }

    //welcome page after login mapping
    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }

    //delete property in admin side logic
    @PostMapping("/admin/delete-property/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProperty(@PathVariable Long id) {
        propertyServices.deleteById(id);
        return "redirect:/admin/dashboard";
    }


    @PostMapping("/auth/resend-otp")
    public String resendOtp(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        boolean sent = userServices.resendOtp(email);
        if (sent) {
            redirectAttributes.addFlashAttribute("message", "OTP resent successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Could not resend OTP. Please try again.");
        }
        return "redirect:/verify-otp-email?email=" + email;
    }






    @GetMapping("/user/profile")
    public String showProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        UserEntity currentUser = userServices.findByUsername(username);
        model.addAttribute("user", currentUser);
        return "user-profile";
    }


    @PostMapping("/user/profile")
    public String updateProfile(@ModelAttribute UserEntity user,
                                @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        String username = userDetails.getUsername();
        user.setUsername(username); // Ensure username isn’t changed
        userServices.updateUser(user);
        return "redirect:/user/profile?updated=true";
    }


@GetMapping("/user/complaint")
public String showComplaintForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
        return "redirect:/login";
    }

    UserEntity loggedInUser = userServices.findByUsername(userDetails.getUsername());

//    List<PropertyInfo> inquiredProperties = propertyServices.getInquiredPropertiesByUserId(loggedInUser.getId());
    List<PropertyInfo> allProperties = propertyServices.getAllProperty();

    model.addAttribute("complaint", new ComplaintEntity());
    model.addAttribute("properties", allProperties); // Only show properties inquired by this user

    return "complaint";
}

    @PostMapping("/user/complaint")
    public String submitComplaint(@ModelAttribute("complaint") ComplaintEntity complaint,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        complaint.setUsername(userDetails.getUsername());
        complaintServices.submitComplaint(complaint);
        return "redirect:/user/complaint?submitted=true";
    }
}