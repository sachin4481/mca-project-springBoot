package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.PropertyInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class PropertyInfoService {

    @Autowired
    private PropertyInfoRepository propertyInfoRepository;

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


    public void deleteProperty(Long id) {
        propertyInfoRepository.deleteById(id);
    }
}
