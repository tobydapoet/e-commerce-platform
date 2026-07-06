package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.ProductVariantRes;
import com.example.e_commerce.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {
    @Mapping(target = "quantity", source = "inventory.quantity")
    ProductVariantRes toProductVariantRes(ProductVariant productVariant);
}
