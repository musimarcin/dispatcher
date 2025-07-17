package com.app.repository;

import com.app.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface UserRepo extends JpaRepository<UserEntity, Long> {

    UserEntity findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.email = :email WHERE u.username = :username")
    int updateEmail(@Param("username") String username, @Param("email") String email);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.username = :newUsername WHERE u.username = :oldUsername")
    int updateUsername(@Param("newUsername") String newUsername, @Param("oldUsername") String oldUsername);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.password = :password WHERE u.username = :username")
    int updatePassword(@Param("password") String password, @Param("username") String username);

}
