package com.example.LoginDemo.Controller;

import com.example.LoginDemo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import com.example.LoginDemo.Entity.PropInquiry;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Services.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiryService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/properties/{id}/inquiry")
    public String sendInquiry(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            inquiryService.createInquiry(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Inquiry sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send inquiry: " + e.getMessage());
        }
        return "redirect:/properties/" + id;
    }

    @GetMapping("/my-inquiries")
    public String showMyInquiries(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<PropInquiry> inquiries = inquiryService.getInquiriesForOwner(user.getId());

        System.out.println("Inquiries found: " + inquiries.size()); // Add this line
        model.addAttribute("inquiries", inquiries);
        return "my-inquiries";
    }


    @PostMapping("/inquiries/{id}/close")
    public String closeInquiry(@PathVariable Long id) {
        inquiryService.closeInquiry(id);
        return "redirect:/my-inquiries";
    }
}