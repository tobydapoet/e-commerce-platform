package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.AttributeRes;
import com.example.e_commerce.dto.response.AttributeValueRes;
import com.example.e_commerce.dto.response.ProductDetailRes;
import com.example.e_commerce.dto.response.ProductRes;
import com.example.e_commerce.entity.Attribute;
import com.example.e_commerce.entity.AttributeValue;
import com.example.e_commerce.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "minPrice", ignore = true)
    @Mapping(target = "maxPrice", ignore = true)
    ProductDetailRes toProductDetailRes(Product product);

    @Mapping(target = "values", source = "attributeValues")
    AttributeRes toAttributeRes(Attribute attribute);

    AttributeValueRes toAttributeValueRes(AttributeValue attributeValue);

    @Mapping(target = "minPrice", ignore = true)
    @Mapping(target = "maxPrice", ignore = true)
    ProductRes toProductRes(Product product);
}
