package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.PropertyDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyDetailsRepository extends JpaRepository<PropertyDetails, Long> {
    Optional<PropertyDetails> findByPropertyInfo_PropId(Long propId);
//    Optional<PropertyDetails> findByPropertyInfoId(Long prop_id);

}
