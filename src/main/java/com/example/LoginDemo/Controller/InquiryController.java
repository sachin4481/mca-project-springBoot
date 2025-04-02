package com.example.LoginDemo.Controller;




import com.example.LoginDemo.Entity.Inquiry;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Services.InquiryService;
import com.example.LoginDemo.Services.UserServices;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping()
public class InquiryController {

    private final InquiryService inquiryService;
    private final UserServices userService;


    public InquiryController(InquiryService inquiryService,UserServices userService) {
        this.inquiryService = inquiryService;
        this.userService = userService;
    }

    @PostMapping("/inquiries/create/{propertyId}")
    public String createInquiry(@PathVariable Long propertyId, Principal principal, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        UserEntity user = userService.findByUsername(username);

        inquiryService.createInquiry(user.getId(), propertyId);
        redirectAttributes.addFlashAttribute("successMessage", "Your inquiry has been sent successfully!");


        return "redirect:/properties/" + propertyId;  // Redirect back to property details
    }

    @GetMapping("/owner/inquiries")
    public String showOwnerInquiries(Model model, Principal principal) {
        String username = principal.getName();
        UserEntity user = userService.findByUsername(username);

        List<Inquiry> inquiries = inquiryService.getInquiriesForOwner(user.getId());
        model.addAttribute("inquiries", inquiries);

        return "owner-inquiries";
    }


    @PostMapping("/owner/inquiries/delete/{id}")
    public String deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return "redirect:/owner/inquiries";
    }

}
