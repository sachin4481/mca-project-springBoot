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
    @Column(length = 20)
    private String title;


    private String description;
    @Column(length = 20)
    private String location;
    @Column(length = 10)
    private double price;

    @Column(name = "pincode" ,length = 6) // New field for pincode
    private String pincode;
    @Column(length = 15)
    private Double area;//size
    @Column(length = 15)
    private String status; // "AVAILABLE", "SOLD"

    @ManyToOne
    private UserEntity user;

    private String image1;
    private String image2;
    private String image3;
}
