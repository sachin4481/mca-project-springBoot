package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.PropertyDetails;
import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyInfoRepository extends JpaRepository<PropertyInfo, Long> {

    List<PropertyInfo> findByUser(UserEntity user);

    @Query("SELECT p FROM PropertyInfo p ORDER BY p.propId DESC")
    List<PropertyInfo> findTop4RecentProperties(Pageable pageable);

    @Query("SELECT DISTINCT p.location FROM PropertyEntity p")
    List<String> findDistinctLocations();

}
