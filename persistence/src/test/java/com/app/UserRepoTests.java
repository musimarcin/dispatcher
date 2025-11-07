//package com.app;
//
//import com.app.model.Role;
//import com.app.model.UserEntity;
//import com.app.repository.UserRepo;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.BeforeEach;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Optional;
//
//@DataJpaTest
//public class UserRepoTests {
//
//    @Autowired
//    private UserRepo userRepo;
//
//    @BeforeEach
//    void setUp() {
//        HashSet<Role> roleDriver = new HashSet<>();
//        roleDriver.add(Role.ROLE_DRIVER);
//        HashSet<Role> roleDispatcher = new HashSet<>();
//        roleDispatcher.add(Role.ROLE_DISPATCHER);
//        HashSet<Role> allRoles = new HashSet<>();
//        allRoles.add(Role.ROLE_DRIVER);
//        allRoles.add(Role.ROLE_DISPATCHER);
//        UserEntity user1 = UserEntity.builder()
//                .username("John")
//                .password("smith")
//                .email("john@smith")
//                .roles(roleDriver)
//                .build();
//        UserEntity user2 = UserEntity.builder()
//                .username("Adam")
//                .password("mada")
//                .email("adam@mada")
//                .roles(roleDispatcher)
//                .build();
//        UserEntity user3 = UserEntity.builder()
//                .username("asdf")
//                .password("qwerty")
//                .email("asdf@qwerty")
//                .roles(allRoles)
//                .build();
//
//        userRepo.saveAll(List.of(user1, user2, user3));
//    }
//
//    @Test
//    public void shouldFindUserByUsername() {
//        Optional<UserEntity> found = userRepo.findByUsername("John");
//
//        Assertions.assertThat(found)
//                .isPresent()
//                .get()
//                .extracting(UserEntity::getEmail)
//                .isEqualTo("john@smith");
//    }
//}
