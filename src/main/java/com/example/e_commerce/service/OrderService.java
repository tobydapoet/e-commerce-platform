package com.example.e_commerce.service;

import com.example.e_commerce.constant.CreatorType;
import com.example.e_commerce.constant.OrderStatus;
import com.example.e_commerce.constant.OrderStoreStatus;
import com.example.e_commerce.dto.request.CreateOrderReq;
import com.example.e_commerce.dto.response.OrderRes;
import com.example.e_commerce.entity.*;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.OrderMapper;
import com.example.e_commerce.repository.CartItemRepository;
import com.example.e_commerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepo;
    private final CartItemRepository cartItemRepo;
    private final AddressService addressService;
    private final CouponService couponService;
    private final CartStoreService cartStoreService;

    private String generateOrderCode() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String random = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 4)
                .toUpperCase();

        return "ORD" + timestamp + random;
    }

    @Transactional
    public Order create(User currentUser, CreateOrderReq req) {
        List<CartItem> selectedItems = cartItemRepo.findAllByIdIn(req.getCartItemIds());

        if (selectedItems.isEmpty()) {
            throw new BadRequestException("No items selected.");
        }

        for (CartItem item : selectedItems) {
            if (!item.getCartStore().getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("You are not allowed to checkout this item.");
            }
        }

        Address address = addressService.findById(req.getAddressId());
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("This address does not belong to you.");
        }

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setUser(currentUser);
        order.setAddress(address);

        Map<CartStore, List<CartItem>> itemsByCartStore = selectedItems.stream()
                .collect(Collectors.groupingBy(CartItem::getCartStore));

        BigDecimal orderSubtotal = BigDecimal.ZERO;
        BigDecimal orderShippingFee = BigDecimal.ZERO;

        for (Map.Entry<CartStore, List<CartItem>> entry : itemsByCartStore.entrySet()) {
            CartStore cartStore = entry.getKey();
            List<CartItem> storeItems = entry.getValue();

            OrderStore orderStore = new OrderStore();
            orderStore.setOrder(order);
            orderStore.setStore(cartStore.getStore());

            BigDecimal storeSubtotal = BigDecimal.ZERO;

            for (CartItem cartItem : storeItems) {
                ProductVariant variant = cartItem.getProductVariant();
                int quantity = cartItem.getQuantity();

                int availableStock = variant.getInventory().getQuantity();
                if (availableStock < quantity) {
                    throw new BadRequestException(
                            "Product " + variant.getProduct().getName() + " is out of stock.");
                }
                variant.getInventory().setQuantity(availableStock - quantity);

                BigDecimal price = variant.getPrice();
                BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(quantity));
                storeSubtotal = storeSubtotal.add(lineTotal);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderStore(orderStore);
                orderItem.setProductVariant(variant);
                orderItem.setPrice(price);
                orderItem.setQuantity(quantity);
                orderItem.setSubtotal(lineTotal);

                orderStore.getOrderItems().add(orderItem);
            }

            BigDecimal storeDiscount = BigDecimal.ZERO;
            if (cartStore.getStoreCoupon() != null) {
                Coupon storeCoupon = couponService.validateAndGet(
                        cartStore.getStoreCoupon().getId(), CreatorType.STORE, storeSubtotal);
                orderStore.setStoreCoupon(storeCoupon);
                storeDiscount = couponService.calculateDiscount(storeCoupon, storeSubtotal);
            }

            BigDecimal shippingFee = BigDecimal.ZERO;

            BigDecimal storeTotal = storeSubtotal
                    .subtract(storeDiscount)
                    .add(shippingFee);

            orderStore.setSubtotal(storeSubtotal);
            orderStore.setDiscount(storeDiscount);
            orderStore.setShippingFee(shippingFee);
            orderStore.setTotal(storeTotal);
            orderStore.setStatus(OrderStoreStatus.PENDING);

            order.getOrderStores().add(orderStore);

            orderSubtotal = orderSubtotal.add(storeSubtotal);
            orderShippingFee = orderShippingFee.add(shippingFee);
        }

        BigDecimal platformDiscount = BigDecimal.ZERO;
        if (req.getPlatformCouponId() != null) {
            Coupon platformCoupon = couponService.validateAndGet(
                    req.getPlatformCouponId(), CreatorType.SYSTEM, orderSubtotal);
            order.setPlatformCoupon(platformCoupon);
            platformDiscount = couponService.calculateDiscount(platformCoupon, orderSubtotal);
        }

        BigDecimal orderTotal = orderSubtotal
                .subtract(platformDiscount)
                .add(orderShippingFee);

        order.setSubtotal(orderSubtotal);
        order.setDiscount(platformDiscount);
        order.setTotal(orderTotal);

        Order savedOrder = orderRepo.save(order);

        List<Long> cartStoreIds = itemsByCartStore.keySet().stream()
                .map(CartStore::getId)
                .toList();

        Map<Long, Long> totalCountByStore = cartItemRepo.countGroupByCartStoreId(cartStoreIds).stream()
                .collect(Collectors.toMap(
                        CartItemRepository.CartStoreItemCount::getCartStoreId,
                        CartItemRepository.CartStoreItemCount::getTotal
                ));

        cartItemRepo.deleteAll(selectedItems);

        List<CartStore> emptyCartStores = itemsByCartStore.entrySet().stream()
                .filter(e -> {
                    long total = totalCountByStore.getOrDefault(e.getKey().getId(), 0L);
                    long selectedCount = e.getValue().size();
                    return total == selectedCount;
                })
                .map(Map.Entry::getKey)
                .toList();

        if (!emptyCartStores.isEmpty()) {
            cartStoreService.deleteMany(emptyCartStores);
        }

        return savedOrder;
    }

    public Order findById(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));
    }

    public Page<OrderRes> getMyOrders(User currentUser, Pageable pageable) {
        return orderRepo.findAllByUserId(currentUser.getId(), pageable)
                .map(OrderMapper::toOrderRes);
    }

    public OrderRes getMyOrderDetail(User currentUser, Long orderId) {
        Order order = orderRepo.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not allowed to view this order.");
        }
        return OrderMapper.toOrderRes(order);
    }
}
