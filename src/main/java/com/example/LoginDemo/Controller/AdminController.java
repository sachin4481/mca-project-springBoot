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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;



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
    public String adminDashboard(Model model, @RequestParam(defaultValue = "0") int page) {

        List<UserEntity> users = userServices.getAllUser();
        List<PropertyInfo> properties = propertyInfoService.getAllProperties();
        List<ComplaintEntity> complaints = complaintServices.getAllComplaints();
        List<PropertyCat> categories = propertyCatRepository.findAll();
        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, 10));
        Page<PropertyInfo> propertyPage = propertyInfoRepository.findAll(PageRequest.of(page, 10));
        model.addAttribute("categories", categories);
        model.addAttribute("users", usersPage);
        model.addAttribute("properties", propertyPage);
        model.addAttribute("complaints", complaints);
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
        model.addAttribute("reportGenerated", false);
        model.addAttribute("categories", propertyCatRepository.findAll());
        return "admin";
    }

    @PostMapping("/report")
    public String generateReport(
            @RequestParam("month") int month,
            @RequestParam("year") int year,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "false") boolean soldOnly,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        List<UserEntity> users = userServices.getAllUser();
        List<PropertyInfo> properties = propertyInfoService.getAllProperties();
        List<ComplaintEntity> complaints = complaintServices.getAllComplaints();
        List<PropertyCat> categories = propertyCatRepository.findAll();
        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, 10));
        Page<PropertyInfo> propertyPage = propertyInfoRepository.findAll(PageRequest.of(page, 10));

        List<PropertyInfo> reportProperties = propertyInfoService.getFilteredProperties(month, year, categoryId, soldOnly);
        if (reportProperties == null) {
            reportProperties = List.of();
        }

        model.addAttribute("reportProperties", reportProperties);
        model.addAttribute("reportMonth", month);
        model.addAttribute("reportYear", year);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("soldOnly", soldOnly);
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
            @RequestParam("year") int year,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "false") boolean soldOnly) throws IOException {

        List<PropertyInfo> reportProperties = propertyInfoService.getFilteredProperties(month, year, categoryId, soldOnly);
        byte[] pdfBytes = generatePdfReport(reportProperties, month, year, categoryId, soldOnly);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "property-report-" + month + "-" + year;
        if (categoryId != null) filename += "-cat" + categoryId;
        if (soldOnly) filename += "-sold";
        filename += ".pdf";

        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename(filename)
                        .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
//    private byte[] generatePdfReport(List<PropertyInfo> properties, int month, int year) throws IOException {
//        try (PDDocument document = new PDDocument()) {
//            PDPage page = new PDPage();
//            document.addPage(page);
//
//            // Initialize first content stream
//            PDPageContentStream contentStream = new PDPageContentStream(document, page);
//
//            try {
//                // Set up fonts
//                PDType1Font titleFont = PDType1Font.HELVETICA_BOLD;
//                PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
//                PDType1Font bodyFont = PDType1Font.HELVETICA;
//
//                // Add title and headers (same as before)
//                contentStream.beginText();
//                contentStream.setFont(titleFont, 16);
//                contentStream.newLineAtOffset(100, 750);
//                contentStream.showText("Property Management System - Monthly Report");
//                contentStream.endText();
//
//                // ... (rest of your header content) ...
//
//                // Add property data
//                int yPosition = 650;
//                contentStream.setFont(bodyFont, 10);
//
//                for (PropertyInfo property : properties) {
//                    if (yPosition < 50) {
//                        // Close current content stream before creating new page
//                        contentStream.close();
//
//                        // Create new page
//                        page = new PDPage();
//                        document.addPage(page);
//
//                        // Create new content stream for new page
//                        contentStream = new PDPageContentStream(document, page);
//
//                        // Reset position and add headers to new page
//                        yPosition = 750;
//                        contentStream.beginText();
//                        contentStream.setFont(headerFont, 12);
//                        contentStream.newLineAtOffset(100, yPosition);
//                        contentStream.showText("Title");
//                        contentStream.newLineAtOffset(150, 0);
//                        contentStream.showText("Price");
//                        contentStream.newLineAtOffset(100, 0);
//                        contentStream.showText("Location");
//                        contentStream.newLineAtOffset(150, 0);
//                        contentStream.showText("Owner");
//                        contentStream.endText();
//
//                        yPosition -= 20;
//                    }
//
//                    // Add property content
//                    contentStream.beginText();
//                    contentStream.newLineAtOffset(100, yPosition);
//                    contentStream.showText(property.getPropTitle() != null ? property.getPropTitle() : "N/A");
//                    contentStream.newLineAtOffset(150, 0);
//                    contentStream.showText(property.getPrice() != null ? "$" + property.getPrice().toString() : "N/A");
//                    contentStream.newLineAtOffset(100, 0);
//                    contentStream.showText(property.getLocation() != null ? property.getLocation() : "N/A");
//                    contentStream.newLineAtOffset(150, 0);
//                    contentStream.showText(property.getUser() != null ? property.getUser().getUsername() : "N/A");
//                    contentStream.endText();
//
//                    yPosition -= 20;
//                }
//            } finally {
//                // Ensure contentStream is always closed
//                if (contentStream != null) {
//                    contentStream.close();
//                }
//            }
//
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            document.save(byteArrayOutputStream);
//            return byteArrayOutputStream.toByteArray();
//        }
//    }


    private byte[] generatePdfReport(List<PropertyInfo> properties, int month, int year, Long categoryId, boolean soldOnly) throws IOException {
        try (PDDocument document = new PDDocument()) {
            float margin = 50;
            float rowHeight = 20;
            float yStart = 650;
            float nextY = yStart;
            int propertyIndex = 0;

            PDPage currentPage = new PDPage();
            document.addPage(currentPage);
            PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

            PDType1Font titleFont = PDType1Font.HELVETICA_BOLD;
            PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
            PDType1Font bodyFont = PDType1Font.HELVETICA;

            contentStream.beginText();
            contentStream.setFont(titleFont, 16);
            contentStream.newLineAtOffset(100, 750);
            String title = "Property Management System - Monthly Report";
            if (categoryId != null || soldOnly) {
                title += " (Filtered)";
            }
            contentStream.showText(title);
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(titleFont, 14);
            contentStream.newLineAtOffset(100, 720);
            String subtitle = "Report for " + Month.of(month).name() + " " + year;
            if (categoryId != null) {
                PropertyCat category = propertyCatRepository.findById(categoryId).orElse(null);
                subtitle += " - Category: " + (category != null ? category.getName() : "Unknown");
            }
            if (soldOnly) {
                subtitle += " - Sold Properties Only";
            }
            contentStream.showText(subtitle);
            contentStream.endText();

            addHeaderContent(contentStream, titleFont, bodyFont, month, year);

            float tableWidth = currentPage.getMediaBox().getWidth() - 2 * margin;
            float colWidth = tableWidth / 6; // 6 columns including status

            while (propertyIndex < properties.size()) {
                if (nextY < 50) {
                    contentStream.close();
                    currentPage = new PDPage();
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                    nextY = 750;
                }

                if (nextY == yStart || nextY == 750) {
                    contentStream.setLineWidth(1f);
                    drawTableHeader(contentStream, margin, nextY, colWidth, headerFont, 12);
                    nextY -= rowHeight;
                    contentStream.moveTo(margin, nextY);
                    contentStream.lineTo(margin + tableWidth, nextY);
                    contentStream.stroke();
                }

                PropertyInfo property = properties.get(propertyIndex);
                drawTableRow(contentStream, margin, nextY, colWidth, tableWidth, rowHeight, bodyFont, property);

                nextY -= rowHeight;
                contentStream.moveTo(margin, nextY);
                contentStream.lineTo(margin + tableWidth, nextY);
                contentStream.stroke();

                propertyIndex++;
            }

            contentStream.close();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private void addHeaderContent(PDPageContentStream contentStream, PDType1Font titleFont,
                                  PDType1Font bodyFont, int month, int year) throws IOException {
        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(100, 700);
        contentStream.showText("Generated on: " + LocalDate.now().toString());
        contentStream.endText();
    }

    private void drawTableHeader(PDPageContentStream contentStream, float margin, float y,
                                 float colWidth, PDType1Font font, int fontSize) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(margin + 5, y + 5);
        contentStream.showText("Title");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(margin + colWidth + 5, y + 5);
        contentStream.showText("Price");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(margin + 2 * colWidth + 5, y + 5);
        contentStream.showText("Location");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(margin + 3 * colWidth + 5, y + 5);
        contentStream.showText("Owner");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(margin + 4 * colWidth + 5, y + 5);
        contentStream.showText("Date");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(margin + 5 * colWidth + 5, y + 5);
        contentStream.showText("Status");
        contentStream.endText();
    }

    private void drawTableRow(PDPageContentStream contentStream, float margin, float y,
                              float colWidth, float tableWidth, float rowHeight,
                              PDType1Font bodyFont, PropertyInfo property) throws IOException {
        contentStream.moveTo(margin, y);
        contentStream.lineTo(margin, y + rowHeight);
        contentStream.moveTo(margin + colWidth, y);
        contentStream.lineTo(margin + colWidth, y + rowHeight);
        contentStream.moveTo(margin + 2 * colWidth, y);
        contentStream.lineTo(margin + 2 * colWidth, y + rowHeight);
        contentStream.moveTo(margin + 3 * colWidth, y);
        contentStream.lineTo(margin + 3 * colWidth, y + rowHeight);
        contentStream.moveTo(margin + 4 * colWidth, y);
        contentStream.lineTo(margin + 4 * colWidth, y + rowHeight);
        contentStream.moveTo(margin + 5 * colWidth, y);
        contentStream.lineTo(margin + 5 * colWidth, y + rowHeight);
        contentStream.moveTo(margin + tableWidth, y);
        contentStream.lineTo(margin + tableWidth, y + rowHeight);
        contentStream.stroke();

        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(margin + 5, y + 5);
        contentStream.showText(property.getPropTitle() != null ? property.getPropTitle() : "N/A");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(margin + colWidth + 5, y + 5);
        contentStream.showText(property.getPrice() != null ? "$" + property.getPrice().toString() : "N/A");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(margin + 2 * colWidth + 5, y + 5);
        contentStream.showText(property.getLocation() != null ? property.getLocation() : "N/A");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(margin + 3 * colWidth + 5, y + 5);
        contentStream.showText(property.getUser() != null ? property.getUser().getUsername() : "N/A");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(margin + 4 * colWidth + 5, y + 5);
        contentStream.showText(property.getListingDate() != null ?
                property.getListingDate().toString() : "N/A");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(margin + 5 * colWidth + 5, y + 5);
        contentStream.showText(property.getStatus() != null ? property.getStatus() : "N/A");
        contentStream.endText();
    }
}
