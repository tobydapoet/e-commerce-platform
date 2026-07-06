package com.example.e_commerce.service;

import com.example.e_commerce.constant.CreatorType;
import com.example.e_commerce.constant.OrderStoreStatus;
import com.example.e_commerce.dto.request.CreateOrderReq;
import com.example.e_commerce.entity.*;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.CartItemRepository;
import com.example.e_commerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepo;
    @Mock private CartItemRepository cartItemRepo;
    @Mock private AddressService addressService;
    @Mock private CouponService couponService;
    @Mock private CartStoreService cartStoreService;

    private OrderService orderService;

    private User currentUser;
    private User otherUser;
    private Address address;
    private Store store;
    private CartStore cartStore;
    private Product product;
    private Inventory inventory;
    private ProductVariant variant;
    private CartItem cartItem;
    private CreateOrderReq req;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepo, cartItemRepo, addressService, couponService, cartStoreService);

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());

        otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        address = new Address();
        address.setId(10L);
        address.setUser(currentUser);

        store = new Store();
        store.setId(100L);
        store.setName("Test Store");

        cartStore = new CartStore();
        cartStore.setId(200L);
        cartStore.setUser(currentUser);
        cartStore.setStore(store);

        product = new Product();
        product.setId(300L);
        product.setName("Test Product");
        product.setStore(store);

        inventory = new Inventory();
        inventory.setQuantity(10);

        variant = new ProductVariant();
        variant.setId(400L);
        variant.setProduct(product);
        variant.setPrice(BigDecimal.valueOf(100_000));
        variant.setInventory(inventory);

        cartItem = new CartItem();
        cartItem.setId(500L);
        cartItem.setCartStore(cartStore);
        cartItem.setProductVariant(variant);
        cartItem.setQuantity(2);

        req = new CreateOrderReq();
        req.setAddressId(10L);
        req.setCartItemIds(List.of(500L));
    }

    @Nested
    @DisplayName("Create")
    class Create {
        @DisplayName("Success no coupon")
        @Test
        void success_noCoupon() {
            when(cartItemRepo.findAllByIdIn(req.getCartItemIds())).thenReturn(List.of(cartItem));
            when(addressService.findById(req.getAddressId())).thenReturn(address);
            when(orderRepo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            CartItemRepository.CartStoreItemCount countMock =
                    mock(CartItemRepository.CartStoreItemCount.class);
            when(countMock.getCartStoreId()).thenReturn(cartStore.getId());
            when(countMock.getTotal()).thenReturn(1L);
            when(cartItemRepo.countGroupByCartStoreId(anyList())).thenReturn(List.of(countMock));

            Order result = orderService.create(currentUser, req);

            assertNotNull(result);
            assertEquals(currentUser, result.getUser());
            assertEquals(address, result.getAddress());
            assertEquals(1, result.getOrderStores().size());

            OrderStore savedOrderStore = result.getOrderStores().iterator().next();
            assertEquals(store, savedOrderStore.getStore());
            assertEquals(OrderStoreStatus.PENDING, savedOrderStore.getStatus());
            assertEquals(0, BigDecimal.valueOf(200_000).compareTo(savedOrderStore.getSubtotal()));

            assertEquals(8, inventory.getQuantity());

            verify(cartItemRepo).deleteAll(List.of(cartItem));
            verify(cartStoreService).deleteMany(anyList());
        }
        @DisplayName("Empty cart item ids throws bad request")
        @Test
        void emptyCartItemIds_throwsBadRequest() {
            when(cartItemRepo.findAllByIdIn(req.getCartItemIds())).thenReturn(List.of());

            assertThrows(BadRequestException.class,
                    () -> orderService.create(currentUser, req));

            verifyNoInteractions(addressService);
            verify(orderRepo, never()).save(any());
        }
        @DisplayName("Item not belong to user throws forbidden")
        @Test
        void itemNotBelongToUser_throwsForbidden() {
            cartStore.setUser(otherUser);

            when(cartItemRepo.findAllByIdIn(req.getCartItemIds())).thenReturn(List.of(cartItem));

            assertThrows(ForbiddenException.class,
                    () -> orderService.create(currentUser, req));

            verify(orderRepo, never()).save(any());
        }
        @DisplayName("Address not belong to user throws forbidden")
        @Test
        void addressNotBelongToUser_throwsForbidden() {
            address.setUser(otherUser);

            when(cartItemRepo.findAllByIdIn(req.getCartItemIds())).thenReturn(List.of(cartItem));
            when(addressService.findById(req.getAddressId())).thenReturn(address);

            assertThrows(ForbiddenException.class,
                    () -> orderService.create(currentUser, req));

            verify(orderRepo, never()).save(any());
        }
        @DisplayName("Out of stock throws bad request")
        @Test
        void outOfStock_throwsBadRequest() {
            inventory.setQuantity(1); // cần 2 nhưng chỉ còn 1

            when(cartItemRepo.findAllByIdIn(req.getCartItemIds())).thenReturn(List.of(cartItem));
            when(addressService.findById(req.getAddressId())).thenReturn(address);

            assertThrows(BadRequestException.class,
                    () -> orderService.create(currentUser, req));

            verify(orderRepo, never()).save(any());
        }
        @DisplayName("With store coupon applies discount")
        @Test
        void withStoreCoupon_appliesDiscount() {
            Coupon storeCoupon = new Coupon();
            storeCoupon.setId(900L);
            cartStore.setStoreCoupon(storeCoupon);

            when(cartItemRepo.findAllByIdIn(req.getCartItemIds())).thenReturn(List.of(cartItem));
            when(addressService.findById(req.getAddressId())).thenReturn(address);
            when(couponService.validateAndGet(eq(900L), eq(CreatorType.STORE), any(BigDecimal.class)))
                    .thenReturn(storeCoupon);
            when(couponService.calculateDiscount(eq(storeCoupon), any(BigDecimal.class)))
                    .thenReturn(BigDecimal.valueOf(20_000));
            when(orderRepo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(cartItemRepo.countGroupByCartStoreId(anyList())).thenReturn(List.of());

            Order result = orderService.create(currentUser, req);

            OrderStore savedOrderStore = result.getOrderStores().iterator().next();
            assertEquals(storeCoupon, savedOrderStore.getStoreCoupon());
            assertEquals(0, BigDecimal.valueOf(20_000).compareTo(savedOrderStore.getDiscount()));
            assertEquals(0, BigDecimal.valueOf(180_000).compareTo(savedOrderStore.getTotal()));
        }
        @DisplayName("With platform coupon applies discount")
        @Test
        void withPlatformCoupon_appliesDiscount() {
            Coupon platformCoupon = new Coupon();
            platformCoupon.setId(999L);
            req.setPlatformCouponId(999L);

            when(cartItemRepo.findAllByIdIn(req.getCartItemIds())).thenReturn(List.of(cartItem));
            when(addressService.findById(req.getAddressId())).thenReturn(address);
            when(couponService.validateAndGet(eq(999L), eq(CreatorType.SYSTEM), any(BigDecimal.class)))
                    .thenReturn(platformCoupon);
            when(couponService.calculateDiscount(eq(platformCoupon), any(BigDecimal.class)))
                    .thenReturn(BigDecimal.valueOf(10_000));
            when(orderRepo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(cartItemRepo.countGroupByCartStoreId(anyList())).thenReturn(List.of());

            Order result = orderService.create(currentUser, req);

            assertEquals(platformCoupon, result.getPlatformCoupon());
            assertEquals(0, BigDecimal.valueOf(10_000).compareTo(result.getDiscount()));
            assertEquals(0, BigDecimal.valueOf(190_000).compareTo(result.getTotal()));
        }
        @DisplayName("Partial checkout does not delete cart store")
        @Test
        void partialCheckout_doesNotDeleteCartStore() {
            when(cartItemRepo.findAllByIdIn(req.getCartItemIds())).thenReturn(List.of(cartItem));
            when(addressService.findById(req.getAddressId())).thenReturn(address);
            when(orderRepo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            CartItemRepository.CartStoreItemCount countMock =
                    mock(CartItemRepository.CartStoreItemCount.class);
            when(countMock.getCartStoreId()).thenReturn(cartStore.getId());
            when(countMock.getTotal()).thenReturn(3L);
            when(cartItemRepo.countGroupByCartStoreId(anyList())).thenReturn(List.of(countMock));

            orderService.create(currentUser, req);

            verify(cartStoreService, never()).deleteMany(anyList());
        }
    }

    @Nested
    @DisplayName("Find By ID")
    class FindById {
        @DisplayName("Found returns order")
        @Test
        void found_returnsOrder() {
            Order order = new Order();
            order.setId(1L);
            when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

            Order result = orderService.findById(1L);

            assertEquals(order, result);
        }
        @DisplayName("Not found throws resource not found")
        @Test
        void notFound_throwsResourceNotFound() {
            when(orderRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> orderService.findById(999L));
        }
    }

    @Nested
    @DisplayName("Get My Order Detail")
    class GetMyOrderDetail {
        @DisplayName("Order not belong to user throws forbidden")
        @Test
        void orderNotBelongToUser_throwsForbidden() {
            Order order = new Order();
            order.setId(1L);
            order.setUser(otherUser);

            when(orderRepo.findWithDetailsById(1L)).thenReturn(Optional.of(order));

            assertThrows(ForbiddenException.class,
                    () -> orderService.getMyOrderDetail(currentUser, 1L));
        }
        @DisplayName("Not found throws resource not found")
        @Test
        void notFound_throwsResourceNotFound() {
            when(orderRepo.findWithDetailsById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> orderService.getMyOrderDetail(currentUser, 1L));
        }
    }
}