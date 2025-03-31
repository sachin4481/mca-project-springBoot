package com.example.LoginDemo.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/Registration", "/register", "/css/**", "/css_main/**", "/js/**", "/static/**","/forgot-password", "/verify-otp", "/reset-password","/verify-otp-email").permitAll() // Allow public access
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Restrict admin pages
                        .anyRequest().authenticated() // Protect all other pages
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true) // Redirect to home after login
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/home") // Redirect to home after logout
                        .invalidateHttpSession(true) // Invalidate session
                        .deleteCookies("JSESSIONID") // Delete session cookie
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use BCrypt for password hashing
    }
}
