package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.PropInquiry;
import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropInquiryRepository extends JpaRepository<PropInquiry, Long> {
    boolean existsByPropertyAndUser(PropertyInfo property, UserEntity user);
    @Query("SELECT i FROM PropInquiry i JOIN i.property p WHERE p.user.id = :ownerId ORDER BY i.inqDate DESC")
    List<PropInquiry> findInquiriesByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT COUNT(i) > 0 FROM PropInquiry i WHERE i.property = :property AND i.user = :user AND i.status = 'ACTIVE'")
    boolean existsActiveInquiryByPropertyAndUser(@Param("property") PropertyInfo property,
                                                 @Param("user") UserEntity user);

}