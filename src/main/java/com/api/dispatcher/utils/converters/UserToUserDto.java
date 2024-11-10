package com.api.dispatcher.utils.converters;

import com.api.dispatcher.dto.UserDto;
import com.api.dispatcher.model.UserEntity;
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
