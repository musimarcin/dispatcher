package com.app.repository;

import com.app.model.Role;
import com.app.model.UserEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepoTests {

    @Autowired
    private UserRepo userRepo;

    private UserEntity user1;
    private UserEntity user2;
    private UserEntity user3;

    @BeforeEach
    void setUp() {
        HashSet<Role> roleDriver = new HashSet<>();
        roleDriver.add(Role.ROLE_DRIVER);
        HashSet<Role> roleDispatcher = new HashSet<>();
        roleDispatcher.add(Role.ROLE_DISPATCHER);
        HashSet<Role> allRoles = new HashSet<>();
        allRoles.add(Role.ROLE_DRIVER);
        allRoles.add(Role.ROLE_DISPATCHER);
        user1 = UserEntity.builder()
                .username("John")
                .password("smith")
                .email("john@smith")
                .roles(roleDriver)
                .build();
        user2 = UserEntity.builder()
                .username("Adam")
                .password("mada")
                .email("adam@mada")
                .roles(roleDispatcher)
                .build();
        user3 = UserEntity.builder()
                .username("asdf")
                .password("qwerty")
                .email("asdf@qwerty")
                .roles(allRoles)
                .build();

        userRepo.saveAll(List.of(user1, user2, user3));
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll();
    }

    @Test
    public void shouldFindUserByUsername() {
        UserEntity found = userRepo.findByUsername("John").get();

        assertEquals(user1.getUsername(), found.getUsername());
    }
}
