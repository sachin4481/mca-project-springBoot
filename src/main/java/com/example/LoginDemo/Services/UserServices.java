package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.UserRepository;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service

public class UserServices {

    private static final Logger logger = LoggerFactory.getLogger(UserServices.class);
    @Autowired
    private UserRepository userRepository;


    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:9999}")
    private String baseUrl;

//    public void registerUser(UserEntity user) {
//        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
//            throw new RuntimeException("Username already exists!");
//        }
//
//        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encode the password
//        user.setRole("USER"); // Assign a default role
//        user.setVerified(false);
//        userRepository.save(user);
//
//        String token = UUID.randomUUID().toString();
//
//        VerificationToken verificationToken = new VerificationToken(token, user);
//        tokenRepository.save(verificationToken);
//
//        sendVerificationEmail(user.getEmail(), token);
//
//
//    }
//    private void sendVerificationEmail(String email, String token) {
//        String verificationUrl = baseUrl + "/auth/verify?token=" + token;
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(email);
//        message.setSubject("Verify Your Email");
//        message.setText("Click the link to verify your email: " + verificationUrl);
//        mailSender.send(message);
//        logger.info("Verification email sent to: {}", email);
//    }
//
//    @Transactional
//    public boolean verifyUser(String token) {
//        logger.info("Verifying token: {}", token);
//        Optional<VerificationToken> tokenOptional = tokenRepository.findByToken(token);
//        if (tokenOptional.isEmpty()) {
//            logger.warn("Invalid or expired token: {}", token);
//            return false;
//        }
//
//        VerificationToken verificationToken = tokenOptional.get();
//        UserEntity user = verificationToken.getUser();
//
//        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
//            logger.warn("Token expired for user: {}", user.getUsername());
//            return false;
//        }
//
//        user.setVerified(true);
//        verificationToken.setVerifiedAt(LocalDateTime.now());
////        userRepository.save(user); // Save user with verified = true
////        tokenRepository.save(verificationToken); // Update token with verifiedAt
//
//        try {
//            userRepository.save(user);
//            tokenRepository.save(verificationToken);
//        } catch (Exception e) {
//            logger.error("Failed to save verification data", e);
//            throw e; // Rollback transaction
//        }
//
//
//        logger.info("User {} verified successfully", user.getUsername());
//        return true;
//    }

// Register user with OTP verification
public void registerUser(UserEntity user) {
    if (userRepository.findByUsername(user.getUsername()).isPresent()) {
        throw new RuntimeException("Username already exists!");
    }

    user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt password
    user.setRole("USER");
    user.setVerified(false);

    // Generate OTP
    String otp = generateOtp();
    user.setOtp(otp);
    user.setOtpExpiry(LocalDateTime.now().plusMinutes(5)); // OTP valid for 5 minutes
    userRepository.save(user);

    sendOtpEmail(user.getEmail(), otp);
}

    // Generate a 6-digit OTP
    private String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    // Send OTP via email
    private void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP for Registration");
        message.setText("Your OTP is: " + otp + ". It is valid for 5 minutes.");
        mailSender.send(message);
        logger.info("OTP sent to {}", email);
    }

    // Verify OTP and activate the account
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        UserEntity user = userRepository.findByEmail(email);
        if (user != null && user.getOtp() != null
                && user.getOtpExpiry().isAfter(LocalDateTime.now())
                && user.getOtp().equals(otp)) {

            logger.info("yaha tak to aa gaye ab age");
            user.setVerified(true);
            logger.info("0yaha tak to aa gaye ab age");
            user.setOtp(null);
            user.setOtpExpiry(null);
            logger.info(" 1yaha tak to aa gaye ab age");
            userRepository.save(user);
            logger.info("User {} verified successfully", email);
            return true;
        }
        logger.warn("Invalid OTP for {}", email);
        return false;
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


    // Find user by email
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Generate OTP and store it
    public void generateOtp(UserEntity user) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000); // Generate 6-digit OTP
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5)); // OTP valid for 5 minutes
        userRepository.save(user);
    }

    // Find user by OTP
    public UserEntity findByOtp(String otp) {
        return userRepository.findByOtp(otp);
    }

    // Update password
    public void updatePassword(UserEntity user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null); // Clear OTP after successful password update
        user.setOtpExpiry(null);
        userRepository.save(user);
    }


}