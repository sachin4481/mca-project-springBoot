package com.example.LoginDemo.Controller;


import com.example.LoginDemo.Entity.*;
import com.example.LoginDemo.Repository.PropertyCatRepository;
//import com.example.LoginDemo.Repository.VerificationTokenRepository;
import com.example.LoginDemo.Repository.PropertyRepository;
import com.example.LoginDemo.Repository.UserRepository;
import com.example.LoginDemo.Services.ComplaintServices;
import com.example.LoginDemo.Services.PropertyServices;
import com.example.LoginDemo.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    private UserServices userServices;

//    @Autowired
//    VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PropertyCatRepository propertyCatRepository;

    @Autowired
    private ComplaintServices complaintServices;

    @Autowired
    private PropertyServices propertyServices;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

//admin home page
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public String adminDashboard(Model model,@RequestParam(defaultValue = "0") int page)
    {

        List<UserEntity> users = userServices.getAllUser();
        List<PropertyInfo> properties = propertyServices.getAllProperty();
        List<ComplaintEntity> complaints= complaintServices.getAllComplaints();
        List<PropertyCat> categories = propertyCatRepository.findAll();
        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, 10));
        Page<PropertyEntity> propertyPage = propertyRepository.findAll(PageRequest.of(page, 10));
        model.addAttribute("categories", categories);
        model.addAttribute("users",usersPage);
        model.addAttribute("properties",propertyPage);
        model.addAttribute("complaints",complaints);
        return "admin";


    }
    //category management
    @PostMapping("/add-category")
    public String addCategory(@RequestParam String name) {
        PropertyCat category = new PropertyCat();
        category.setName(name);
        propertyCatRepository.save(category);
        return "redirect:/admin/dashboard?success";
    }

    @PostMapping("/delete-category/{id}")
    public String deleteCategory(@PathVariable Long id) {
        propertyCatRepository.deleteById(id);
        return "redirect:/admin/dashboard?deleted";
    }
//user management
    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id) {
//        verificationTokenRepository.deleteByUserId(id);

     //   propertyServices.deleteById();
        userServices.deleteUserById(id);
        return "redirect:/admin/dashboard";
    }

//for complaint management
    @GetMapping("/complaints")
    public String showAdminComplaints(Model model) {
        List<ComplaintEntity> complaints = complaintServices.getAllComplaints();
        model.addAttribute("complaints", complaints);
        return "admin-complaints";
    }

    @PostMapping("/complaints/resolve")
    public String resolveComplaint(@RequestParam("complaintId") Long complaintId,
                                   @RequestParam("adminResponse") String adminResponse) {
        complaintServices.resolveComplaint(complaintId, adminResponse);
        return "redirect:/admin/complaints";
    }


    // Change role
    @PostMapping("/change-role")
    public String changeRole(@RequestParam("userId") Long userId,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {
        userServices.changeUserRole(userId, "ADMIN");
        // Fetch paginated data again
        List<PropertyEntity> properties = propertyServices.getAllProperty();
        List<ComplaintEntity> complaints = complaintServices.getAllComplaints();
        List<PropertyCat> categories = propertyCatRepository.findAll();
        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, 10));
        Page<PropertyEntity> propertyPage = propertyRepository.findAll(PageRequest.of(page, 10));
        model.addAttribute("message", "User role changed to ADMIN!");
        model.addAttribute("categories", categories);
        model.addAttribute("users", usersPage);
        model.addAttribute("properties", propertyPage);
        model.addAttribute("complaints", complaints);
        return "admin"; // Return to the same admin dashboard template
    }

}
