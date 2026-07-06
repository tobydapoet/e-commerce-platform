package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.AttributeValueRes;
import com.example.e_commerce.dto.response.CartItemRes;
import com.example.e_commerce.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;

public interface CartItemMapper {
    public static CartItemRes toCartItemRes(CartItem cartItem) {
        var variant = cartItem.getProductVariant();
        BigDecimal subtotal = variant.getPrice()
                .multiply(new BigDecimal(cartItem.getQuantity()));
        List<AttributeValueRes> attributeValues = variant.getVariantAttributeValues().stream()
                .map(vav -> {
                    var attrValue = vav.getAttributeValue();
                    return new AttributeValueRes(attrValue.getId(), attrValue.getValue());
                })
                .toList();
        return CartItemRes.builder()
                .id(cartItem.getId())
                .productVariantId(variant.getId())
                .productName(variant.getProduct().getName())
                .variantName(variant.getProduct().getName())
                .thumbnail(variant.getImage())
                .attributeValues(attributeValues)
                .price(variant.getPrice())
                .quantity(cartItem.getQuantity())
                .subtotal(subtotal)
                .stock(variant.getInventory().getQuantity())
                .build();
    }
}
