package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.CreateCartItemReq;
import com.example.e_commerce.dto.request.UpdateQuantityReq;
import com.example.e_commerce.entity.CartItem;
import com.example.e_commerce.entity.CartStore;
import com.example.e_commerce.entity.ProductVariant;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartItemService {
    private final CartItemRepository cartItemRepo;
    private final ProductVariantService productVariantService;
    private final CartStoreService cartStoreService;

    public CartItem create(User currentUser, CreateCartItemReq req) {
        ProductVariant productVariant = productVariantService.findById(req.getProductVariantId());

        Long storeId = productVariant.getProduct().getStore().getId();
        CartStore cartStore = cartStoreService.findOrCreate(currentUser, storeId);

        CartItem cartItem = new CartItem();
        cartItem.setCartStore(cartStore);
        cartItem.setUser(currentUser);
        cartItem.setProductVariant(productVariant);
        cartItem.setQuantity(req.getQuantity());
        return cartItemRepo.save(cartItem);
    }

    public CartItem findById(Long id){
        return cartItemRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found."));
    }

    public void updateQuantity(User currentUser, Long id, UpdateQuantityReq req){
        CartItem cartItem = findById(id);
        if(!cartItem.getCartStore().getUser().getId().equals(currentUser.getId())){
            throw new ForbiddenException("You are not allowed to update this item.");
        }
        cartItem.setQuantity(req.getQuantity());
        cartItemRepo.save(cartItem);
    }

    @Transactional
    public void delete(User currentUser, Long id) {
        CartItem cartItem = findById(id);
        CartStore cartStore = cartItem.getCartStore();

        if (!cartStore.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not allowed to delete this item.");
        }

        cartItemRepo.delete(cartItem);

        long remaining = cartItemRepo.countByCartStoreId(cartStore.getId());
        if (remaining == 0) {
            cartStoreService.delete(cartStore);
        }
    }
}
