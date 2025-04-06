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
    private String username;

    @Column(name = "propId")
    private Long propId;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "status")
    private String status;

    @Column(name = "admin_response")
    private String adminResponse;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // Constructors
    public ComplaintEntity() {
        this.submittedAt = LocalDateTime.now();
        this.status = "Pending";
    }

    public ComplaintEntity(String username, Long propId, String subject, String description) {
        this.username = username;
        this.propId = propId;
        this.subject = subject;
        this.description = description;
        this.submittedAt = LocalDateTime.now();
        this.status = "Pending";
    }
}