package com.example.LoginDemo.Controller;

import com.example.LoginDemo.Entity.Feedback;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.FeedbackRepository;
import com.example.LoginDemo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/feedback")
    public String showFeedbackForm(Model model) {
        // Get the current authenticated username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Find the user by username to get the ID
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("userId", user.getId());
        return "feedback";
    }

    @PostMapping("/feedback")
    public String saveFeedback(@RequestParam String message,
                               RedirectAttributes redirectAttributes) {
        // Get the current authenticated username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Find the user by username to get the ID
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Feedback feedback = new Feedback();
        feedback.setUserId(user.getId());
        feedback.setMessage(message);
        feedback.setSubmittedAt(LocalDateTime.now());

        feedbackRepository.save(feedback);

        redirectAttributes.addFlashAttribute("successMessage", "Feedback submitted successfully!");
        return "redirect:/properties";
    }

    @GetMapping("/admin/feedback")
    @ResponseBody
    public List<Feedback> getFeedback(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return feedbackRepository.findByUserId(userId);
        }
        return feedbackRepository.findAll();
    }
}