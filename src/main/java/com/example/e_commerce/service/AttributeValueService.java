package com.example.e_commerce.service;

import com.example.e_commerce.dto.response.AttributeValueRes;
import com.example.e_commerce.entity.Attribute;
import com.example.e_commerce.entity.AttributeValue;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ResourceInUseException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.AttributeValueMapper;
import com.example.e_commerce.repository.AttributeRepository;
import com.example.e_commerce.repository.AttributeValueRepository;
import com.example.e_commerce.repository.ProductVariantAttributeValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttributeValueService {
    private final AttributeValueRepository attributeValueRepo;
    private final ProductVariantAttributeValueRepository productVariantAttributeValueRepo;
    private final AttributeRepository attributeRepo;
    private final AttributeValueMapper mapper;
    private static final int MAX_ATTRIBUTE_VALUES = 20;

    public List<AttributeValue> findAllById(List<Long> Ids){
        return attributeValueRepo.findAllById(Ids);
    }

    public List<AttributeValue> createBatch(Map<Attribute, List<String>> valuesByAttribute) {

        for (Map.Entry<Attribute, List<String>> entry : valuesByAttribute.entrySet()) {
            long distinctValues = entry.getValue().stream()
                    .distinct()
                    .count();

            if (distinctValues > MAX_ATTRIBUTE_VALUES) {
                throw new BadRequestException(
                        "Attribute '" + entry.getKey().getName()
                                + "' can have at most "
                                + MAX_ATTRIBUTE_VALUES
                                + " values."
                );
            }
        }

        List<AttributeValue> attributeValues = valuesByAttribute.entrySet().stream()
                .flatMap(entry -> {
                    Attribute attribute = entry.getKey();
                    return entry.getValue().stream()
                            .distinct()
                            .map(value -> {
                                AttributeValue av = new AttributeValue();
                                av.setAttribute(attribute);
                                av.setValue(value);
                                return av;
                            });
                })
                .toList();

        return attributeValueRepo.saveAll(attributeValues);
    }

    public List<AttributeValue> createForAttribute(Long attributeId, List<String> values) {
        Attribute attribute = attributeRepo.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute not found."));
        return createBatch(Map.of(attribute, values));
    }

    public AttributeValue findById(Long id) {
        return attributeValueRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute value not found"));
    }

    public List<AttributeValueRes> findByAttributeId(Long attributeId) {
        return attributeValueRepo.findByAttributeId(attributeId)
                .stream()
                .map(mapper::toAttributeValueRes)
                .toList();
    }

    public void update(Long id, String value) {
        AttributeValue attributeValue = findById(id);
        attributeValue.setValue(value);
        attributeValueRepo.save(attributeValue);
    }

    public void delete(Long attributeValueId) {
        boolean isInUse = productVariantAttributeValueRepo.existsByAttributeValueId(attributeValueId);
        if (isInUse) {
            throw new ResourceInUseException(
                    "Cannot delete this attribute value because it is associated with one or more product variants."
            );
        }
        attributeValueRepo.deleteById(attributeValueId);
    }
}
