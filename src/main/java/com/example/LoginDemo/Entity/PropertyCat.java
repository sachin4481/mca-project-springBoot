package com.example.LoginDemo.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "property_cat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PropertyCat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
