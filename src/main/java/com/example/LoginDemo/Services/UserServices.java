package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service

public class UserServices {

    private static final Logger logger = LoggerFactory.getLogger(UserServices.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;

    public void registerUser(UserEntity user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }


        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encode the password
        user.setRole("USER"); // Assign a default role
        userRepository.save(user);
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



        // Handle profile photo upload
//        if (profilePhoto != null && !profilePhoto.isEmpty()) {
//            File uploadDirectory = new File(uploadDir);
////            if (!uploadDirectory.exists()) {
////                logger.info("Creating upload directory: {}", uploadDir);
////                uploadDirectory.mkdirs();
////            }
//            String fileName = System.currentTimeMillis() + "-" + profilePhoto.getOriginalFilename();
//            File dest = new File(uploadDirectory.getAbsolutePath() + File.separator + fileName);
//            logger.info("Saving profile photo to: {}", dest.getAbsolutePath());
//            profilePhoto.transferTo(dest);
//            existingUser.setProfilePhoto("/uploads/" + fileName); // Ensure this path is set
//            logger.info("Profile photo path set to: {}", existingUser.getProfilePhoto());
//        } else {
//            logger.warn("No profile photo provided or file is empty");
//        }


//        // Password update could be optional; add logic if needed
//        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
//            existingUser.setPassword(updatedUser.getPassword()); // Encode if using BCrypt
//        }
        return userRepository.save(existingUser);
    }




}