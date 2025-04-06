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



    public void registerUser(UserEntity user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
          throw new RuntimeException("Username already exists!");
         }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
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


    @Transactional
    public boolean verifyOtp(String email, String otp) {
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            if (user.getOtp() != null
                    && user.getOtpExpiry().isAfter(LocalDateTime.now())
                    && user.getOtp().equals(otp)) {

                user.setVerified(true);
                user.setOtp(null);
                user.setOtpExpiry(null);
                userRepository.save(user);

                return true;
            }
        }
        logger.warn("Invalid OTP for {}", email);
        return false;
    }


    public boolean resendOtp(String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            String otp = generateOtp(); // Your existing OTP generator
            UserEntity user = userOpt.get();
            user.setOtp(otp);
            //user.setOtpGeneratedTime(LocalDateTime.now());
            userRepository.save(user);

            // Send the OTP again via email
            sendOtpEmail(email,otp);

            return true;
        }
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


    // Find user by email diff then repository method
//    public UserEntity findbyEmail(String email) {
//        return userRepository.findByEmail(email).orElseThrow();
//    }

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


    //for changing the user role
    public void changeUserRole(Long userId, String newRole) {
        logger.debug("Changing role for user ID: {} to {}", userId, newRole);
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setRole(newRole);
            userRepository.save(user);
            logger.info("Role updated to {} for user: {}", newRole, user.getUsername());
        } else {
            logger.warn("User not found for ID: {}", userId);
        }
    }


}