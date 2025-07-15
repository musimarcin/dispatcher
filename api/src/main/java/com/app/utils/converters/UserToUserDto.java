package com.app.utils.converters;

import com.app.dto.UserDto;
import com.app.model.UserEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserDto implements Converter<UserEntity, UserDto> {

    @Override
    public UserDto convert(UserEntity source) {
        return new UserDto(
                source.getId(),
                source.getUsername(),
                source.getPassword(),
                source.getEmail(),
                source.getRegistered(),
                source.getRoles()
        );
    }
}
