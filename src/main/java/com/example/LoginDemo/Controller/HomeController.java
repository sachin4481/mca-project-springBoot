package com.example.LoginDemo.Controller;

import com.example.LoginDemo.Entity.ComplaintEntity;
import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Entity.VerificationToken;
import com.example.LoginDemo.Repository.PropertyRepository;
import com.example.LoginDemo.Repository.VerificationTokenRepository;
import com.example.LoginDemo.Security.CustomUserDetailsService;
import com.example.LoginDemo.Services.ComplaintServices;
import com.example.LoginDemo.Services.PropertyServices;
import com.example.LoginDemo.Services.UserServices;
import jakarta.servlet.http.HttpSession;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
    private ComplaintServices complaintServices;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/Registration")
    public String registration() {

        return "Registration";
    }


    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }

    @GetMapping("/properties/list")
    public String showListPropertyForm(Model model) {
        model.addAttribute("property", new PropertyEntity());
        return "list-property";
    }



@GetMapping("/auth/verify")
public String verifyUser(@RequestParam("token") String token) {
    logger.info("Received verification request for token: {}", token);
    if (userServices.verifyUser(token)) {
        return "redirect:/home";
    }
    logger.warn("Verification failed for token: {}", token);
    return "redirect:/login?error";

}

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails userDetails, HttpSession session) {
        if (userDetails != null) {
            String username = userDetails.getUsername();
            session.setAttribute("username", username); // Store in session

            String role = userServices.getUserRole(username);
            logger.info("User {} authenticated with role {}", username, role);

            if ("ADMIN".equalsIgnoreCase(role)) {
                return "redirect:/admin/dashboard";
            } else if ("USER".equalsIgnoreCase(role)) {
                return "redirect:/properties";
            }else {
                logger.warn("Unknown role {} for user {}, redirecting to login", role, username);
                return "redirect:/login";
            }
        }

        // Show home page for unauthenticated users
        return "home";

//        String username = userDetails.getUsername();
//        session.setAttribute("username", username); // Store in session
//
//        String role = userServices.getUserRole(username);
//        logger.info("User {} authenticated with role {}", username, role);
//
//        if ("ADMIN".equalsIgnoreCase(role)) {
//            return "redirect:/admin/dashboard";
//        } else if ("USER".equalsIgnoreCase(role)) {
//            return "redirect:/properties";
//        } else {
//            logger.warn("Unknown role {} for user {}, redirecting to login", role, username);
//            return "redirect:/login";
//        }
    }







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
            model.addAttribute("error", e.getMessage());// Add error message to the model
            model.addAttribute("message", "A verification email has been sent. Please check your inbox.");
            return "Registration"; // Return to the registration page with an error message
        }
    }




    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }

    @PostMapping("/admin/delete-property/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProperty(@PathVariable Long id) {
        propertyServices.deleteById(id);
        return "redirect:/admin/dashboard";
    }




    @PostMapping("/properties/list")
    public String listProperty(@ModelAttribute PropertyEntity property,
                               @RequestParam("images") MultipartFile[] images,
                               @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        String username = userDetails.getUsername();
        UserEntity currentUser = userServices.findByUsername(username);
        property.setUser(currentUser); // Set the UserEntity when listing
        propertyServices.listProperty(property, images);
        return "redirect:/properties";
    }


    @GetMapping("/properties/{id}")
    public String getPropertyDetails(@PathVariable Long id, Model model) {
        model.addAttribute("property", propertyServices.getPropertyById(id));
        return "property-details";
    }

    @GetMapping("/properties/edit/{id}")
    public String showEditPropertyForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        PropertyEntity property = propertyServices.getPropertyById(id);
        String username = userDetails.getUsername();
        UserEntity currentUser = userServices.findByUsername(username);

        if (!property.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/properties"; // Redirect if unauthorized
        }

        model.addAttribute("property", property);
        return "edit-property";
    }
    @PostMapping("/properties/edit/{id}")
    public String updateProperty(@PathVariable Long id,
                                 @ModelAttribute PropertyEntity property,
                                 @RequestParam("images") MultipartFile[] images,
                                 @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        String username = userDetails.getUsername();
        UserEntity currentUser = userServices.findByUsername(username); // Fetch from service
        propertyServices.updateProperty(id, property, images, currentUser);
        return "redirect:/properties/{id}";
    }


    //updating user and user profile

    //show profile
    @GetMapping("/user/profile")
    public String showProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        UserEntity currentUser = userServices.findByUsername(username);
        model.addAttribute("user", currentUser);
        return "user-profile";
    }


    //update the profile
    @PostMapping("/user/profile")
    public String updateProfile(@ModelAttribute UserEntity user,

                                @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        String username = userDetails.getUsername();
        user.setUsername(username); // Ensure username isn’t changed
        userServices.updateUser(user);
        return "redirect:/user/profile?updated=true";
    }



    // Complaint Form
    @GetMapping("/user/complaint")
    public String showComplaintForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        model.addAttribute("complaint", new ComplaintEntity());
        model.addAttribute("properties", propertyServices.getAllProperty()); // For property selection
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