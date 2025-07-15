package com.app.utils.converters;

import com.app.dto.UserDto;
import com.app.model.UserEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

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
                source.getRoles()
        );
    }
}
