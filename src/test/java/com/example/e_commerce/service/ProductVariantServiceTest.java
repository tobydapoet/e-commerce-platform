package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.CreateProductReq;
import com.example.e_commerce.dto.response.ProductVariantRes;
import com.example.e_commerce.entity.AttributeValue;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.entity.ProductVariant;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.ProductVariantMapper;
import com.example.e_commerce.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Variant Service tests")
class ProductVariantServiceTest {

    @Mock private ProductVariantRepository productVariantRepo;
    @Mock private AttributeValueService attributeValueService;
    @Mock private UploadService uploadService;
    @Mock private ProductVariantMapper mapper;

    @InjectMocks
    private ProductVariantService productVariantService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
    }

    private CreateProductReq.VariantReq buildVariantReq(BigDecimal price, MultipartFile image) {
        CreateProductReq.VariantReq req = mock(CreateProductReq.VariantReq.class);
        when(req.getPrice()).thenReturn(price);
        when(req.getImage()).thenReturn(image);
        return req;
    }

    @Nested
    @DisplayName("Find By ID")
    class FindById {
        @DisplayName("Should return variant when exists")
        @Test
        void shouldReturnVariant_whenExists() {
            ProductVariant variant = new ProductVariant();
            variant.setId(1L);

            when(productVariantRepo.findById(1L)).thenReturn(Optional.of(variant));

            ProductVariant result = productVariantService.findById(1L);

            assertThat(result).isEqualTo(variant);
        }
        @DisplayName("Should throw when not found")
        @Test
        void shouldThrow_whenNotFound() {
            when(productVariantRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productVariantService.findById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product Variant Not Found");
        }
    }

    @Nested
    @DisplayName("Create")
    class Create {
        @DisplayName("Should create variants when all have images")
        @Test
        void shouldCreateVariants_whenAllHaveImages() {
            MultipartFile img1 = mock(MultipartFile.class);
            MultipartFile img2 = mock(MultipartFile.class);
            when(img1.isEmpty()).thenReturn(false);
            when(img2.isEmpty()).thenReturn(false);

            List<CreateProductReq.VariantReq> requests = List.of(
                    buildVariantReq(BigDecimal.TEN, img1),
                    buildVariantReq(BigDecimal.valueOf(20), img2)
            );

            when(uploadService.uploadMultiple(anyList(), eq("variant_image")))
                    .thenReturn(List.of("url1.jpg", "url2.jpg"));
            when(productVariantRepo.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            List<ProductVariant> result = productVariantService.create(product, requests);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getImage()).isEqualTo("url1.jpg");
            assertThat(result.get(1).getImage()).isEqualTo("url2.jpg");
            assertThat(result.get(0).getProduct()).isEqualTo(product);
            assertThat(result.get(0).getActive()).isTrue();
            assertThat(result.get(0).getSku()).startsWith("SKU-");

            verify(uploadService, times(1)).uploadMultiple(anyList(), eq("variant_image"));
        }
        @DisplayName("Should skip null or empty images and keep correct order")
        @Test
        void shouldSkipNullOrEmptyImages_andKeepCorrectOrder() {
            MultipartFile img1 = mock(MultipartFile.class);
            MultipartFile emptyImg = mock(MultipartFile.class);
            when(img1.isEmpty()).thenReturn(false);
            when(emptyImg.isEmpty()).thenReturn(true);

            List<CreateProductReq.VariantReq> requests = List.of(
                    buildVariantReq(BigDecimal.TEN, null),
                    buildVariantReq(BigDecimal.valueOf(20), img1),
                    buildVariantReq(BigDecimal.valueOf(30), emptyImg)
            );

            ArgumentCaptor<List<MultipartFile>> uploadCaptor = ArgumentCaptor.forClass(List.class);
            when(uploadService.uploadMultiple(uploadCaptor.capture(), eq("variant_image")))
                    .thenReturn(List.of("url-for-index-1.jpg"));
            when(productVariantRepo.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            List<ProductVariant> result = productVariantService.create(product, requests);

            assertThat(uploadCaptor.getValue()).hasSize(1).containsExactly(img1);

            assertThat(result.get(0).getImage()).isNull();
            assertThat(result.get(1).getImage()).isEqualTo("url-for-index-1.jpg");
            assertThat(result.get(2).getImage()).isNull();
        }
        @DisplayName("Should not call upload when no images present")
        @Test
        void shouldNotCallUpload_whenNoImagesPresent() {
            List<CreateProductReq.VariantReq> requests = List.of(
                    buildVariantReq(BigDecimal.TEN, null)
            );

            when(uploadService.uploadMultiple(List.of(), "variant_image"))
                    .thenReturn(List.of());
            when(productVariantRepo.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            List<ProductVariant> result = productVariantService.create(product, requests);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getImage()).isNull();
            verify(uploadService, times(1)).uploadMultiple(List.of(), "variant_image");
        }
        @DisplayName("Should return empty list when requests empty")
        @Test
        void shouldReturnEmptyList_whenRequestsEmpty() {
            when(uploadService.uploadMultiple(List.of(), "variant_image"))
                    .thenReturn(List.of());
            when(productVariantRepo.saveAll(anyList())).thenReturn(List.of());

            List<ProductVariant> result = productVariantService.create(product, List.of());

            assertThat(result).isEmpty();
        }
        @DisplayName("Should generate unique skus")
        @Test
        void shouldGenerateUniqueSkus() {
            MultipartFile img = mock(MultipartFile.class);
            when(img.isEmpty()).thenReturn(false);

            List<CreateProductReq.VariantReq> requests = List.of(
                    buildVariantReq(BigDecimal.TEN, img),
                    buildVariantReq(BigDecimal.TEN, img)
            );

            when(uploadService.uploadMultiple(anyList(), eq("variant_image")))
                    .thenReturn(List.of("url1.jpg", "url2.jpg"));
            when(productVariantRepo.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            List<ProductVariant> result = productVariantService.create(product, requests);

            assertThat(result.get(0).getSku()).isNotEqualTo(result.get(1).getSku());
        }
    }

    @Nested
    @DisplayName("Get Variant By Attribute Value Ids")
    class GetVariantByAttributeValueIds {
        @DisplayName("Should return variant when found")
        @Test
        void shouldReturnVariant_whenFound() {
            List<Long> ids = List.of(1L, 2L);

            AttributeValue av1 = new AttributeValue();
            av1.setId(1L);
            AttributeValue av2 = new AttributeValue();
            av2.setId(2L);

            ProductVariant variant = new ProductVariant();
            variant.setId(10L);

            ProductVariantRes res = mock(ProductVariantRes.class);

            when(attributeValueService.findAllById(ids)).thenReturn(List.of(av1, av2));
            when(productVariantRepo.findByAttributeValueIds(anyList(), eq(2L)))
                    .thenReturn(Optional.of(variant));
            when(mapper.toProductVariantRes(variant)).thenReturn(res);

            ProductVariantRes result = productVariantService.getVariantByAttributeValueIds(ids);

            assertThat(result).isEqualTo(res);
        }
        @DisplayName("Should throw when ids null")
        @Test
        void shouldThrow_whenIdsNull() {
            assertThatThrownBy(() -> productVariantService.getVariantByAttributeValueIds(null))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(attributeValueService, productVariantRepo);
        }
        @DisplayName("Should throw when ids empty")
        @Test
        void shouldThrow_whenIdsEmpty() {
            assertThatThrownBy(() -> productVariantService.getVariantByAttributeValueIds(List.of()))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(attributeValueService, productVariantRepo);
        }
        @DisplayName("Should throw when some ids not found")
        @Test
        void shouldThrow_whenSomeIdsNotFound() {
            List<Long> ids = List.of(1L, 2L, 3L);

            AttributeValue av1 = new AttributeValue();
            av1.setId(1L);
            AttributeValue av2 = new AttributeValue();
            av2.setId(2L);

            when(attributeValueService.findAllById(ids)).thenReturn(List.of(av1, av2));

            assertThatThrownBy(() -> productVariantService.getVariantByAttributeValueIds(ids))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("One or more attribute values not found");

            verifyNoInteractions(productVariantRepo);
        }
        @DisplayName("Should throw when variant not found")
        @Test
        void shouldThrow_whenVariantNotFound() {
            List<Long> ids = List.of(1L);

            AttributeValue av1 = new AttributeValue();
            av1.setId(1L);

            when(attributeValueService.findAllById(ids)).thenReturn(List.of(av1));
            when(productVariantRepo.findByAttributeValueIds(anyList(), eq(1L)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> productVariantService.getVariantByAttributeValueIds(ids))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Variant not found");

            verifyNoInteractions(mapper);
        }
    }
}
