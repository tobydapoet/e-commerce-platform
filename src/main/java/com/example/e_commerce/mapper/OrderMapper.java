package com.example.e_commerce.mapper;

import com.example.e_commerce.dto.response.OrderItemRes;
import com.example.e_commerce.dto.response.OrderRes;
import com.example.e_commerce.dto.response.OrderStoreRes;
import com.example.e_commerce.entity.Order;
import com.example.e_commerce.entity.OrderItem;
import com.example.e_commerce.entity.OrderStore;

public interface OrderMapper {
    public static OrderItemRes toItemRes(OrderItem item) {
        var variant = item.getProductVariant();
        return OrderItemRes.builder()
                .id(item.getId())
                .productVariantId(variant.getId())
                .productName(variant.getProduct().getName())
                .thumbnail(variant.getImage())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }

    public static OrderStoreRes toOrderStoreRes(OrderStore os) {
        return OrderStoreRes.builder()
                .id(os.getId())
                .storeId(os.getStore().getId())
                .storeName(os.getStore().getName())
                .items(os.getOrderItems().stream().map(OrderMapper::toItemRes).toList())
                .subtotal(os.getSubtotal())
                .discount(os.getDiscount())
                .shippingFee(os.getShippingFee())
                .total(os.getTotal())
                .status(os.getStatus())
                .note(os.getNote())
                .createdAt(os.getCreatedAt())
                .build();
    }

    public static OrderRes toOrderRes(Order order) {
        return OrderRes.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .orderStores(order.getOrderStores().stream()
                        .map(OrderMapper::toOrderStoreRes)
                        .toList())
                .createdAt(order.getCreatedAt())
                .build();
    }
}