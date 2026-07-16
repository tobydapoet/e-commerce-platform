package com.example.e_commerce.service;

import com.example.e_commerce.constant.CategoryStatus;
import com.example.e_commerce.dto.request.CreateCategoryReq;
import com.example.e_commerce.entity.Category;
import com.example.e_commerce.exception.DuplicateResourceException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.repository.CategoryRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category Service tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepo;

    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("Find By ID")
    class FindById {
        @DisplayName("Should return category when exists")
        @Test
        void shouldReturnCategory_whenExists() {
            Category category = new Category();
            category.setId(1L);

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));

            Category result = categoryService.findById(1L);

            assertThat(result).isEqualTo(category);
        }
        @DisplayName("Should throw when not found")
        @Test
        void shouldThrow_whenNotFound() {
            when(categoryRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category Not Found");
        }
    }

    @Nested
    @DisplayName("Create")
    class Create {
        @DisplayName("Should create root category when no parent")
        @Test
        void shouldCreateRootCategory_whenNoParent() {
            CreateCategoryReq req = mock(CreateCategoryReq.class);
            when(req.getName()).thenReturn("Electronics");
            when(req.getDescription()).thenReturn("Electronic devices");
            when(req.getParentId()).thenReturn(null);

            when(categoryRepo.existsByNameAndParentIsNull("Electronics")).thenReturn(false);
            when(categoryRepo.save(any(Category.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Category result = categoryService.create(req);

            assertThat(result.getName()).isEqualTo("Electronics");
            assertThat(result.getDescription()).isEqualTo("Electronic devices");
            assertThat(result.getStatus()).isEqualTo(CategoryStatus.ACTIVE);
            assertThat(result.getParent()).isNull();

            verify(categoryRepo, times(1)).existsByNameAndParentIsNull("Electronics");
            verify(categoryRepo, never()).existsByNameAndParent(any(), any());
            verify(categoryRepo, times(1)).save(any(Category.class));
        }
        @DisplayName("Should create child category when parent provided")
        @Test
        void shouldCreateChildCategory_whenParentProvided() {
            Category parent = new Category();
            parent.setId(5L);
            parent.setName("Electronics");

            CreateCategoryReq req = mock(CreateCategoryReq.class);
            when(req.getName()).thenReturn("Laptops");
            when(req.getDescription()).thenReturn("Laptop computers");
            when(req.getParentId()).thenReturn(5L);

            when(categoryRepo.findById(5L)).thenReturn(Optional.of(parent));
            when(categoryRepo.existsByNameAndParent("Laptops", parent)).thenReturn(false);
            when(categoryRepo.save(any(Category.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Category result = categoryService.create(req);

            assertThat(result.getName()).isEqualTo("Laptops");
            assertThat(result.getParent()).isEqualTo(parent);

            verify(categoryRepo, times(1)).findById(5L);
            verify(categoryRepo, times(1)).existsByNameAndParent("Laptops", parent);
            verify(categoryRepo, never()).existsByNameAndParentIsNull(any());
        }
        @DisplayName("Should throw when duplicate at root")
        @Test
        void shouldThrow_whenDuplicateAtRoot() {
            CreateCategoryReq req = mock(CreateCategoryReq.class);
            when(req.getName()).thenReturn("Electronics");
            when(req.getParentId()).thenReturn(null);

            when(categoryRepo.existsByNameAndParentIsNull("Electronics")).thenReturn(true);

            assertThatThrownBy(() -> categoryService.create(req))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Electronics")
                    .hasMessageContaining("already exists in this branch");

            verify(categoryRepo, never()).save(any());
        }
        @DisplayName("Should throw when duplicate under same parent")
        @Test
        void shouldThrow_whenDuplicateUnderSameParent() {
            Category parent = new Category();
            parent.setId(5L);

            CreateCategoryReq req = mock(CreateCategoryReq.class);
            when(req.getName()).thenReturn("Laptops");
            when(req.getParentId()).thenReturn(5L);

            when(categoryRepo.findById(5L)).thenReturn(Optional.of(parent));
            when(categoryRepo.existsByNameAndParent("Laptops", parent)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.create(req))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Laptops");

            verify(categoryRepo, never()).save(any());
        }
        @DisplayName("Should allow same name under different parent")
        @Test
        void shouldAllowSameName_underDifferentParent() {
            Category parent = new Category();
            parent.setId(7L);

            CreateCategoryReq req = mock(CreateCategoryReq.class);
            when(req.getName()).thenReturn("Accessories");
            when(req.getParentId()).thenReturn(7L);

            when(categoryRepo.findById(7L)).thenReturn(Optional.of(parent));
            when(categoryRepo.existsByNameAndParent("Accessories", parent)).thenReturn(false);
            when(categoryRepo.save(any(Category.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Category result = categoryService.create(req);

            assertThat(result.getName()).isEqualTo("Accessories");
            assertThat(result.getParent()).isEqualTo(parent);
        }
        @DisplayName("Should throw when parent not found")
        @Test
        void shouldThrow_whenParentNotFound() {
            CreateCategoryReq req = mock(CreateCategoryReq.class);
            when(req.getParentId()).thenReturn(99L);

            when(categoryRepo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.create(req))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category Not Found");

            verify(categoryRepo, never()).existsByNameAndParent(any(), any());
            verify(categoryRepo, never()).existsByNameAndParentIsNull(any());
            verify(categoryRepo, never()).save(any());
        }
        @DisplayName("Should always set status active")
        @Test
        void shouldAlwaysSetStatusActive() {
            CreateCategoryReq req = mock(CreateCategoryReq.class);
            when(req.getName()).thenReturn("Books");
            when(req.getParentId()).thenReturn(null);

            when(categoryRepo.existsByNameAndParentIsNull("Books")).thenReturn(false);

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            when(categoryRepo.save(captor.capture()))
                    .thenAnswer(inv -> inv.getArgument(0));

            categoryService.create(req);

            assertThat(captor.getValue().getStatus()).isEqualTo(CategoryStatus.ACTIVE);
        }
    }
}