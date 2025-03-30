package com.example.LoginDemo.Controller;


import com.example.LoginDemo.Entity.ComplaintEntity;
import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Services.ComplaintServices;
import com.example.LoginDemo.Services.PropertyServices;
import com.example.LoginDemo.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    private UserServices userServices;


    @Autowired
    private ComplaintServices complaintServices;

    @Autowired
    private PropertyServices propertyServices;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public String adminDashboard(Model model)
    {
        List<UserEntity> users = userServices.getAllUser();
        List<PropertyEntity> properties = propertyServices.getAllProperty();
        List<ComplaintEntity> complaints= complaintServices.getAllComplaints();
        model.addAttribute("users",users);
        model.addAttribute("properties",properties);
        model.addAttribute("complaints",complaints);
        return "admin";


    }

    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id) {
        //verificationTokenRepository.deleteByUserId(id);
        userServices.deleteUserById(id);
        return "redirect:/admin/dashboard";
    }


    // complaints
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



}
