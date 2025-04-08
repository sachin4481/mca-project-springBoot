package com.example.LoginDemo.Controller;


import com.example.LoginDemo.Entity.*;
import com.example.LoginDemo.Repository.*;
//import com.example.LoginDemo.Repository.VerificationTokenRepository;
import com.example.LoginDemo.Services.ComplaintServices;
import com.example.LoginDemo.Services.PropertyInfoService;
import com.example.LoginDemo.Services.PropertyServices;
import com.example.LoginDemo.Services.UserServices;
import org.springframework.http.ContentDisposition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
//import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import java.io.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    private UserServices userServices;

    @Autowired
    FeedbackRepository feedbackRepository;

    @Autowired
    private PropertyCatRepository propertyCatRepository;

    @Autowired
    private ComplaintServices complaintServices;

    @Autowired
    private PropertyServices propertyServices;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyInfoService propertyInfoService;

    @Autowired
    private PropertyInfoRepository propertyInfoRepository;

    @Autowired
    private PropertyRepository propertyRepository;

//admin home page
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public String adminDashboard(Model model,@RequestParam(defaultValue = "0") int page)
    {

        List<UserEntity> users = userServices.getAllUser();
        List<PropertyInfo> properties = propertyInfoService.getAllProperties();
        List<ComplaintEntity> complaints= complaintServices.getAllComplaints();
        List<PropertyCat> categories = propertyCatRepository.findAll();
        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, 10));
        Page<PropertyInfo> propertyPage = propertyInfoRepository.findAll(PageRequest.of(page, 10));
        model.addAttribute("categories", categories);
        model.addAttribute("users",usersPage);
        model.addAttribute("properties",propertyPage);
        model.addAttribute("complaints",complaints);
        model.addAttribute("reportGenerated", false);
        return "admin";


    }
    //category management
    @PostMapping("/add-category")
    public String addCategory(@RequestParam String name) {
        PropertyCat category = new PropertyCat();
        category.setName(name);
        propertyCatRepository.save(category);
        return "redirect:/admin/dashboard?success";
    }

    @PostMapping("/delete-category/{id}")
    public String deleteCategory(@PathVariable Long id) {
        propertyCatRepository.deleteById(id);
        return "redirect:/admin/dashboard?deleted";
    }
//user management
    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id) {
//        verificationTokenRepository.deleteByUserId(id);

     //   propertyServices.deleteById();
        userServices.deleteUserById(id);
        return "redirect:/admin/dashboard";
    }

//for complaint management
    @GetMapping("/complaints")
    public String showAdminComplaints(Model model) {
        List<ComplaintEntity> complaints = complaintServices.getAllComplaints();
        model.addAttribute("complaints", complaints);
        return "admin-complaints";
    }

    @PostMapping("/complaints/resolve")
    public String resolveComplaint(@RequestParam("complaintId") Long complaintId,
                                   @RequestParam("adminResponse") String adminResponse) {
        complaintServices.resolveComplaint(complaintId, adminResponse);
        return "redirect:/admin/complaints";
    }


    // Change role
    @PostMapping("/change-role")
    public String changeRole(@RequestParam("userId") Long userId,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {
        userServices.changeUserRole(userId, "ADMIN");
        // Fetch paginated data again
        List<PropertyInfo> properties = propertyInfoService.getAllProperties();
        List<ComplaintEntity> complaints = complaintServices.getAllComplaints();
        List<PropertyCat> categories = propertyCatRepository.findAll();
        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, 10));
        Page<PropertyInfo> propertyPage = propertyInfoRepository.findAll(PageRequest.of(page, 10));
        model.addAttribute("message", "User role changed to ADMIN!");
        model.addAttribute("categories", categories);
        model.addAttribute("users", usersPage);
        model.addAttribute("properties", propertyPage);
        model.addAttribute("complaints", complaints);
        return "admin"; // Return to the same admin dashboard template
    }



    @GetMapping("/report")
    public String showReportForm(Model model) {
        // Initial load, no data yet
        model.addAttribute("reportGenerated", false);
        return "admin"; // Reuse admin dashboard with report section
    }

    @PostMapping("/report")
    public String generateReport(@RequestParam("month") int month,
                                 @RequestParam("year") int year,
                                 @RequestParam(defaultValue = "0") int page,
                                 Model model) {


        Date reportDate = java.sql.Date.valueOf(LocalDate.of(year, month, 1));


        List<UserEntity> users = userServices.getAllUser();
        List<PropertyInfo> properties = propertyInfoService.getAllProperties();
        List<ComplaintEntity> complaints = complaintServices.getAllComplaints();
        List<PropertyCat> categories = propertyCatRepository.findAll();
        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, 10));
        Page<PropertyInfo> propertyPage = propertyInfoRepository.findAll(PageRequest.of(page, 10));

        // Fetch report data, default to empty list if null
        List<PropertyInfo> reportProperties = propertyInfoService.getPropertiesByMonthAndYear(month, year);
        if (reportProperties == null) {
            reportProperties = List.of(); // Empty list instead of null
        }
        model.addAttribute("reportProperties", reportProperties);
        model.addAttribute("reportMonth", month);
        model.addAttribute("reportYear", year);
        model.addAttribute("reportGenerated", true);

        model.addAttribute("categories", categories);
        model.addAttribute("users", usersPage);
        model.addAttribute("properties", propertyPage);
        model.addAttribute("complaints", complaints);

        return "admin";
    }



    @ExceptionHandler(Exception.class)
    public String handleErrors(Exception e, Model model) {
        model.addAttribute("error", "An error occurred: " + e.getMessage());
        return "admin";
    }



    @PostMapping("/report/download-pdf")
    public ResponseEntity<byte[]> downloadReportPdf(
            @RequestParam("month") int month,
            @RequestParam("year") int year) throws IOException {

        List<PropertyInfo> reportProperties = propertyInfoService.getPropertiesByMonthAndYear(month, year);
        byte[] pdfBytes = generatePdfReport(reportProperties, month, year);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename("property-report-" + month + "-" + year + ".pdf")
                        .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    private byte[] generatePdfReport(List<PropertyInfo> properties, int month, int year) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // Initialize first content stream
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                // Set up fonts
                PDType1Font titleFont = PDType1Font.HELVETICA_BOLD;
                PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
                PDType1Font bodyFont = PDType1Font.HELVETICA;

                // Add title and headers (same as before)
                contentStream.beginText();
                contentStream.setFont(titleFont, 16);
                contentStream.newLineAtOffset(100, 750);
                contentStream.showText("Property Management System - Monthly Report");
                contentStream.endText();

                // ... (rest of your header content) ...

                // Add property data
                int yPosition = 650;
                contentStream.setFont(bodyFont, 10);

                for (PropertyInfo property : properties) {
                    if (yPosition < 50) {
                        // Close current content stream before creating new page
                        contentStream.close();

                        // Create new page
                        page = new PDPage();
                        document.addPage(page);

                        // Create new content stream for new page
                        contentStream = new PDPageContentStream(document, page);

                        // Reset position and add headers to new page
                        yPosition = 750;
                        contentStream.beginText();
                        contentStream.setFont(headerFont, 12);
                        contentStream.newLineAtOffset(100, yPosition);
                        contentStream.showText("Title");
                        contentStream.newLineAtOffset(150, 0);
                        contentStream.showText("Price");
                        contentStream.newLineAtOffset(100, 0);
                        contentStream.showText("Location");
                        contentStream.newLineAtOffset(150, 0);
                        contentStream.showText("Owner");
                        contentStream.endText();

                        yPosition -= 20;
                    }

                    // Add property content
                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, yPosition);
                    contentStream.showText(property.getPropTitle() != null ? property.getPropTitle() : "N/A");
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(property.getPrice() != null ? "$" + property.getPrice().toString() : "N/A");
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText(property.getLocation() != null ? property.getLocation() : "N/A");
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(property.getUser() != null ? property.getUser().getUsername() : "N/A");
                    contentStream.endText();

                    yPosition -= 20;
                }
            } finally {
                // Ensure contentStream is always closed
                if (contentStream != null) {
                    contentStream.close();
                }
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

}
