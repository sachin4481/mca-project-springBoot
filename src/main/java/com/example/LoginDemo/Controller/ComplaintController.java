package com.example.LoginDemo.Controller;

import com.example.LoginDemo.Entity.ComplaintEntity;
import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Repository.ComplaintRepository;
import com.example.LoginDemo.Repository.PropertyInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ComplaintController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private PropertyInfoRepository propertyInfoRepository;

//    @GetMapping("/my-complaints")
//    public String getUserComplaints(Model model, Principal principal) {
//        String username = principal.getName();
//        List<ComplaintEntity> complaints = complaintRepository.findByUsernameOrderBySubmittedAtDesc(username);
//
//        model.addAttribute("complaints", complaints);
//        return "user-complaints";
//    }

    @GetMapping("/my-complaints")
    public String getUserComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt,desc") String sort,
            Model model, Principal principal) {

        String username = principal.getName();
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortBy = Sort.by(direction, sortParams[0]);

        Pageable pageable = PageRequest.of(page, size, sortBy);
        Page<ComplaintEntity> complaintPage = complaintRepository.findByUsername(username, pageable);

        // Create a list of ComplaintWithProperty objects
        List<ComplaintWithProperty> complaintsWithProperties = complaintPage.getContent().stream()
                .map(complaint -> {
                    String propTitle = "N/A";
                    if (complaint.getPropId() != null) {
                        PropertyInfo property = propertyInfoRepository.findById(complaint.getPropId()).orElse(null);
                        if (property != null) {
                            propTitle = property.getPropTitle();
                        }
                    }
                    return new ComplaintWithProperty(complaint, propTitle);
                })
                .collect(Collectors.toList());

        model.addAttribute("complaints", complaintsWithProperties);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", complaintPage.getTotalPages());
        model.addAttribute("sort", sort);

        return "my-complaints";
    }

    // Inner class to hold complaint and property title
    private static class ComplaintWithProperty {
        private final ComplaintEntity complaint;
        private final String propTitle;

        public ComplaintWithProperty(ComplaintEntity complaint, String propTitle) {
            this.complaint = complaint;
            this.propTitle = propTitle;
        }

        public ComplaintEntity getComplaint() {
            return complaint;
        }

        public String getPropTitle() {
            return propTitle;
        }
    }
}