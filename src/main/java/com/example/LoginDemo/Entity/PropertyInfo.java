package com.example.LoginDemo.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "property_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyInfo {

    @ManyToOne
    @JoinColumn(name = "prop_cat_id", nullable = false)
    private PropertyCat propertyCategory;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String propTitle;
    private String location;
    private String pinCode;
    private String area;
    private String facing;

    @Column(nullable = false)
    private String img1;

    private String img2;
    private String img3;
    private String img4;
    private String img5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long propId;

    private Double price;
    private Date listingDate;

    @Column(nullable = false)
    private String status = "AVAILABLE";
}
