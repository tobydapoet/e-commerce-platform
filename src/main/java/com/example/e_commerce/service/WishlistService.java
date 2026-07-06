package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.CreateWishlistReq;
import com.example.e_commerce.dto.response.ProductRes;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.Wishlist;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.ProductMapper;
import com.example.e_commerce.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepo;
    private final ProductService productService;
    private final ProductMapper productMapper;

    public Wishlist create(User currentUser, CreateWishlistReq req) {
        Product product = productService.findById(req.getProductId());
        Wishlist wishlist = new Wishlist();
        wishlist.setProduct(product);
        wishlist.setUser(currentUser);
        return wishlistRepo.save(wishlist);
    }

    public Wishlist findById(Long id) {
        return wishlistRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found."));
    }

    public void  delete(Long id) {
        Wishlist wishlist = findById(id);
        wishlistRepo.delete(wishlist);
    }

    public Page<ProductRes> findByUser(User currentUser, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Wishlist> wishlistPage = wishlistRepo.findByUserId(currentUser.getId(), sortedPageable);
        return wishlistPage.map(
                wishlist -> productMapper.toProductRes(wishlist.getProduct()));
    }
}
