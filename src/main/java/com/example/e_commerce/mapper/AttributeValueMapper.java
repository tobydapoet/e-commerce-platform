package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.AttributeValueRes;
import com.example.e_commerce.entity.AttributeValue;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttributeValueMapper {
    AttributeValueRes toAttributeValueRes(AttributeValue attributeValue);
}
