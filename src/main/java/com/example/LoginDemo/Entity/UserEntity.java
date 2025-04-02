package com.example.LoginDemo.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "user")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,length = 20)
    private String username;


    @Column(nullable = false)
    private String password;

    @Column(nullable = false,length = 20)
    private String role;



    @Column(nullable = false, unique = true,length = 50)
    private String email;

    @Column(nullable = false ,length = 20)
    private String firstName;

    @Column(nullable = false,length = 20)
    private String lastName;

    @Column(nullable = false,length = 10)
    private String phone;

    @Column(nullable = false,length = 10)
    private String gender;

    @Column(nullable = false,length = 50)
    private String address;

    @Column(name = "profile_photo")
    private String profilePhoto;


    @Column(nullable = false)
    private boolean verified=false;


    private String otp;
    private LocalDateTime otpExpiry;

    public boolean isOtpValid() {
        return otpExpiry != null && otpExpiry.isAfter(LocalDateTime.now());
    }



}