package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.PropertyDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyDetailsRepository extends JpaRepository<PropertyDetails, Long> {
}