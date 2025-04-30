package com.example.LoginDemo.Controller;


import com.example.LoginDemo.Entity.*;
import com.example.LoginDemo.Repository.*;
//import com.example.LoginDemo.Repository.VerificationTokenRepository;
import com.example.LoginDemo.Services.*;
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

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private  PropInquiryRepository propInquiryRepository;

    //admin home page
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public String adminDashboard(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "overview") String section) {
        // Populate model attributes
        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, 10));
        Page<PropertyInfo> propertyPage = propertyInfoRepository.findAll(PageRequest.of(page, 10));
        List<ComplaintEntity> complaints = complaintServices.getAllComplaints();
        List<PropertyCat> categories = propertyCatRepository.findAll();
        List<Feedback> feedbacks = feedbackRepository.findAll();
        List<PropInquiry> inquiries = propInquiryRepository.findAll(); // Fetch all inquiries

        model.addAttribute("users", usersPage);
        model.addAttribute("properties", propertyPage);
        model.addAttribute("complaints", complaints);
        model.addAttribute("categories", categories);
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("inquiries", inquiries);
        model.addAttribute("reportGenerated", false);
        model.addAttribute("section", section);

        return "admin";


    }
    // Close inquiry endpoint
    @PostMapping("/close-inquiry")
    @PreAuthorize("hasRole('ADMIN')")
    public String closeInquiry(@RequestParam("inquiryId") Long inquiryId,
                               @RequestParam(defaultValue = "inquiries") String section) {
        inquiryService.closeInquiry(inquiryId);
        return "redirect:/admin/dashboard?section=" + section;
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
        return "redirect:/admin/dashboard";
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


    //change role to the user
    @PostMapping("/change-role-to-user")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeToUserRole(@RequestParam("userId") Long userId,
                                   @RequestParam(defaultValue = "0") int page,
                                   Model model) {
        try {
            userServices.changeUserRole(userId, "USER");
            model.addAttribute("message", "User role changed to USER!");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, 10));
        Page<PropertyInfo> propertyPage = propertyInfoRepository.findAll(PageRequest.of(page, 10));
        model.addAttribute("categories", propertyCatRepository.findAll());
        model.addAttribute("users", usersPage);
        model.addAttribute("properties", propertyPage);
        model.addAttribute("complaints", complaintServices.getAllComplaints());
        model.addAttribute("reportGenerated", false);
        model.addAttribute("section", "users");
        return "admin";
    }











    @GetMapping("/report")
    public String showReportForm(Model model) {
        model.addAttribute("reportGenerated", false);
        model.addAttribute("categories", propertyCatRepository.findAll());
        model.addAttribute("selectedCategoryId", null);
        model.addAttribute("reportProperties", Collections.emptyList());
        model.addAttribute("reportMonth", null);
        model.addAttribute("reportYear", null);
        model.addAttribute("soldOnly", false);
        return "report";
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

        List<PropertyCat> categories = propertyCatRepository.findAll();
        model.addAttribute("categories", categories);

        // ✅ Add selected category name
        if (categoryId != null) {
            categories.stream()
                    .filter(cat -> cat.getId().equals(categoryId))
                    .findFirst()
                    .ifPresent(cat -> model.addAttribute("selectedCategoryName", cat.getName()));
        }

        return "report"; // Return to the report template
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





    // inquiry report generation

    @GetMapping("/inquiry-report")
    public String showInquiryReportForm(Model model) {
        model.addAttribute("reportGenerated", false);
        model.addAttribute("reportInquiries", Collections.emptyList());
        model.addAttribute("reportMonth", null);
        model.addAttribute("reportYear", null);
        model.addAttribute("statusFilter", "ALL");
        return "inquiry-report";
    }

    @PostMapping("/inquiry-report")
    public String generateInquiryReport(
            @RequestParam("month") int month,
            @RequestParam("year") int year,
            @RequestParam(required = false, defaultValue = "ALL") String statusFilter,
            Model model) {

        List<PropInquiry> reportInquiries = inquiryService.getFilteredInquiries(month, year, statusFilter);
        model.addAttribute("reportInquiries", reportInquiries);
        model.addAttribute("reportMonth", month);
        model.addAttribute("reportYear", year);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("reportGenerated", true);

        return "inquiry-report";
    }

    @PostMapping("/inquiry-report/download-pdf")
    public ResponseEntity<byte[]> downloadInquiryReportPdf(
            @RequestParam("month") int month,
            @RequestParam("year") int year,
            @RequestParam(required = false, defaultValue = "ALL") String statusFilter) throws IOException {

        List<PropInquiry> reportInquiries = inquiryService.getFilteredInquiries(month, year, statusFilter);
        byte[] pdfBytes = generateInquiryPdfReport(reportInquiries, month, year, statusFilter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "inquiry-report-" + month + "-" + year;
        if (!"ALL".equals(statusFilter)) filename += "-" + statusFilter.toLowerCase();
        filename += ".pdf";

        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename(filename)
                        .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    private byte[] generateInquiryPdfReport(List<PropInquiry> inquiries, int month, int year, String statusFilter) throws IOException {
        try (PDDocument document = new PDDocument()) {
            float margin = 50;
            float rowHeight = 25;
            float yStart = 700;
            float nextY = yStart;
            int inquiryIndex = 0;

            PDPage currentPage = new PDPage(PDRectangle.A4);
            document.addPage(currentPage);
            PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

            // Fonts
            PDType1Font titleFont = PDType1Font.HELVETICA_BOLD;
            PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
            PDType1Font bodyFont = PDType1Font.HELVETICA;

            // Add report title
            contentStream.beginText();
            contentStream.setFont(titleFont, 16);
            contentStream.newLineAtOffset(margin, nextY);
            String title = "PropertyNest - Inquiry Report";
            contentStream.showText(title);
            contentStream.endText();
            nextY -= 30;

            // Add subtitle
            contentStream.beginText();
            contentStream.setFont(titleFont, 14);
            contentStream.newLineAtOffset(margin, nextY);
            String subtitle = "Report for " + Month.of(month).name() + " " + year;
            if (!"ALL".equals(statusFilter)) {
                subtitle += " - Status: " + statusFilter;
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
                    tableWidth * 0.25f, // Property (25%)
                    tableWidth * 0.20f, // User (20%)
                    tableWidth * 0.20f, // Owner (20%)
                    tableWidth * 0.15f, // Date (15%)
                    tableWidth * 0.10f, // Status (10%)
                    tableWidth * 0.10f  // Action (10%)
            };

            while (inquiryIndex < inquiries.size()) {
                if (nextY < 100) {
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

                if (inquiryIndex == 0 || nextY == 750) {
                    // Draw table header
                    drawInquiryTableHeader(contentStream, margin, nextY, colWidths, headerFont, 12);
                    nextY -= rowHeight;
                    contentStream.setLineWidth(1f);
                    contentStream.moveTo(margin, nextY);
                    contentStream.lineTo(margin + tableWidth, nextY);
                    contentStream.stroke();
                }

                PropInquiry inquiry = inquiries.get(inquiryIndex);
                drawInquiryTableRow(contentStream, margin, nextY, colWidths, rowHeight, bodyFont, inquiry);

                nextY -= rowHeight;
                contentStream.setLineWidth(0.5f);
                contentStream.moveTo(margin, nextY);
                contentStream.lineTo(margin + tableWidth, nextY);
                contentStream.stroke();

                inquiryIndex++;
            }

            contentStream.close();

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private void drawInquiryTableHeader(PDPageContentStream contentStream, float margin, float y,
                                        float[] colWidths, PDType1Font font, int fontSize) throws IOException {
        contentStream.setNonStrokingColor(200, 200, 200);
        contentStream.addRect(margin, y - 20,
                colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] + colWidths[4] + colWidths[5],
                20);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0);

        float currentX = margin;
        String[] headers = {"Property", "Inquirer", "Owner", "Date", "Status", "Action"};

        for (int i = 0; i < headers.length; i++) {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(currentX + 5, y - 15);
            contentStream.showText(headers[i]);
            contentStream.endText();

            contentStream.moveTo(currentX, y);
            contentStream.lineTo(currentX, y - 20);
            contentStream.stroke();

            currentX += colWidths[i];
        }

        contentStream.moveTo(currentX, y);
        contentStream.lineTo(currentX, y - 20);
        contentStream.stroke();
    }

    private void drawInquiryTableRow(PDPageContentStream contentStream, float margin, float y,
                                     float[] colWidths, float rowHeight, PDType1Font bodyFont,
                                     PropInquiry inquiry) throws IOException {
        float currentX = margin;

        // Property Title
        String propertyTitle = inquiry.getProperty().getPropTitle() != null ?
                inquiry.getProperty().getPropTitle() : "N/A";
        drawWrappedText(contentStream, propertyTitle, currentX + 5, y - 15, colWidths[0] - 10, bodyFont, 10);
        currentX += colWidths[0];

        // Inquirer
        String inquirer = inquiry.getUser().getUsername() != null ?
                inquiry.getUser().getUsername() : "N/A";
        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(currentX + 5, y - 15);
        contentStream.showText(inquirer);
        contentStream.endText();
        currentX += colWidths[1];

        // Owner
        String owner = inquiry.getProperty().getUser().getUsername() != null ?
                inquiry.getProperty().getUser().getUsername() : "N/A";
        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(currentX + 5, y - 15);
        contentStream.showText(owner);
        contentStream.endText();
        currentX += colWidths[2];

        // Date
        String date = inquiry.getInqDate() != null ?
                inquiry.getInqDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "N/A";
        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(currentX + 5, y - 15);
        contentStream.showText(date);
        contentStream.endText();
        currentX += colWidths[3];

        // Status
        String status = inquiry.getStatus() != null ? inquiry.getStatus() : "N/A";
        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(currentX + 5, y - 15);
        contentStream.showText(status);
        contentStream.endText();
        currentX += colWidths[4];

        // Action (just for display)
        String action = inquiry.getStatus().equals("ACTIVE") ? "Pending" : "Closed";
        contentStream.beginText();
        contentStream.setFont(bodyFont, 10);
        contentStream.newLineAtOffset(currentX + 5, y - 15);
        contentStream.showText(action);
        contentStream.endText();
    }








}
