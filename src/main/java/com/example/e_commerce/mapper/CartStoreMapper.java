package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.CartItemRes;
import com.example.e_commerce.dto.response.CartStoreRes;
import com.example.e_commerce.entity.CartStore;

import java.math.BigDecimal;
import java.util.List;

public interface CartStoreMapper {
    public static CartStoreRes toStoreRes(CartStore cartStore) {
        List<CartItemRes> items = cartStore.getCartItems().stream()
                .map(CartItemMapper::toCartItemRes)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemRes::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartStoreRes.builder()
                .id(cartStore.getId())
                .storeId(cartStore.getStore().getId())
                .storeName(cartStore.getStore().getName())
                .storeLogo(cartStore.getStore().getLogo())
                .couponId(cartStore.getStoreCoupon() != null ? cartStore.getStoreCoupon().getId() : null)
                .couponCode(cartStore.getStoreCoupon() != null ? cartStore.getStoreCoupon().getCode() : null)
                .items(items)
                .subtotal(subtotal)
                .createdAt(cartStore.getCreatedAt())
                .build();
    }
}
