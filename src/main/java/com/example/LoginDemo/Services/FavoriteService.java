package com.example.LoginDemo.Services;

import com.example.LoginDemo.Entity.Favorite;
import com.example.LoginDemo.Entity.PropertyEntity;
import com.example.LoginDemo.Entity.PropertyInfo;
import com.example.LoginDemo.Entity.UserEntity;
import com.example.LoginDemo.Repository.FavoriteRepository;
import com.example.LoginDemo.Repository.PropertyInfoRepository;
import com.example.LoginDemo.Repository.PropertyRepository;
import com.example.LoginDemo.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
//
//@Service
//public class FavoriteService {
//
//    @Autowired
//    private FavoriteRepository favoriteRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PropertyInfoRepository propertyInfoRepository;
//
////    public List<Favorite> getUserFavorites(Long userId) {
////        UserEntity user = userRepository.findById(userId)
////                .orElseThrow(() -> new RuntimeException("User not found"));
////        return favoriteRepository.findByUser(user);
////    }
////
////    public void addFavorite(Long userId, Long propertyId) {
////        UserEntity user = userRepository.findById(userId)
////                .orElseThrow(() -> new RuntimeException("User not found"));
////        PropertyEntity property = propertyRepository.findById(propertyId)
////                .orElseThrow(() -> new RuntimeException("Property not found"));
////
////        if (favoriteRepository.findByUserAndProperty(user, property).isEmpty()) {
////            favoriteRepository.save(new Favorite(user, property));
////        }
////    }
////
////    public void removeFavorite(Long userId, Long propertyId) {
////        UserEntity user = userRepository.findById(userId)
////                .orElseThrow(() -> new RuntimeException("User not found"));
////        PropertyEntity property = propertyRepository.findById(propertyId)
////                .orElseThrow(() -> new RuntimeException("Property not found"));
////
////        favoriteRepository.deleteByUserAndProperty(user, property);
////    }
//
//
//    public void addFavorite(Long userId, Long propertyId) {
//        if (favoriteRepository.findByUserIdAndPropertyId(userId, propertyId) == null) {
//            Favorite favorite = new Favorite();
//            UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
//            PropertyInfo property = propertyInfoRepository.findById(propertyId).orElseThrow(() -> new RuntimeException("Property not found"));
//            favorite.setUser(user);
//            favorite.setProperty(property);
//            favoriteRepository.save(favorite);
//            //logger.debug("Added property {} to favorites for user {}", propertyId, userId);
//        }
//    }
//
//    @Transactional
//    public void removeFavorite(Long userId, Long propertyId) {
//        favoriteRepository.deleteByUserIdAndPropertyId(userId, propertyId);
//       // logger.debug("Removed property {} from favorites for user {}", propertyId, userId);
//    }
//
//    public List<PropertyInfo> getUserFavorites(Long userId) {
//        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
//        return favorites.stream().map(Favorite::getProperty).collect(Collectors.toList());
//    }
//
//
//
//}

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyInfoRepository propertyInfoRepository;

    public void addFavorite(Long userId, Long propertyId) {
        if (!favoriteRepository.existsByUserIdAndPropertyId(userId, propertyId)) {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            PropertyInfo property = propertyInfoRepository.findById(propertyId)
                    .orElseThrow(() -> new RuntimeException("Property not found"));

            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setProperty(property);
            favoriteRepository.save(favorite);
        }
    }

    public void removeFavorite(Long userId, Long propertyId) {
        favoriteRepository.deleteByUserIdAndPropertyId(userId, propertyId);
    }

    public List<PropertyInfo> getUserFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(Favorite::getProperty)
                .collect(Collectors.toList());
    }
}