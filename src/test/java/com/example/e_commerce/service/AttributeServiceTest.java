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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Attribute Service tests")
class AttributeServiceTest {

    @Mock private AttributeRepository attributeRepo;
    @Mock private AttributeValueRepository attributeValueRepo;
    @Mock private ProductRepository productRepository;
    @Mock private AttributeMapper mapper;

    @InjectMocks
    private AttributeService attributeService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
    }

    @Nested
    @DisplayName("Create")
    class Create {
        @DisplayName("Should save attributes when names are valid")
        @Test
        void shouldSaveAttributes_whenNamesAreValid() {
            List<String> names = List.of("Color", "Size");

            when(attributeRepo.saveAll(anyList()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            List<Attribute> result = attributeService.create(names, product);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Color");
            assertThat(result.get(0).getProduct()).isEqualTo(product);
            verify(attributeRepo, times(1)).saveAll(anyList());
        }
        @DisplayName("Should remove duplicate names")
        @Test
        void shouldRemoveDuplicateNames() {
            List<String> names = List.of("Color", "Color", "Size");

            ArgumentCaptor<List<Attribute>> captor = ArgumentCaptor.forClass(List.class);
            when(attributeRepo.saveAll(captor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            attributeService.create(names, product);

            assertThat(captor.getValue()).hasSize(2);
        }
        @DisplayName("Should throw bad request when exceeds max attributes")
        @Test
        void shouldThrowBadRequest_whenExceedsMaxAttributes() {
            List<String> names = List.of("1","2","3","4","5","6","7","8","9","10","11");

            assertThatThrownBy(() -> attributeService.create(names, product))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("at most 10 attributes");

            verifyNoInteractions(attributeRepo);
        }
    }

    @Nested
    @DisplayName("Create For Product")
    class CreateForProduct {
        @DisplayName("Should fetch product and create attributes")
        @Test
        void shouldFetchProductAndCreateAttributes() {
            CreateAttributeReq req = mock(CreateAttributeReq.class);
            when(req.getNames()).thenReturn(List.of("Color"));
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(attributeRepo.saveAll(anyList()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            List<Attribute> result = attributeService.createForProduct(req, 1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getProduct()).isEqualTo(product);
            verify(productRepository, times(1)).findById(1L);
        }
        @DisplayName("Should throw when product not found")
        @Test
        void shouldThrow_whenProductNotFound() {
            CreateAttributeReq req = mock(CreateAttributeReq.class);
            when(productRepository.findById(99L))
                    .thenThrow(new ResourceNotFoundException("Product not found"));

            assertThatThrownBy(() -> attributeService.createForProduct(req, 99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(attributeRepo);
        }
    }

    @Nested
    @DisplayName("Find By ID")
    class FindById {
        @DisplayName("Should return attribute when exists")
        @Test
        void shouldReturnAttribute_whenExists() {
            Attribute attribute = new Attribute();
            attribute.setId(1L);
            attribute.setName("Color");

            when(attributeRepo.findById(1L)).thenReturn(Optional.of(attribute));

            Attribute result = attributeService.findById(1L);

            assertThat(result).isEqualTo(attribute);
        }
        @DisplayName("Should throw when not found")
        @Test
        void shouldThrow_whenNotFound() {
            when(attributeRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> attributeService.findById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Attribute not found");
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {
        @DisplayName("Should change name and save")
        @Test
        void shouldChangeNameAndSave() {
            Attribute attribute = new Attribute();
            attribute.setId(1L);
            attribute.setName("Old Name");

            UpdateAttributeReq req = mock(UpdateAttributeReq.class);
            when(req.getName()).thenReturn("New Name");
            when(attributeRepo.findById(1L)).thenReturn(Optional.of(attribute));

            attributeService.update(1L, req);

            assertThat(attribute.getName()).isEqualTo("New Name");
            verify(attributeRepo, times(1)).save(attribute);
        }
        @DisplayName("Should throw when attribute not found")
        @Test
        void shouldThrow_whenAttributeNotFound() {
            UpdateAttributeReq req = mock(UpdateAttributeReq.class);
            when(attributeRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> attributeService.update(1L, req))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(attributeRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find By Product ID")
    class FindByProductId {
        @DisplayName("Should return mapped list")
        @Test
        void shouldReturnMappedList() {
            Attribute attribute = new Attribute();
            attribute.setId(1L);
            attribute.setName("Color");

            AttributeRes res = mock(AttributeRes.class);

            when(attributeRepo.findByProductId(1L)).thenReturn(List.of(attribute));
            when(mapper.toAttributeRes(attribute)).thenReturn(res);

            List<AttributeRes> result = attributeService.findByProductId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(res);
        }
        @DisplayName("Should return empty list when no attributes")
        @Test
        void shouldReturnEmptyList_whenNoAttributes() {
            when(attributeRepo.findByProductId(1L)).thenReturn(List.of());

            List<AttributeRes> result = attributeService.findByProductId(1L);

            assertThat(result).isEmpty();
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {
        @DisplayName("Should remove attribute when not in use")
        @Test
        void shouldRemoveAttribute_whenNotInUse() {
            Attribute attribute = new Attribute();
            attribute.setId(1L);

            when(attributeRepo.findById(1L)).thenReturn(Optional.of(attribute));
            when(attributeValueRepo.existsByAttributeId(1L)).thenReturn(false);

            attributeService.delete(1L);

            verify(attributeRepo, times(1)).delete(attribute);
        }
        @DisplayName("Should throw when attribute in use")
        @Test
        void shouldThrow_whenAttributeInUse() {
            Attribute attribute = new Attribute();
            attribute.setId(1L);

            when(attributeRepo.findById(1L)).thenReturn(Optional.of(attribute));
            when(attributeValueRepo.existsByAttributeId(1L)).thenReturn(true);

            assertThatThrownBy(() -> attributeService.delete(1L))
                    .isInstanceOf(ResourceInUseException.class)
                    .hasMessageContaining("still has associated attribute values");

            verify(attributeRepo, never()).delete(any());
        }
        @DisplayName("Should throw when attribute not found")
        @Test
        void shouldThrow_whenAttributeNotFound() {
            when(attributeRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> attributeService.delete(1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(attributeValueRepo);
        }
    }
}