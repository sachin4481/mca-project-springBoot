package com.example.LoginDemo.Entity;


import jakarta.persistence.*;
import lombok.Data;



@Entity
@Table(name = "Property")
@Data
public class PropertyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String location;
    private double price;

    @Column(name = "pincode") // New field for pincode
    private String pincode;
    private double area; //size
    private String status; // "AVAILABLE", "SOLD"

    @ManyToOne
    private UserEntity user;

    private String image1;
    private String image2;
    private String image3;
}
