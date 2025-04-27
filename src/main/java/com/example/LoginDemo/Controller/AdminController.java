package com.example.LoginDemo.Controller;


import com.example.LoginDemo.Entity.*;
import com.example.LoginDemo.Repository.*;
//import com.example.LoginDemo.Repository.VerificationTokenRepository;
import com.example.LoginDemo.Services.ComplaintServices;
import com.example.LoginDemo.Services.PropertyInfoService;
import com.example.LoginDemo.Services.PropertyServices;
import com.example.LoginDemo.Services.UserServices;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
        List<Feedback> feedback=feedbackRepository.findAll();
        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, 10));
        Page<PropertyInfo> propertyPage = propertyInfoRepository.findAll(PageRequest.of(page, 10));
        model.addAttribute("categories", categories);
        model.addAttribute("users", usersPage);
        model.addAttribute("properties", propertyPage);
        model.addAttribute("complaints", complaints);
        model.addAttribute("feedbacks",feedback);
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



    // Report generation form
    @GetMapping("/report")
    public String showReportForm(Model model) {
        model.addAttribute("reportGenerated", false);
        model.addAttribute("categories", propertyCatRepository.findAll());
        return "report"; // Return to the report template
    }

    // Generate report
    @PostMapping("/report")
    public String generateReport(
            @RequestParam("month") int month,
            @RequestParam("year") int year,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "false") boolean soldOnly,
            Model model) {

        List<PropertyInfo> reportProperties = propertyInfoService.getFilteredProperties(month, year, categoryId, soldOnly);
        model.addAttribute("reportProperties", reportProperties);
        model.addAttribute("reportMonth", month);
        model.addAttribute("reportYear", year);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("soldOnly", soldOnly);
        model.addAttribute("reportGenerated", true);
        model.addAttribute("categories", propertyCatRepository.findAll());

        return "report"; // Return to the report template with generated data
    }

    // Download PDF report
    @PostMapping("/report/download-pdf")
    public ResponseEntity<byte[]> downloadReportPdf(
            @RequestParam("month") int month,
            @RequestParam("year") int year,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false , defaultValue = "false") boolean soldOnly) throws IOException {

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

    // Exception handling
    @ExceptionHandler(Exception.class)
    public String handleErrors(Exception e, Model model) {
        model.addAttribute("error", "An error occurred: " + e.getMessage());
        return "report"; // Redirect to report page with error message
    }
private byte[] generatePdfReport(List<PropertyInfo> properties, int month, int year, Long categoryId, boolean soldOnly) throws IOException {
    try (PDDocument document = new PDDocument()) {
        float margin = 50;
        float rowHeight = 25;
        float yStart = 700;
        float nextY = yStart;
        int propertyIndex = 0;

        PDPage currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);
        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

        // Use better fonts (if available)
        PDType1Font titleFont = PDType1Font.HELVETICA_BOLD;
        PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
        PDType1Font bodyFont = PDType1Font.HELVETICA;

        // Add report title
        contentStream.beginText();
        contentStream.setFont(titleFont, 16);
        contentStream.newLineAtOffset(margin, nextY);
        String title = "PropertyNest - Monthly Report";
        if (categoryId != null || soldOnly) {
            title += " (Filtered)";
        }
        contentStream.showText(title);
        contentStream.endText();
        nextY -= 30;

        // Add subtitle
        contentStream.beginText();
        contentStream.setFont(titleFont, 14);
        contentStream.newLineAtOffset(margin, nextY);
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
        nextY -= 25;

        // Add generation date
        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(margin, nextY);
        contentStream.showText("Generated on: " + LocalDate.now().toString());
        contentStream.endText();
        nextY -= 30;

        float tableWidth = currentPage.getMediaBox().getWidth() - 2 * margin;
        float[] colWidths = {
                tableWidth * 0.25f, // Title (25%)
                tableWidth * 0.15f, // Price (15%)
                tableWidth * 0.20f, // Location (20%)
                tableWidth * 0.15f, // Owner (15%)
                tableWidth * 0.15f, // Date (15%)
                tableWidth * 0.10f  // Status (10%)
        };

        while (propertyIndex < properties.size()) {
            if (nextY < 100) { // Increased bottom margin
                contentStream.close();
                currentPage = new PDPage(PDRectangle.A4);
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                nextY = 750;

                // Add header on new page
                contentStream.beginText();
                contentStream.setFont(titleFont, 16);
                contentStream.newLineAtOffset(margin, nextY);
                contentStream.showText(title + " (continued)");
                contentStream.endText();
                nextY -= 30;
            }

            if (propertyIndex == 0 || nextY == 750) {
                // Draw table header
                drawTableHeader(contentStream, margin, nextY, colWidths, headerFont, 12);
                nextY -= rowHeight;
                contentStream.setLineWidth(1f);
                contentStream.moveTo(margin, nextY);
                contentStream.lineTo(margin + tableWidth, nextY);
                contentStream.stroke();
            }

            PropertyInfo property = properties.get(propertyIndex);
            drawTableRow(contentStream, margin, nextY, colWidths, rowHeight, bodyFont, property);

            nextY -= rowHeight;
            contentStream.setLineWidth(0.5f);
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
                             float[] colWidths, PDType1Font font, int fontSize) throws IOException {
    // Draw header background
    contentStream.setNonStrokingColor(200, 200, 200); // Light gray
    contentStream.addRect(margin, y - 20,
            colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] + colWidths[4] + colWidths[5],
            20);
    contentStream.fill();
    contentStream.setNonStrokingColor(0, 0, 0); // Reset to black

    // Draw column headers
    float currentX = margin;

    String[] headers = {"Title", "Price", "Location", "Owner", "Date", "Status"};
    for (int i = 0; i < headers.length; i++) {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(currentX + 5, y - 15);
        contentStream.showText(headers[i]);
        contentStream.endText();

        // Draw vertical line
        contentStream.moveTo(currentX, y);
        contentStream.lineTo(currentX, y - 20);
        contentStream.stroke();

        currentX += colWidths[i];
    }

    // Draw right border
    contentStream.moveTo(currentX, y);
    contentStream.lineTo(currentX, y - 20);
    contentStream.stroke();
}

private void drawTableRow(PDPageContentStream contentStream, float margin, float y,
                          float[] colWidths, float rowHeight, PDType1Font bodyFont,
                          PropertyInfo property) throws IOException {

    float currentX = margin;

    // Reset to start position
    currentX = margin;

    // Title
    String title = property.getPropTitle() != null ? property.getPropTitle() : "N/A";
    drawWrappedText(contentStream, title, currentX + 5, y - (rowHeight / 2) + 3  , colWidths[0] - 10, bodyFont, 8);
    currentX += colWidths[0];

    // Price (increased width)
    String price = property.getPrice() != null ?
            String.format("Rs.%,.2f", property.getPrice()) : "N/A";
    contentStream.beginText();
    contentStream.setFont(bodyFont, 10);
    contentStream.newLineAtOffset(currentX + 5, y - 15);
    contentStream.showText(price);
    contentStream.endText();
    currentX += colWidths[2];

    // Location (decreased width)
    String location = property.getLocation() != null ? property.getLocation() : "N/A";
    drawWrappedText(contentStream, location, currentX + 5, y - 15, colWidths[2] - 10, bodyFont, 10);
    currentX += colWidths[1];

    // Owner
    String owner = property.getUser() != null ? property.getUser().getUsername() : "N/A";
    contentStream.beginText();
    contentStream.setFont(bodyFont, 10);
    contentStream.newLineAtOffset(currentX + 5, y - 15);
    contentStream.showText(owner);
    contentStream.endText();
    currentX += colWidths[3];

    // Date
    String date = formatDate(property.getListingDate());
    contentStream.beginText();
    contentStream.setFont(bodyFont, 10);
    contentStream.newLineAtOffset(currentX + 5, y - 15);
    contentStream.showText(date);
    contentStream.endText();
    currentX += colWidths[4];

    // Status (increased width)
    String status = property.getStatus() != null ?
            (property.getStatus().equals("AVAILABLE") ? "AVAILABLE" : property.getStatus())
            : "N/A";
    contentStream.beginText();
    contentStream.setFont(bodyFont, 10);
    contentStream.newLineAtOffset(currentX + 5, y - 15);
    contentStream.showText(status);
    contentStream.endText();
}


    private String formatDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            return sdf.format(date);
        } catch (Exception e) {
            return "N/A";
        }
    }
    private void drawWrappedText(PDPageContentStream contentStream, String text, float x, float y,
                                 float maxWidth, PDType1Font font, int fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            float width = font.getStringWidth(testLine) * fontSize / 1000f;

            if (width < maxWidth) {
                currentLine.append(currentLine.length() > 0 ? " " + word : word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        lines.add(currentLine.toString());

        for (int i = 0; i < lines.size(); i++) {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, y - (i * (fontSize + 2)));
            contentStream.showText(lines.get(i));
            contentStream.endText();
        }
    }
}
