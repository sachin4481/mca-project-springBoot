package com.example.LoginDemo.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@Entity
@Table(name = "property_info")
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyInfo {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prop_id")
    private Long propId;

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


    private Double price;
    private Date listingDate;

    @Column(nullable = false)
    private String status;

    @PrePersist
    public void setDefaultStatus() {
        if (this.status == null) {
            this.status = "AVAILABLE";
        }
    }

}
