package com.example.LoginDemo.Entity;



import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inqId;

    @ManyToOne
    @JoinColumn(name = "prop_id", nullable = false)
    private PropertyEntity property;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private LocalDateTime inqDate;

    // Constructors
    public Inquiry() {
        this.inqDate = LocalDateTime.now();
    }

    public Inquiry(PropertyEntity property, UserEntity user) {
        this.property = property;
        this.user = user;
        this.inqDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getInqId() {
        return inqId;
    }

    public PropertyEntity getProperty() {
        return property;
    }

    public void setProperty(PropertyEntity property) {
        this.property = property;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public LocalDateTime getInqDate() {
        return inqDate;
    }

    public void setInqDate(LocalDateTime inqDate) {
        this.inqDate = inqDate;
    }
}
