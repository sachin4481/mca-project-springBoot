package com.example.LoginDemo.Services;


import com.example.LoginDemo.Entity.Inquiry;
import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.InquiryRepository;
import com.example.LoginDemo.Repository.PropertyRepository;
import com.example.LoginDemo.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InquiryService {


    private final InquiryRepository inquiryRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public InquiryService(InquiryRepository inquiryRepository, PropertyRepository propertyRepository, UserRepository userRepository) {
        this.inquiryRepository = inquiryRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    public void createInquiry(Long userId, Long propertyId) {
        Inquiry inquiry = new Inquiry();
        inquiry.setUser(userRepository.findById(userId).orElseThrow());
        inquiry.setProperty(propertyRepository.findById(propertyId).orElseThrow());
        inquiry.setInqDate(LocalDateTime.now());

        inquiryRepository.save(inquiry);
    }
//    public List<Inquiry> getInquiriesForOwner(Long ownerId) {
//        return inquiryRepository.findByProperty_User_Id(ownerId);
//    }

    public List<Inquiry> getInquiriesForProperty(Long propertyId) {
        return inquiryRepository.findByProperty_Id(propertyId);
    }
    public List<Inquiry> getInquiriesForOwner(Long ownerId) {
        return inquiryRepository.findByPropertyOwnerId(ownerId);
    }

    public void deleteInquiry(Long id)
    {
        inquiryRepository.deleteById(id);
    }
}

