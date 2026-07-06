package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.CreateProductReq;
import com.example.e_commerce.dto.response.ProductVariantRes;
import com.example.e_commerce.entity.AttributeValue;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.ProductVariant;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.ProductVariantMapper;
import com.example.e_commerce.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductVariantService {
    private final ProductVariantRepository productVariantRepo;
    private final UploadService uploadService;
    private final ProductVariantMapper mapper;
    private final AttributeValueService attributeValueService;

    public ProductVariant findById(Long productVariantId) {
        return productVariantRepo.findById(productVariantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant Not Found"));
    }

    public List<ProductVariant> create(
            Product product,
            List<CreateProductReq.VariantReq> requests) {

        List<MultipartFile> images = requests.stream()
                .map(CreateProductReq.VariantReq::getImage)
                .toList();

        Map<Integer, MultipartFile> imagesToUpload = new LinkedHashMap<>();
        for (int i = 0; i < images.size(); i++) {
            MultipartFile img = images.get(i);
            if (img != null && !img.isEmpty()) {
                imagesToUpload.put(i, img);
            }
        }

        List<String> uploadedUrls = uploadService.uploadMultiple(
                new ArrayList<>(imagesToUpload.values()), "variant_image"
        );

        Map<Integer, String> urlByIndex = new HashMap<>();
        int idx = 0;
        for (Integer originalIndex : imagesToUpload.keySet()) {
            urlByIndex.put(originalIndex, uploadedUrls.get(idx++));
        }

        List<ProductVariant> variants = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            CreateProductReq.VariantReq req = requests.get(i);
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setPrice(req.getPrice());
            variant.setActive(true);
            variant.setSku("SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

            if (urlByIndex.containsKey(i)) {
                variant.setImage(urlByIndex.get(i));
            }

            variants.add(variant);
        }

        return productVariantRepo.saveAll(variants);
    }

    public ProductVariantRes getVariantByAttributeValueIds(List<Long> attributeValueIds) {
        if (attributeValueIds == null || attributeValueIds.isEmpty()) {
            throw new ResourceNotFoundException("Variant not found");
        }

        List<AttributeValue> attributeValues = attributeValueService.findAllById(attributeValueIds);

        if (attributeValues.size() != attributeValueIds.size()) {
            throw new ResourceNotFoundException("One or more attribute values not found");
        }

        List<Long> resolvedIds = attributeValues.stream()
                .map(AttributeValue::getId)
                .toList();

        ProductVariant variant = productVariantRepo.findByAttributeValueIds(
                        resolvedIds,
                        resolvedIds.size())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

        return mapper.toProductVariantRes(variant);
    }
}