package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.PropInquiry;
import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.PropInquiryRepository;
import com.example.LoginDemo.Repository.PropertyInfoRepository;
import com.example.LoginDemo.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class PropertyInfoService {

    @Autowired
    private PropertyInfoRepository propertyInfoRepository;
    @Autowired
    private PropInquiryRepository propInquiryRepository;
    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(PropertyServices.class);

    public PropertyInfo getPropertyById(Long id) {
        return propertyInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with ID: " + id));
    }

    public List<PropertyInfo> getAllProperties() {
        return propertyInfoRepository.findAll();
    }

    public PropertyInfo saveProperty(PropertyInfo propertyInfo) {
        return propertyInfoRepository.save(propertyInfo);
    }

    public void updateProperty(Long id, PropertyInfo updatedProperty, MultipartFile[] images, UserEntity currentUser) throws IOException {
        PropertyInfo existingProperty = propertyInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with ID: " + id));

        // Ensure the user updating the property is the owner
        if (!existingProperty.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to update this property");
        }
        if (!Arrays.asList("AVAILABLE", "SOLD").contains(updatedProperty.getStatus())) {
            throw new IllegalArgumentException("Invalid status value");
        }
        // Update property fields
        existingProperty.setPropTitle(updatedProperty.getPropTitle());
        existingProperty.setLocation(updatedProperty.getLocation());
        existingProperty.setPinCode(updatedProperty.getPinCode());
        existingProperty.setArea(updatedProperty.getArea());
        existingProperty.setFacing(updatedProperty.getFacing());
        existingProperty.setPrice(updatedProperty.getPrice());
        existingProperty.setStatus(updatedProperty.getStatus());

        // Handle image updates
        if (images != null && images.length > 0) {
            for (int i = 0; i < images.length; i++) {
                if (!images[i].isEmpty()) {
                    String fileName = saveImage(images[i]); // Implement `saveImage()` method
                    switch (i) {
                        case 0 -> existingProperty.setImg1(fileName);
                        case 1 -> existingProperty.setImg2(fileName);
                        case 2 -> existingProperty.setImg3(fileName);
                        case 3 -> existingProperty.setImg4(fileName);
                        case 4 -> existingProperty.setImg5(fileName);
                    }
                }
            }
        }

        propertyInfoRepository.save(existingProperty);
    }

    private String saveImage(MultipartFile file) throws IOException {
        // Implement logic to save image and return the file path
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get("uploads/" + fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }



    public List<PropertyInfo> getPropertiesByMonthAndYear(int month, int year) {
        logger.debug("Fetching properties for month: {} and year: {}", month, year);
        return propertyInfoRepository.findByMonthAndYear(month, year);
    }

    public List<PropertyInfo> getFilteredProperties(int month, int year, Long categoryId, boolean soldOnly) {
        logger.debug("Fetching properties for month: {}, year: {}, category: {}, soldOnly: {}",
                month, year, categoryId, soldOnly);

        if (categoryId != null && soldOnly) {
            return propertyInfoRepository.findByMonthAndYearAndCategoryAndStatus(month, year, categoryId, "SOLD");
        } else if (categoryId != null) {
            return propertyInfoRepository.findByMonthAndYearAndCategory(month, year, categoryId);
        } else if (soldOnly) {
            return propertyInfoRepository.findByMonthAndYearAndStatus(month, year, "SOLD");
        } else {
            return propertyInfoRepository.findByMonthAndYear(month, year);
        }
    }

}
