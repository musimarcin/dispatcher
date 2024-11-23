package com.api.dispatcher.utils.converters;

import com.api.dispatcher.dto.UserDto;
import com.api.dispatcher.model.Role;
import com.api.dispatcher.model.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserDto toDTO(UserEntity user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRegistered(),
                user.getRoles()
        );
    }

    public UserEntity toEntity(UserDto userDTO, List<Role> roles) {
        return new UserEntity(
                userDTO.getId(),
                userDTO.getUsername(),
                userDTO.getPassword(),
                userDTO.getEmail(),
                userDTO.getRegistered(),
                roles
        );
    }

}
