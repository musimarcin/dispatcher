package com.app.utils;

import com.app.dto.UserInfo;
import com.app.model.UserEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserInfo implements Converter<UserEntity, UserInfo> {

    @Override
    public UserInfo convert(UserEntity source) {
        return new UserInfo(source.getId(), source.getUsername(), source.getRoles());
    }
}
