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

//    @ManyToOne
//    @JoinColumn(name = "propId")
//    private PropertyInfo property;


    @ManyToOne
    @JoinColumn(name = "prop_id",nullable = false) // Changed to match likely DB column name
    private PropertyInfo property;

    public Favorite() {}

    public Favorite(UserEntity user, PropertyInfo property) {
        this.user = user;
        this.property = property;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public PropertyInfo getProperty() {
        return property;
    }

    public void setProperty(PropertyInfo property) {
        this.property = property;
    }
// Getters and Setters
}
