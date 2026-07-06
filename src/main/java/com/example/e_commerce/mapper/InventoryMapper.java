package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.InventoryRes;
import com.example.e_commerce.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "sku", source = "productVariant.sku")
    @Mapping(target = "image", source = "productVariant.image")
    InventoryRes toInventoryRes(Inventory inventory);
}
