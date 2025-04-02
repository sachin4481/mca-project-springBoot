package com.example.LoginDemo.Repository;




import com.example.LoginDemo.Entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByProperty_User_Id(Long ownerId);
    List<Inquiry> findByProperty_Id(Long propertyId);
    @Query("SELECT i FROM Inquiry i JOIN i.property p WHERE p.user.id = :ownerId")
    List<Inquiry> findByPropertyOwnerId(@Param("ownerId") Long ownerId);
}
