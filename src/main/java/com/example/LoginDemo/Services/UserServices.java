package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Entity.VerificationToken;
import com.example.LoginDemo.Repository.UserRepository;
import com.example.LoginDemo.Repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service

public class UserServices {

    private static final Logger logger = LoggerFactory.getLogger(UserServices.class);
    @Autowired
    private UserRepository userRepository;


    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void registerUser(UserEntity user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encode the password
        user.setRole("USER"); // Assign a default role
        user.setVerified(false);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);

        sendVerificationEmail(user.getEmail(), token);


    }
    private void sendVerificationEmail(String email, String token) {
        String verificationUrl = baseUrl + "/auth/verify?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify Your Email");
        message.setText("Click the link to verify your email: " + verificationUrl);
        mailSender.send(message);
        logger.info("Verification email sent to: {}", email);
    }

    @Transactional
    public boolean verifyUser(String token) {
        logger.info("Verifying token: {}", token);
        Optional<VerificationToken> tokenOptional = tokenRepository.findByToken(token);
        if (tokenOptional.isEmpty()) {
            logger.warn("Invalid or expired token: {}", token);
            return false;
        }

        VerificationToken verificationToken = tokenOptional.get();
        UserEntity user = verificationToken.getUser();

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.warn("Token expired for user: {}", user.getUsername());
            return false;
        }

        user.setVerified(true);
        verificationToken.setVerifiedAt(LocalDateTime.now());
//        userRepository.save(user); // Save user with verified = true
//        tokenRepository.save(verificationToken); // Update token with verifiedAt

        try {
            userRepository.save(user);
            tokenRepository.save(verificationToken);
            tokenRepository.delete(verificationToken);
        } catch (Exception e) {
            logger.error("Failed to save verification data", e);
            throw e; // Rollback transaction
        }


        logger.info("User {} verified successfully", user.getUsername());
        return true;
    }




    public List<UserEntity> getAllUser()
    {
        return userRepository.findAll();
    }


    public Optional<UserEntity> getUserById(Long id)
    {
        return userRepository.findById(id);
    }

    public void deleteUserById(Long id)
    {
        userRepository.deleteById(id);
    }

    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


    public UserEntity updateUser(UserEntity updatedUser) throws IOException {
        UserEntity existingUser = findByUsername(updatedUser.getUsername());
        // Update fields
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setGender(updatedUser.getGender());
        existingUser.setAddress(updatedUser.getAddress());




        return userRepository.save(existingUser);
    }

    //get user role by id
    public String getUserRole(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getRole();
    }



}