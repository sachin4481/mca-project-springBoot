package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.ComplaintEntity;
import com.example.LoginDemo.Repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintServices {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Transactional
    public ComplaintEntity submitComplaint(ComplaintEntity complaint) {
        return complaintRepository.save(complaint);
    }

    public List<ComplaintEntity> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public List<ComplaintEntity> getUserComplaints(String username) {
        return complaintRepository.findByUsername(username);
    }

    @Transactional
    public ComplaintEntity resolveComplaint(Long complaintId, String adminResponse) {
        ComplaintEntity complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        complaint.setStatus("Resolved");
        complaint.setAdminResponse(adminResponse);
        complaint.setResolvedAt(LocalDateTime.now());
        return complaintRepository.save(complaint);
    }
}