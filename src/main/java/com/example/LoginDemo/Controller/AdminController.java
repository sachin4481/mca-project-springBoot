package com.example.LoginDemo.Controller;


import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Services.PropertyServices;
import com.example.LoginDemo.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    private UserServices userServices;

    @Autowired
    private PropertyServices propertyServices;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public String adminDashboard(Model model)
    {
        List<UserEntity> users = userServices.getAllUser();
        List<PropertyEntity> properties = propertyServices.getAllProperty();
        model.addAttribute("users",users);
        model.addAttribute("properties",properties);
        return "admin";


    }    





}
