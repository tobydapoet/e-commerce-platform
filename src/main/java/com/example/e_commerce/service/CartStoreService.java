package com.example.e_commerce.service;

import com.example.e_commerce.constant.CreatorType;
import com.example.e_commerce.dto.response.CartStoreRes;
import com.example.e_commerce.entity.CartStore;
import com.example.e_commerce.entity.Coupon;
import com.example.e_commerce.entity.Store;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.CartStoreMapper;
import com.example.e_commerce.repository.CartStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartStoreService {
    private final CartStoreRepository cartStoreRepo;
    private final StoreService storeService;
    private final CouponService couponService;

    public CartStore create(User currentUser, Long storeId) {
        CartStore cartStore = new CartStore();
        cartStore.setUser(currentUser);
        Store store = storeService.findById(storeId);
        cartStore.setStore(store);
        return cartStoreRepo.save(cartStore);
    }

    public CartStore findById(Long Id) {
        return cartStoreRepo.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart store not found."));
    }

    public CartStore findOrCreate(User currentUser, Long storeId) {
        Store store = storeService.findById(storeId);
        return cartStoreRepo.findByUserAndStore(currentUser, store)
                .orElseGet(() -> create(currentUser, storeId));
    }

    public void delete(CartStore cartStore) {
        cartStoreRepo.delete(cartStore);
    }

    public void deleteMany(List<CartStore> cartStores) {
        cartStoreRepo.deleteAll(cartStores);
    }

    public CartStore addCoupon(Long id, Long couponId) {
        CartStore cartStore = findById(id);
        Coupon coupon = couponService.findById(couponId);

        if (coupon.getCreatorType() != CreatorType.STORE) {
            throw new BadRequestException("This coupon is not applicable for a store cart.");
        }
        if (!coupon.getStore().getId().equals(cartStore.getStore().getId())) {
            throw new BadRequestException("This coupon does not belong to this store.");
        }

        cartStore.setStoreCoupon(coupon);
        return cartStoreRepo.save(cartStore);
    }

    public CartStore removeCoupon(Long id) {
        CartStore cartStore = findById(id);
        if (cartStore.getStoreCoupon() == null) {
            throw new BadRequestException("This cart has no coupon applied.");
        }
        cartStore.setStoreCoupon(null);
        return cartStoreRepo.save(cartStore);
    }

    public Page<CartStoreRes> findAllByUser(User currentUser, Pageable pageable) {
        Page<CartStore> cartStores = cartStoreRepo.findAllByUserId(currentUser.getId(), pageable);
        return cartStores.map(CartStoreMapper::toStoreRes);
    }
}
