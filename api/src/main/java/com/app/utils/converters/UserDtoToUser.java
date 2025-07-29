package com.app.utils.converters;

import com.app.dto.UserDto;
import com.app.model.Role;
import com.app.model.UserEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserDtoToUser implements Converter<UserDto, UserEntity> {

    @Override
    public UserEntity convert(UserDto source) {
        return new UserEntity(
                source.getId(),
                source.getUsername(),
                source.getPassword(),
                source.getEmail(),
                source.getRegistered(),
                source.getRoles().stream().map(Role::valueOf).collect(Collectors.toSet())
        );
    }
}
