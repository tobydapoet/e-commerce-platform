package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.UserSimpleRes;
import com.example.e_commerce.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserSimpleRes toUserSimpleRes(User user);
}
