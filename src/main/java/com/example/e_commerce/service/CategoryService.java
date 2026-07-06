package com.example.e_commerce.service;

import com.example.e_commerce.constant.CategoryStatus;
import com.example.e_commerce.dto.request.CreateCategoryReq;
import com.example.e_commerce.entity.Category;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.exception.DuplicateResourceException;
import com.example.e_commerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepo;

    public Category findById(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category Not Found"));
    }

    public Category create(CreateCategoryReq req) {
        Category parent = null;

        if (req.getParentId() != null) {
            parent = findById(req.getParentId());
        }

        boolean isDuplicate = (parent == null)
                ? categoryRepo.existsByNameAndParentIsNull(req.getName())
                : categoryRepo.existsByNameAndParent(req.getName(), parent);

        if (isDuplicate) {
            throw new DuplicateResourceException(
                    "Category '" + req.getName() + "' already exists in this branch");
        }

        Category category = new Category();
        category.setName(req.getName());
        category.setDescription(req.getDescription());
        category.setStatus(CategoryStatus.ACTIVE);
        category.setParent(parent);

        return categoryRepo.save(category);
    }
}