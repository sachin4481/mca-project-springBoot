package com.example.LoginDemo.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "property_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long propDetailsId;

    @OneToOne
    @JoinColumn(name = "prop_id", nullable = false)
    private PropertyInfo propertyInfo;

    private Integer bhk;
    private Integer floors;
    private Boolean corner;
    private Integer floor;
    private Boolean furnished;
    private Integer washroom;
    private Boolean parking;
    private String schemeName;
    private String propDetails;

}
