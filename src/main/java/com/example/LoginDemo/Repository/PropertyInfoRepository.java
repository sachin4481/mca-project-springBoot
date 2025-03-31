package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.PropertyDetails;
import com.example.LoginDemo.Entity.PropertyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyInfoRepository extends JpaRepository<PropertyInfo, Long> {
//    List<PropertyDetails> findByPropertyInfo(PropertyInfo propertyInfo);
}
