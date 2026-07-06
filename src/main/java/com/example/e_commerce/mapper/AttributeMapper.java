package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.AttributeRes;
import com.example.e_commerce.entity.Attribute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface AttributeMapper {
    @Mapping(target = "values", source = "attributeValues")
    AttributeRes toAttributeRes(Attribute attribute);
}
