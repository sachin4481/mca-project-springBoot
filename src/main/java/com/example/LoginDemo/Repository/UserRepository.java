package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);
    UserEntity findByOtp(String otp);

    Page<UserEntity> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String search, String search1, PageRequest of);
}