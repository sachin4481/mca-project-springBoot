package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<PropertyEntity,Long> {
    List<PropertyEntity> findByStatus(String status);

    List<PropertyEntity> findByUser(UserEntity user);
    List<PropertyEntity> findByLocationContainingIgnoreCase(String area);

    List<PropertyEntity> findByPriceLessThanEqual(double price);
    List<PropertyEntity> findByPincode(String pincode);

    @Query("SELECT DISTINCT p.location FROM PropertyEntity p")
    List<String> findDistinctLocations();

    @Query("SELECT p FROM PropertyEntity p ORDER BY p.id DESC")
    List<PropertyEntity> findTop4RecentProperties(Pageable pageable);


}
