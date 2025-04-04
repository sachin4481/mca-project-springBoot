package com.example.LoginDemo.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "favorites")
@Data
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyEntity property;

    public Favorite() {}

    public Favorite(UserEntity user, PropertyEntity property) {
        this.user = user;
        this.property = property;
    }

    // Getters and Setters
}
