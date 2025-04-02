package com.example.LoginDemo.Controller;

import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.UserRepository;
import com.example.LoginDemo.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserServices userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password"; // Thymeleaf template
    }


@PostMapping("/forgot-password")
public String processForgotPassword(@RequestParam("email") String email, Model model) {
    Optional<UserEntity> userOptional = userRepository.findByEmail(email);
    if (userOptional.isEmpty()) {
        model.addAttribute("error", "Email not found!");
        return "forgot-password";
    }

    UserEntity user = userOptional.get();
    userService.generateOtp(user);
    sendOtpEmail(user.getEmail(), user.getOtp());

    model.addAttribute("message", "OTP sent to your email!");
    return "verify-otp";
}

    private void sendOtpEmail(String to, String otp) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject("Password Reset OTP");
        email.setText("Your OTP for password reset is: " + otp);
        mailSender.send(email);
    }

    @GetMapping("/verify-otp")
    public String showOtpPage() {
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otp, Model model) {
        UserEntity user = userService.findByOtp(otp);
        if (user == null || !user.isOtpValid()) {
            model.addAttribute("error", "Invalid or expired OTP!");
            return "verify-otp";
        }

        model.addAttribute("otp", otp);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("otp") String otp,
                                @RequestParam("password") String password, Model model) {
        UserEntity user = userService.findByOtp(otp);
        if (user == null || !user.isOtpValid()) {
            model.addAttribute("error", "Invalid or expired OTP!");
            return "reset-password";
        }

        userService.updatePassword(user, password);

        model.addAttribute("message", "Password reset successfully! Please log in.");
        return "login";
    }
}
