package com.example.demo.name;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserFavorite> findByUserIdAndNameId(Long userId, Long nameId);

    boolean existsByUserIdAndNameId(Long userId, Long nameId);
}