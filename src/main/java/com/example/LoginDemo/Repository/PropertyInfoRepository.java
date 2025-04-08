package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.PropertyDetails;
import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyInfoRepository extends JpaRepository<PropertyInfo, Long> {

    List<PropertyInfo> findByUser(UserEntity user);

    @Query("SELECT p FROM PropertyInfo p WHERE p.status <> 'SOLD' ORDER BY p.propId DESC")
    List<PropertyInfo> findTop4RecentProperties(Pageable pageable);


    @Query("SELECT DISTINCT p.location FROM PropertyInfo p")
    List<String> findDistinctLocations();


    @Query("SELECT p FROM PropertyInfo p WHERE YEAR(p.listingDate) = :year AND MONTH(p.listingDate) = :month")
    List<PropertyInfo> findByMonthAndYear(@Param("month") int month, @Param("year") int year);


    @Query("SELECT pi FROM PropertyInfo pi " +
            "JOIN PropInquiry inq ON inq.property = pi " +
            "WHERE inq.user.id = :userId AND inq.status = 'ACTIVE'")
    List<PropertyInfo> findInquiredPropertiesByUserId(@Param("userId") Long userId);




    @Query("SELECT p FROM PropertyInfo p WHERE MONTH(p.listingDate) = :month AND YEAR(p.listingDate) = :year AND p.propertyCategory.id = :categoryId")
    List<PropertyInfo> findByMonthAndYearAndCategory(@Param("month") int month, @Param("year") int year, @Param("categoryId") Long categoryId);

    @Query("SELECT p FROM PropertyInfo p WHERE MONTH(p.listingDate) = :month AND YEAR(p.listingDate) = :year AND p.status = :status")
    List<PropertyInfo> findByMonthAndYearAndStatus(@Param("month") int month, @Param("year") int year, @Param("status") String status);

    @Query("SELECT p FROM PropertyInfo p WHERE MONTH(p.listingDate) = :month AND YEAR(p.listingDate) = :year AND p.propertyCategory.id = :categoryId AND p.status = :status")
    List<PropertyInfo> findByMonthAndYearAndCategoryAndStatus(@Param("month") int month, @Param("year") int year,
                                                              @Param("categoryId") Long categoryId, @Param("status") String status);
}



