package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.CreateAttributeReq;
import com.example.e_commerce.dto.request.UpdateAttributeReq;
import com.example.e_commerce.dto.response.AttributeRes;
import com.example.e_commerce.entity.Attribute;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ResourceInUseException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.AttributeMapper;
import com.example.e_commerce.repository.AttributeRepository;
import com.example.e_commerce.repository.AttributeValueRepository;
import com.example.e_commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttributeService {
    private final AttributeRepository attributeRepo;
    private final AttributeValueRepository attributeValueRepo;
    private final ProductRepository productRepo;
    private final AttributeMapper mapper;
    private static final int MAX_ATTRIBUTES = 10;

    public List<Attribute> create(List<String> names, Product product) {
        if (names.size() > MAX_ATTRIBUTES) {
            throw new BadRequestException(
                    "A product can have at most " + MAX_ATTRIBUTES + " attributes."
            );
        }

        List<Attribute> attributes = names.stream()
                .distinct()
                .map(name -> {
                    Attribute attribute = new Attribute();
                    attribute.setName(name);
                    attribute.setProduct(product);
                    return attribute;
                })
                .toList();

        return attributeRepo.saveAll(attributes);
    }

    public List<Attribute> createForProduct(CreateAttributeReq req, Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return create(req.getNames(), product);
    }

    public Attribute findById(Long id){
        return attributeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found"));
    }

    public void update(Long id, UpdateAttributeReq req) {
        Attribute attribute = findById(id);
        attribute.setName(req.getName());
        attributeRepo.save(attribute);
    }

    public List<AttributeRes> findByProductId(Long productId){
        return attributeRepo.findByProductId(productId).stream()
                .map(mapper::toAttributeRes)
                .toList();
    }

    public void delete(Long attributeId) {
        Attribute attribute = findById(attributeId);

        boolean isInUse = attributeValueRepo.existsByAttributeId(attributeId);
        if (isInUse) {
            throw new ResourceInUseException(
                    "Cannot delete this attribute because it still has associated attribute values. " +
                            "Please remove all attribute values before deleting the attribute."
            );
        }

        attributeRepo.delete(attribute);
    }
}
