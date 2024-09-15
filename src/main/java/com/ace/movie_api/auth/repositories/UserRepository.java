package com.ace.movie_api.auth.repositories;

import com.ace.movie_api.auth.entities.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String username);

    @Transactional
    @Modifying
    @Query("update UserEntity u set u.password = ?2 where u.email = ?1")
    void updatePassword(Integer id, String password);
}
