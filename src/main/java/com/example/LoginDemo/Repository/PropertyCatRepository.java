package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.PropertyCat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyCatRepository extends JpaRepository<PropertyCat, Long> {
    Optional<PropertyCat> findByName(String name);

}