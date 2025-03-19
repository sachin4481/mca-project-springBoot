package com.example.LoginDemo.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaint")
@Data
public class ComplaintEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username; // The user submitting the complaint

    @Column(name = "property_id")
    private Long propertyId; // Optional: Link to a specific property (can be null)

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "status")
    private String status; // e.g., "Pending", "Resolved"

    @Column(name = "admin_response")
    private String adminResponse; // Admin's solution or reply

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // Constructors
    public ComplaintEntity() {
        this.submittedAt = LocalDateTime.now();
        this.status = "Pending"; // Default status
    }

    public ComplaintEntity(String username, Long propertyId, String subject, String description) {
        this.username = username;
        this.propertyId = propertyId;
        this.subject = subject;
        this.description = description;
        this.submittedAt = LocalDateTime.now();
        this.status = "Pending";
    }
}