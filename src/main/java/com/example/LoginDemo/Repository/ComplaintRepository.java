package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.ComplaintEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<ComplaintEntity,Long> {
    List<ComplaintEntity> findByUsername(String username);
    // Add this paginated method
    Page<ComplaintEntity> findByUsername(String username, Pageable pageable);

    // Keep this if you need it for other cases
    List<ComplaintEntity> findByUsernameOrderBySubmittedAtDesc(String username);
}
