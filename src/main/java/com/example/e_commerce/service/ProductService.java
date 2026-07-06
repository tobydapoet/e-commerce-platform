package com.example.e_commerce.service;

import com.example.e_commerce.constant.ProductStatus;
import com.example.e_commerce.dto.request.CreateProductReq;
import com.example.e_commerce.dto.request.UpdateProductReq;
import com.example.e_commerce.dto.request.UpdateProductStatusReq;
import com.example.e_commerce.dto.response.ProductDetailRes;
import com.example.e_commerce.dto.response.ProductPriceRangeProjection;
import com.example.e_commerce.dto.response.ProductRes;
import com.example.e_commerce.entity.*;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.ProductMapper;
import com.example.e_commerce.repository.ProductRepository;
import com.example.e_commerce.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final UploadService uploadService;
    private final ProductPersistenceService productPersistenceService;
    private final ProductVariantRepository productVariantRepo;
    private final ProductRepository productRepo;
    private final ProductMapper mapper;
    private final CategoryService categoryService;

    public Product create(CreateProductReq req) {
        CompletableFuture<String> thumbnailFuture = CompletableFuture.supplyAsync(() ->
                uploadService.upload(req.getThumbnail(), "thumbnail_product"));

        List<CreateProductReq.VariantReq> variantReqs = req.getVariants();
        Map<Integer, MultipartFile> variantImagesToUpload = new LinkedHashMap<>();
        for (int i = 0; i < variantReqs.size(); i++) {
            MultipartFile img = variantReqs.get(i).getImage();
            if (img != null && !img.isEmpty()) {
                variantImagesToUpload.put(i, img);
            }
        }
        CompletableFuture<List<String>> variantImagesFuture = CompletableFuture.supplyAsync(() ->
                uploadService.uploadMultiple(
                        new ArrayList<>(variantImagesToUpload.values()), "variant_image"));

        CompletableFuture.allOf(thumbnailFuture, variantImagesFuture).join();

        String thumbnailUrl = thumbnailFuture.join();
        List<String> uploadedVariantUrls = variantImagesFuture.join();

        Map<Integer, String> variantImageUrlByIndex = new HashMap<>();
        int idx = 0;
        for (Integer originalIndex : variantImagesToUpload.keySet()) {
            variantImageUrlByIndex.put(originalIndex, uploadedVariantUrls.get(idx++));
        }

        return productPersistenceService.saveProduct(
                req, thumbnailUrl, variantImageUrlByIndex);
    }

    public Product findById(Long productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public ProductDetailRes findByIdWithPrice(Long productId) {
        Product product = findById(productId);

        ProductDetailRes base = mapper.toProductDetailRes(product);

        List<ProductPriceRangeProjection> ranges =
                productVariantRepo.findPriceRangeByProductIds(List.of(productId));

        BigDecimal minPrice = ranges.isEmpty() ? BigDecimal.ZERO : ranges.get(0).getMinPrice();
        BigDecimal maxPrice = ranges.isEmpty() ? BigDecimal.ZERO : ranges.get(0).getMaxPrice();

        return new ProductDetailRes(
                base.id(),
                base.name(),
                base.thumbnail(),
                base.description(),
                minPrice,
                maxPrice,
                base.attributes()
        );
    }

    private Page<ProductRes> mapPageWithPriceRange(Page<Product> products) {
        List<Long> productIds = products.getContent().stream()
                .map(Product::getId)
                .toList();

        Map<Long, ProductPriceRangeProjection> priceRangeMap = productIds.isEmpty()
                ? Map.of()
                : productVariantRepo.findPriceRangeByProductIds(productIds).stream()
                .collect(Collectors.toMap(
                        ProductPriceRangeProjection::getProductId,
                        Function.identity()
                ));

        return products.map(product -> {
            ProductRes base = mapper.toProductRes(product);
            ProductPriceRangeProjection range = priceRangeMap.get(product.getId());

            BigDecimal minPrice = range != null ? range.getMinPrice() : BigDecimal.ZERO;
            BigDecimal maxPrice = range != null ? range.getMaxPrice() : BigDecimal.ZERO;

            return new ProductRes(
                    base.id(),
                    base.name(),
                    base.thumbnail(),
                    minPrice,
                    maxPrice,
                    base.status(),
                    base.soldCount()
            );
        });
    }

    public Page<ProductRes> getByPage(Pageable pageable) {
        Page<Product> products = productRepo.getByPage(
                List.of(ProductStatus.INACTIVE, ProductStatus.DELETED),
                pageable
        );

        return mapPageWithPriceRange(products);
    }

    public Page<ProductRes> search(String keyword, Pageable pageable) {
        Page<Product> products = productRepo.search(
                List.of(ProductStatus.INACTIVE, ProductStatus.DELETED),
                keyword,
                pageable
        );
        return mapPageWithPriceRange(products);
    }

    public void update(Long id, UpdateProductReq updateProductReq) {
        Product product =  this.findById(id);
        if(updateProductReq.getName() != null) {
            product.setName(updateProductReq.getName());
        }
        if(updateProductReq.getDescription() != null) {
            product.setDescription(updateProductReq.getDescription());
        }
        if(updateProductReq.getThumbnail() != null) {
            if(product.getThumbnail() != null) {
                uploadService.delete(product.getThumbnail());
            }
            String thumbnail = uploadService.upload(updateProductReq.getThumbnail(), product.getThumbnail());
            product.setThumbnail(thumbnail);
        }
        if (updateProductReq.getCategoryId() != null) {
            Category category = categoryService.findById(updateProductReq.getCategoryId());
            product.setCategory(category);
        }
        productRepo.save(product);
    }

    public void updateStatus(Long id, UpdateProductStatusReq req) {
        Product product =  findById(id);
        product.setStatus(req.getStatus());
        productRepo.save(product);
    }
}
