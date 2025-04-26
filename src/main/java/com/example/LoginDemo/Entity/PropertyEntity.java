package com.example.LoginDemo.Entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "pincode" ,length = 6)
    private String pincode;

    @Column(length = 15)
    private Double area;

    @Column(length = 15)
    private String status;

    @ManyToOne
    private UserEntity user;

    private String image1;
    private String image2;
    private String image3;
}
