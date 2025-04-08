package com.example.LoginDemo.Repository;

import com.example.LoginDemo.Entity.Favorite;
import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(UserEntity user);
    Optional<Favorite> findByUserAndProperty(UserEntity user, PropertyInfo property);
    void deleteByUserAndProperty(UserEntity user, PropertyInfo property);

@Query("SELECT f FROM Favorite f JOIN FETCH f.property WHERE f.user.id = :userId")
List<Favorite> findByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM Favorite f WHERE f.user.id = :userId AND f.property.propId = :propId")
    boolean existsByUserIdAndPropertyId(@Param("userId") Long userId,
                                        @Param("propId") Long propId);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId AND f.property.propId = :propId")
    void deleteByUserIdAndPropertyId(@Param("userId") Long userId,
                                     @Param("propId") Long propId);

    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId AND f.property.propId = :propId")
    Optional<Favorite> findByUserIdAndPropertyId(@Param("userId") Long userId,
                                                 @Param("propId") Long propId);


}
