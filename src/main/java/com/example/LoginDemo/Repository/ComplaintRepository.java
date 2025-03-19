package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.ComplaintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<ComplaintEntity,Long> {
    List<ComplaintEntity> findByUsername(String username);
}
