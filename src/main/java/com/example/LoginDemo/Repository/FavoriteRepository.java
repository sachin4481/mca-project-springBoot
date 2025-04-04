package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.Favorite;
import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(UserEntity user);
    Optional<Favorite> findByUserAndProperty(UserEntity user, PropertyEntity property);
    void deleteByUserAndProperty(UserEntity user, PropertyEntity property);
    List<Favorite> findByUserId(Long userId);
    Favorite findByUserIdAndPropertyId(Long userId, Long propertyId);
    void deleteByUserIdAndPropertyId(Long userId, Long propertyId);
}
