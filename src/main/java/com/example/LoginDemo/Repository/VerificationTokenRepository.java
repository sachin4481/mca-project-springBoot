package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.VerificationToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    // Find all verification tokens for a given user ID
    List<VerificationToken> findByUserId(Long userId);

    // Delete all verification tokens for a given user ID
    @Transactional
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.user.id = :userId")
    void deleteByUserId(Long userId);
}