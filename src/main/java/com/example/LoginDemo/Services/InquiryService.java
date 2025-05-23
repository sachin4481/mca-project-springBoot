package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.PropInquiry;
import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.PropInquiryRepository;
import com.example.LoginDemo.Repository.PropertyInfoRepository;
import com.example.LoginDemo.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final PropInquiryRepository propInquiryRepository;

    @Autowired
    private PropertyInfoRepository propertyInfoRepository;

    @Autowired
    private UserRepository userRepository;

    public void createInquiry(Long propertyId, String username) {
        PropertyInfo property = propertyInfoRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!propInquiryRepository.existsActiveInquiryByPropertyAndUser(property, user)) {
            PropInquiry inquiry = new PropInquiry();
            inquiry.setProperty(property);
            inquiry.setUser(user);
            propInquiryRepository.save(inquiry);
        } else {
            throw new IllegalStateException("You have already sent an inquiry for this property.");
        }
    }
    public List<PropInquiry> getInquiriesForOwner(Long ownerId) {
        return propInquiryRepository.findInquiriesByOwnerId(ownerId);
    }

    public void closeInquiry(Long inquiryId) {
        PropInquiry inquiry = propInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));
        inquiry.setStatus("CLOSED");
        propInquiryRepository.save(inquiry);
    }

    // Add to InquiryService

    public List<PropInquiry> getFilteredInquiries(int month, int year, String statusFilter) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        if ("ALL".equals(statusFilter)) {
            return propInquiryRepository.findByInqDateBetween(
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59)
            );
        } else {
            return propInquiryRepository.findByStatusAndInqDateBetween(
                    statusFilter,
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59)
            );
        }
    }


}