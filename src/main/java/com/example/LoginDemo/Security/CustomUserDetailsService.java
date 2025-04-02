//package com.example.LoginDemo.Security;
//
//import com.example.LoginDemo.Entity.UserEntity;
//import com.example.LoginDemo.Repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        System.out.println("Loading user: " + username);
//        UserEntity user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
//
//        // Returning user without roles/authorities
//        return new org.springframework.security.core.userdetails.User(
//                user.getUsername(),
//                user.getPassword(),
//
//                true,  // Enabled
//                true,  // Account non-expired
//                true,  // Credentials non-expired
//                true,  // Account non-locked
//
//                new ArrayList<>()  // Empty list of authorities (no roles)
//        );
//    }
//}

package com.example.LoginDemo.Security;

import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user: " + username);

        Optional<UserEntity> userOpt=userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("Username not found!");
        }
        UserEntity user = userOpt.get();
//        if (!user.isVerified()) {
//            throw new UsernameNotFoundException("User not verified! Please verify your email.");
//        }


        System.out.println("User roles: " + user.getRole());




//        // Convert the role to a GrantedAuthority
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" +user.getRole())); // Add the role as an authority







        // Return the UserDetails object with roles
        return new User(
                user.getUsername(),
                user.getPassword(),
                true,  // Enabled
                true,  // Account non-expired
                true,  // Credentials non-expired
                true,  // Account non-locked
                authorities  // role of user
        );
    }
}
