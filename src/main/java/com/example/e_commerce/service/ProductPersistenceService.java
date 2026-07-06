package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.CreateProductReq;
import com.example.e_commerce.entity.*;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductPersistenceService {

    private final ProductRepository productRepo;
    private final ProductVariantAttributeValueService productVariantAttributeService;
    private final ProductVariantService productVariantService;
    private final InventoryService inventoryService;
    private final AttributeService attributeService;
    private final AttributeValueService attributeValueService;
    private final CategoryService categoryService;

    @Transactional
    public Product saveProduct(
            CreateProductReq req,
            String thumbnailUrl,
            Map<Integer, String> variantImageUrlByIndex) {

        Product product = new Product();
        product.setName(req.getName());
        product.setDescription(req.getDescription());

        Category category = categoryService.findById(req.getCategoryId());
        product.setCategory(category);
        product.setThumbnail(thumbnailUrl);

        product = productRepo.save(product);

        List<String> attributeNames = req.getVariants().stream()
                .flatMap(variant -> variant.getOptions().stream())
                .map(CreateProductReq.OptionReq::getName)
                .distinct()
                .toList();

        List<Attribute> attributes = attributeService.create(attributeNames, product);

        Map<String, Attribute> attributeMap = attributes.stream()
                .collect(Collectors.toMap(Attribute::getName, Function.identity()));

        Map<Attribute, List<String>> valuesByAttribute = req.getVariants().stream()
                .flatMap(variant -> variant.getOptions().stream())
                .collect(Collectors.groupingBy(
                        opt -> attributeMap.get(opt.getName()),
                        Collectors.mapping(CreateProductReq.OptionReq::getValue, Collectors.toList())
                ));

        List<AttributeValue> attributeValues = attributeValueService.createBatch(valuesByAttribute);

        Map<String, AttributeValue> attributeValueMap = attributeValues.stream()
                .collect(Collectors.toMap(
                        av -> av.getAttribute().getName() + ":" + av.getValue(),
                        Function.identity()
                ));

        List<CreateProductReq.VariantReq> variantReqs = req.getVariants();
        List<ProductVariant> variants = productVariantService.create(product, variantReqs);

        List<ProductVariantAttributeValue> variantAttributeValues = new ArrayList<>();
        List<Inventory> inventories = new ArrayList<>();

        for (int i = 0; i < variantReqs.size(); i++) {
            CreateProductReq.VariantReq variantReq = variantReqs.get(i);
            ProductVariant variant = variants.get(i);

            String variantImageUrl = variantImageUrlByIndex.get(i);
            if (variantImageUrl != null) {
                variant.setImage(variantImageUrl);
            }

            for (CreateProductReq.OptionReq opt : variantReq.getOptions()) {
                String key = opt.getName() + ":" + opt.getValue();
                AttributeValue attributeValue = attributeValueMap.get(key);
                if (attributeValue == null) {
                    throw new ResourceNotFoundException("Can't find attribute value for: " + key);
                }

                ProductVariantAttributeValue pvav = new ProductVariantAttributeValue();
                pvav.setProductVariant(variant);
                pvav.setAttributeValue(attributeValue);
                variantAttributeValues.add(pvav);
            }

            Inventory inventory = new Inventory();
            inventory.setProductVariant(variant);
            inventory.setQuantity(variantReq.getQuantity());
            inventories.add(inventory);
        }

        productVariantAttributeService.createBatch(variantAttributeValues);
        inventoryService.createBatch(inventories);

        return product;
    }
}