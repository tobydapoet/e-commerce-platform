package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.StoreRes;
import com.example.e_commerce.dto.response.UserSimpleRes;
import com.example.e_commerce.entity.Store;
import com.example.e_commerce.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StoreMapper {
    @Mapping(target = "owner", source = "owner")
    StoreRes toStoreRes(Store store);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    UserSimpleRes toUserSimpleRes(User user);
}
