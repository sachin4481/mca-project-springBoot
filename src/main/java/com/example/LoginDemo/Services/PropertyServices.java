package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.PropertyCat;
import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropertyServices {

    private static final Logger logger = LoggerFactory.getLogger(PropertyServices.class);

    @Autowired
    private PropertyRepository propertyRepository;

    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;


    @Autowired
    private PropertyCatRepository propertyCatRepository;


    @Autowired
    private PropertyInfoRepository propertyInfoRepository;

    @Autowired
    private UserRepository userRepository;


    public PropertyEntity listProperty(PropertyEntity property, MultipartFile[] images) throws IOException {

        if (property.getStatus() == null) {
            property.setStatus("AVAILABLE");
        }

        String uploadDir = "src/main/resources/static/uploads/";
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            boolean created = uploadDirectory.mkdirs();
            if (!created) {
                throw new IOException("Failed to create upload directory: " + uploadDir);
            }
        }

        for (int i = 0; i < images.length && i < 5; i++) {
            if (!images[i].isEmpty()) {
                String fileName = System.currentTimeMillis() + "-" + images[i].getOriginalFilename();
                File dest = new File(uploadDirectory.getAbsolutePath() + File.separator + fileName);
                images[i].transferTo(dest);
                switch (i) {
                    case 0:
                        property.setImage1("/uploads/" + fileName);
                        break;
                    case 1:
                        property.setImage2("/uploads/" + fileName);
                        break;
                    case 2:
                        property.setImage3("/uploads/" + fileName);
                        break;

                }
            }
        }

        return propertyRepository.save(property);
    }

    public PropertyEntity updateProperty(Long id, PropertyEntity updatedProperty, MultipartFile[] images, UserEntity currentUser) throws IOException {
        PropertyEntity existingProperty = getPropertyById(id);


        if (!existingProperty.getUser().getId().equals(currentUser.getId())) {
            logger.warn("User {} attempted to edit property {} but is not authorized", currentUser.getId(), id);
            throw new SecurityException("You are not authorized to update this property.");
        }

        logger.info("Updating property ID: {}", id);

        existingProperty.setTitle(updatedProperty.getTitle());
        existingProperty.setDescription(updatedProperty.getDescription());
        existingProperty.setLocation(updatedProperty.getLocation());
        existingProperty.setPrice(updatedProperty.getPrice());
        existingProperty.setPincode(updatedProperty.getPincode());


        existingProperty.setArea(updatedProperty.getArea());

        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        for (int i = 0; i < images.length && i < 5; i++) {
            if (!images[i].isEmpty()) {
                String fileName = System.currentTimeMillis() + "-" + images[i].getOriginalFilename();
                File dest = new File(uploadDirectory.getAbsolutePath() + File.separator + fileName);
                images[i].transferTo(dest);
                switch (i) {
                    case 0:
                        existingProperty.setImage1("/uploads/" + fileName);
                        break;
                    case 1:
                        existingProperty.setImage2("/uploads/" + fileName);
                        break;
                    case 2:
                        existingProperty.setImage3("/uploads/" + fileName);
                        break;

                }
            }
        }

        return propertyRepository.save(existingProperty);
    }


    public PropertyEntity listProperty(PropertyEntity property) {
        property.setStatus("AVAILABLE");
        return propertyRepository.save(property);
    }

    public List<PropertyEntity> getAllProperty() {
        return propertyRepository.findAll();
    }

    public PropertyEntity getPropertyById(Long id) {
        return propertyRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Property Not Found"));
    }

    public void deleteById(Long id) {
        propertyRepository.deleteById(id);
    }


    public List<PropertyEntity> getPropertiesByUser(UserEntity user) {
        return propertyRepository.findByUser(user);
    }


    public List<PropertyEntity> searchProperties(String area, Double price, String location) {
        List<PropertyEntity> properties = propertyRepository.findAll();

        if (area != null && !area.trim().isEmpty()) {
            int areaValue = Integer.parseInt(area);
            properties = properties.stream()
                    .filter(p -> p.getArea() != null && p.getArea() <= areaValue)
                    .collect(Collectors.toList());
        }
        if (price != null) {
            properties = properties.stream()
                    .filter(p -> p.getPrice() <= price)
                    .collect(Collectors.toList());
        }

        if (location != null && !location.trim().isEmpty()) {
            properties = properties.stream()
                    .filter(p -> p.getLocation().toLowerCase().contains(location.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return properties;
    }

    public List<PropertyInfo> getRecentListings(Pageable pageable) {
        return propertyInfoRepository.findTop4RecentProperties(pageable);
    }


}






