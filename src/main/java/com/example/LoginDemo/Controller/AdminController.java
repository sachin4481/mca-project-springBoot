package com.example.LoginDemo.Controller;


import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    private UserServices userServices;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-user")
    public List<UserEntity> allUser()
    {
        return userServices.getAll();
    }



}
