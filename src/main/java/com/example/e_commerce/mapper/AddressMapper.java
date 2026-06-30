package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.AddressRes;
import com.example.e_commerce.dto.response.UserSimpleRes;
import com.example.e_commerce.entity.Address;
import com.example.e_commerce.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "user", source = "user")
    AddressRes toAddressRes(Address address);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    UserSimpleRes toUserSimpleRes(User user);
}