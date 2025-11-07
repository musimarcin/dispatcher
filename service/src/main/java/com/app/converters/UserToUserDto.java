package com.app.converters;

import com.app.dto.UserDto;
import com.app.model.UserEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserToUserDto implements Converter<UserEntity, UserDto> {

    @Override
    public UserDto convert(UserEntity source) {
        return new UserDto(
                source.getId(),
                source.getUsername(),
                "",
                source.getEmail(),
                source.getRegistered(),
                source.getRoles().stream().map(String::valueOf).collect(Collectors.toSet())
        );
    }
}