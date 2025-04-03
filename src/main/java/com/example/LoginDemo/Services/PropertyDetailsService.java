package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.PropertyDetails;
import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Repository.PropertyDetailsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PropertyDetailsService {

    @Autowired
    private PropertyDetailsRepository propertyDetailsRepository;

    public PropertyDetails getDetailsByPropertyId(Long propertyId) {
        return propertyDetailsRepository.findByPropertyInfo_PropId(propertyId).orElse(null);

    }


    public PropertyDetails savePropertyDetails(PropertyDetails propertyDetails) {
        return propertyDetailsRepository.save(propertyDetails);
    }

    public PropertyDetails updatePropertyDetails(Long id, PropertyDetails updatedDetails) {
        PropertyDetails existingDetails = propertyDetailsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property details not found with ID: " + id));

        existingDetails.setBhk(updatedDetails.getBhk());
        existingDetails.setFloors(updatedDetails.getFloors());
        existingDetails.setCorner(updatedDetails.getCorner());
        existingDetails.setFloor(updatedDetails.getFloor());
        existingDetails.setFurnished(updatedDetails.getFurnished());
        existingDetails.setWashroom(updatedDetails.getWashroom());
        existingDetails.setParking(updatedDetails.getParking());
        existingDetails.setSchemeName(updatedDetails.getSchemeName());
        existingDetails.setPropDetails(updatedDetails.getPropDetails());

        return propertyDetailsRepository.save(existingDetails);
    }
}

