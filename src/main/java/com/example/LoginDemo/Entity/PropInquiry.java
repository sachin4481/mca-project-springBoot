package com.example.LoginDemo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "prop_inquiries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropInquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inqId;

    @ManyToOne
    @JoinColumn(name = "prop_id", nullable = false)
    private PropertyInfo property;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDateTime inqDate = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "ACTIVE";
}