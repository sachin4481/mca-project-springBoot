package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.PropertyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropertyServices {

    private static final Logger logger = LoggerFactory.getLogger(PropertyServices.class);

        @Autowired
        private PropertyRepository propertyRepository;

    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;






    public PropertyEntity listProperty(PropertyEntity property,  MultipartFile[] images) throws IOException {

        if (property.getStatus() == null) {
            property.setStatus("AVAILABLE");
        }

        // Handle image uploads (store in a folder and save paths)
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
                images[i].transferTo(dest); // Write the file to the destination
                switch (i) {
                    case 0: property.setImage1("/uploads/" + fileName); break;
                    case 1: property.setImage2("/uploads/" + fileName); break;
                    case 2: property.setImage3("/uploads/" + fileName); break;

                }
            }
        }

        return propertyRepository.save(property);
    }

    public PropertyEntity updateProperty(Long id, PropertyEntity updatedProperty, MultipartFile[] images, UserEntity currentUser) throws IOException {
        PropertyEntity existingProperty = getPropertyById(id);


// Check if the current user is the one who listed the property
        if (!existingProperty.getUser().getId().equals(currentUser.getId())) {
            logger.warn("User {} attempted to edit property {} but is not authorized", currentUser.getId(), id);
            throw new SecurityException("You are not authorized to update this property.");
        }

        logger.info("Updating property ID: {}", id);

        // Update fields
        existingProperty.setTitle(updatedProperty.getTitle());
        existingProperty.setDescription(updatedProperty.getDescription());
        existingProperty.setLocation(updatedProperty.getLocation());
        existingProperty.setPrice(updatedProperty.getPrice());
        existingProperty.setPincode(updatedProperty.getPincode());


        existingProperty.setArea(updatedProperty.getArea());

        // Handle image updates if new images are uploaded
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        for (int i = 0; i < images.length && i < 5; i++) {
            if (!images[i].isEmpty()) {
                String fileName = System.currentTimeMillis() + "-" + images[i].getOriginalFilename();
                File dest = new File(uploadDirectory.getAbsolutePath() + File.separator + fileName);
                images[i].transferTo(dest);
                switch (i) {case 0: existingProperty.setImage1("/uploads/" + fileName); break;
                    case 1: existingProperty.setImage2("/uploads/" + fileName); break;
                    case 2: existingProperty.setImage3("/uploads/" + fileName); break;

                }
            }
        }

        return propertyRepository.save(existingProperty);
    }






        public PropertyEntity listProperty(PropertyEntity property)
        {
            property.setStatus("AVAILABLE");
            return propertyRepository.save(property);
        }

        public List<PropertyEntity> getAllProperty()
        {
            return propertyRepository.findAll();
        }

        public PropertyEntity getPropertyById(Long id)
        {
            return propertyRepository.findById(id).orElseThrow(
                    ()->new RuntimeException("Property Not Found"));
        }

        public void deleteById(Long id)
        {
            propertyRepository.deleteById(id);
        }



    public List<PropertyEntity> getPropertiesByUser(UserEntity user) {
        return propertyRepository.findByUser(user);
    }

    //find by Area
//    public List<PropertyEntity> searchPropertiesByArea(String location) {
//        if (location == null || location.trim().isEmpty()) {
//            return propertyRepository.findAll(); // Return all if no search term
//        }
//        return propertyRepository.findByLocationContainingIgnoreCase(location);
//    }

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


}
