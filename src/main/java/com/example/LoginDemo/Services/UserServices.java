package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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





}