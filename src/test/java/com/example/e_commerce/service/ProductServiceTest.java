package com.example.e_commerce.service;

import com.example.e_commerce.constant.ProductStatus;
import com.example.e_commerce.dto.request.CreateProductReq;
import com.example.e_commerce.dto.request.UpdateProductReq;
import com.example.e_commerce.dto.request.UpdateProductStatusReq;
import com.example.e_commerce.dto.response.ProductDetailRes;
import com.example.e_commerce.dto.response.ProductPriceRangeProjection;
import com.example.e_commerce.dto.response.ProductRes;
import com.example.e_commerce.entity.Category;
import com.example.e_commerce.entity.Product;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.ProductMapper;
import com.example.e_commerce.repository.ProductRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service tests")
class ProductServiceTest {

    @Mock private UploadService uploadService;
    @Mock private ProductPersistenceService productPersistenceService;
    @Mock private ProductVariantRepository productVariantRepo;
    @Mock private ProductRepository productRepo;
    @Mock private ProductMapper mapper;
    @Mock private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
    }

    private CreateProductReq.VariantReq buildVariant(MultipartFile image) {
        CreateProductReq.VariantReq variant = mock(CreateProductReq.VariantReq.class);
        when(variant.getImage()).thenReturn(image);
        return variant;
    }

    @Nested
    @DisplayName("Create")
    class Create {
        @DisplayName("Should upload images and save product")
        @Test
        void shouldUploadImagesAndSaveProduct() {
            MultipartFile thumbnail = mock(MultipartFile.class);
            MultipartFile variantImg1 = mock(MultipartFile.class);
            when(variantImg1.isEmpty()).thenReturn(false);

            CreateProductReq.VariantReq variant1 = buildVariant(null);
            CreateProductReq.VariantReq variant2 = buildVariant(variantImg1);

            CreateProductReq req = mock(CreateProductReq.class);
            when(req.getThumbnail()).thenReturn(thumbnail);
            when(req.getVariants()).thenReturn(List.of(variant1, variant2));

            when(uploadService.upload(thumbnail, "thumbnail_product"))
                    .thenReturn("thumbnail-url.jpg");
            when(uploadService.uploadMultiple(anyList(), eq("variant_image")))
                    .thenReturn(List.of("variant-url.jpg"));

            when(productPersistenceService.saveProduct(eq(req), eq("thumbnail-url.jpg"), anyMap()))
                    .thenReturn(product);

            Product result = productService.create(req);

            assertThat(result).isEqualTo(product);

            ArgumentCaptor<Map<Integer, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(productPersistenceService, times(1))
                    .saveProduct(eq(req), eq("thumbnail-url.jpg"), mapCaptor.capture());

            assertThat(mapCaptor.getValue()).containsEntry(1, "variant-url.jpg");
            assertThat(mapCaptor.getValue()).doesNotContainKey(0);
        }
        @DisplayName("Should not upload variant images when none provided")
        @Test
        void shouldNotUploadVariantImages_whenNoneProvided() {
            MultipartFile thumbnail = mock(MultipartFile.class);
            CreateProductReq.VariantReq variant1 = buildVariant(null);

            CreateProductReq req = mock(CreateProductReq.class);
            when(req.getThumbnail()).thenReturn(thumbnail);
            when(req.getVariants()).thenReturn(List.of(variant1));

            when(uploadService.upload(thumbnail, "thumbnail_product")).thenReturn("thumb.jpg");
            when(uploadService.uploadMultiple(List.of(), "variant_image"))
                    .thenReturn(List.of());
            when(productPersistenceService.saveProduct(eq(req), eq("thumb.jpg"), anyMap()))
                    .thenReturn(product);

            productService.create(req);

            ArgumentCaptor<Map<Integer, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            verify(productPersistenceService)
                    .saveProduct(eq(req), eq("thumb.jpg"), mapCaptor.capture());
            assertThat(mapCaptor.getValue()).isEmpty();
        }
        @DisplayName("Should skip empty images")
        @Test
        void shouldSkipEmptyImages() {
            MultipartFile thumbnail = mock(MultipartFile.class);
            MultipartFile emptyImg = mock(MultipartFile.class);
            when(emptyImg.isEmpty()).thenReturn(true);
            MultipartFile validImg = mock(MultipartFile.class);
            when(validImg.isEmpty()).thenReturn(false);

            CreateProductReq.VariantReq variant1 = buildVariant(emptyImg);
            CreateProductReq.VariantReq variant2 = buildVariant(validImg);

            CreateProductReq req = mock(CreateProductReq.class);
            when(req.getThumbnail()).thenReturn(thumbnail);
            when(req.getVariants()).thenReturn(List.of(variant1, variant2));

            when(uploadService.upload(thumbnail, "thumbnail_product")).thenReturn("thumb.jpg");

            ArgumentCaptor<List<MultipartFile>> uploadCaptor = ArgumentCaptor.forClass(List.class);
            when(uploadService.uploadMultiple(uploadCaptor.capture(), eq("variant_image")))
                    .thenReturn(List.of("url-for-index-1.jpg"));
            when(productPersistenceService.saveProduct(eq(req), eq("thumb.jpg"), anyMap()))
                    .thenReturn(product);

            productService.create(req);

            assertThat(uploadCaptor.getValue()).containsExactly(validImg);
        }
    }

    @Nested
    @DisplayName("Find By ID")
    class FindById {
        @DisplayName("Should return product when exists")
        @Test
        void shouldReturnProduct_whenExists() {
            when(productRepo.findById(1L)).thenReturn(Optional.of(product));

            Product result = productService.findById(1L);

            assertThat(result).isEqualTo(product);
        }
        @DisplayName("Should throw when not found")
        @Test
        void shouldThrow_whenNotFound() {
            when(productRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }
    }

    @Nested
    @DisplayName("Find By ID With Price")
    class FindByIdWithPrice {
        @DisplayName("Should return detail with price range")
        @Test
        void shouldReturnDetailWithPriceRange() {
            when(productRepo.findById(1L)).thenReturn(Optional.of(product));

            ProductDetailRes base = mock(ProductDetailRes.class);
            when(base.id()).thenReturn(1L);
            when(base.name()).thenReturn("T-Shirt");
            when(base.thumbnail()).thenReturn("thumb.jpg");
            when(base.description()).thenReturn("Nice shirt");
            when(base.attributes()).thenReturn(List.of());
            when(mapper.toProductDetailRes(product)).thenReturn(base);

            ProductPriceRangeProjection range = mock(ProductPriceRangeProjection.class);
            when(range.getMinPrice()).thenReturn(BigDecimal.TEN);
            when(range.getMaxPrice()).thenReturn(BigDecimal.valueOf(100));
            when(productVariantRepo.findPriceRangeByProductIds(List.of(1L)))
                    .thenReturn(List.of(range));

            ProductDetailRes result = productService.findByIdWithPrice(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("T-Shirt");
            assertThat(result.minPrice()).isEqualTo(BigDecimal.TEN);
            assertThat(result.maxPrice()).isEqualTo(BigDecimal.valueOf(100));
        }
        @DisplayName("Should return zero price when no variants")
        @Test
        void shouldReturnZeroPrice_whenNoVariants() {
            when(productRepo.findById(1L)).thenReturn(Optional.of(product));

            ProductDetailRes base = mock(ProductDetailRes.class);
            when(base.id()).thenReturn(1L);
            when(base.name()).thenReturn("T-Shirt");
            when(base.thumbnail()).thenReturn("thumb.jpg");
            when(base.description()).thenReturn("Nice shirt");
            when(base.attributes()).thenReturn(List.of());
            when(mapper.toProductDetailRes(product)).thenReturn(base);

            when(productVariantRepo.findPriceRangeByProductIds(List.of(1L)))
                    .thenReturn(List.of());

            ProductDetailRes result = productService.findByIdWithPrice(1L);

            assertThat(result.minPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.maxPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
        @DisplayName("Should throw when product not found")
        @Test
        void shouldThrow_whenProductNotFound() {
            when(productRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findByIdWithPrice(1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(productVariantRepo, mapper);
        }
    }

    @Nested
    @DisplayName("Get By Page And Search")
    class GetByPageAndSearch {
        @DisplayName("Get by page should map with price range")
        @Test
        void getByPage_shouldMapWithPriceRange() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            when(productRepo.getByPage(
                    List.of(ProductStatus.INACTIVE, ProductStatus.DELETED), pageable))
                    .thenReturn(productPage);

            ProductRes base = mock(ProductRes.class);
            when(base.id()).thenReturn(1L);
            when(base.name()).thenReturn("T-Shirt");
            when(base.thumbnail()).thenReturn("thumb.jpg");
            when(base.status()).thenReturn(ProductStatus.ACTIVE);
            when(base.soldCount()).thenReturn(5);
            when(mapper.toProductRes(product)).thenReturn(base);

            ProductPriceRangeProjection range = mock(ProductPriceRangeProjection.class);
            when(range.getProductId()).thenReturn(1L);
            when(range.getMinPrice()).thenReturn(BigDecimal.ONE);
            when(range.getMaxPrice()).thenReturn(BigDecimal.TEN);
            when(productVariantRepo.findPriceRangeByProductIds(List.of(1L)))
                    .thenReturn(List.of(range));

            Page<ProductRes> result = productService.getByPage(pageable);

            assertThat(result.getContent()).hasSize(1);
            ProductRes res = result.getContent().get(0);
            assertThat(res.id()).isEqualTo(1L);
            assertThat(res.minPrice()).isEqualTo(BigDecimal.ONE);
            assertThat(res.maxPrice()).isEqualTo(BigDecimal.TEN);
        }
        @DisplayName("Search should return empty page when no results")
        @Test
        void search_shouldReturnEmptyPage_whenNoResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(productRepo.search(
                    List.of(ProductStatus.INACTIVE, ProductStatus.DELETED), "abc", pageable))
                    .thenReturn(emptyPage);

            Page<ProductRes> result = productService.search("abc", pageable);

            assertThat(result.getContent()).isEmpty();
            verifyNoInteractions(productVariantRepo);
        }
        @DisplayName("Should return zero price when product missing in range map")
        @Test
        void shouldReturnZeroPrice_whenProductMissingInRangeMap() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

            when(productRepo.getByPage(anyList(), eq(pageable))).thenReturn(productPage);

            ProductRes base = mock(ProductRes.class);
            when(base.id()).thenReturn(1L);
            when(base.name()).thenReturn("T-Shirt");
            when(base.thumbnail()).thenReturn("thumb.jpg");
            when(base.status()).thenReturn(ProductStatus.ACTIVE);
            when(base.soldCount()).thenReturn(0);
            when(mapper.toProductRes(product)).thenReturn(base);

            when(productVariantRepo.findPriceRangeByProductIds(List.of(1L)))
                    .thenReturn(List.of());

            Page<ProductRes> result = productService.getByPage(pageable);

            ProductRes res = result.getContent().get(0);
            assertThat(res.minPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(res.maxPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {
        @DisplayName("Should update only provided fields")
        @Test
        void shouldUpdateOnlyProvidedFields() {
            product.setName("Old name");
            product.setDescription("Old desc");

            UpdateProductReq req = mock(UpdateProductReq.class);
            when(req.getName()).thenReturn("New name");
            when(req.getDescription()).thenReturn(null);
            when(req.getThumbnail()).thenReturn(null);
            when(req.getCategoryId()).thenReturn(null);

            when(productRepo.findById(1L)).thenReturn(Optional.of(product));

            productService.update(1L, req);

            assertThat(product.getName()).isEqualTo("New name");
            assertThat(product.getDescription()).isEqualTo("Old desc");
            verify(productRepo, times(1)).save(product);
            verifyNoInteractions(uploadService, categoryService);
        }
        @DisplayName("Should replace thumbnail when provided")
        @Test
        void shouldReplaceThumbnail_whenProvided() {
            product.setThumbnail("old-thumb.jpg");

            MultipartFile newThumbnail = mock(MultipartFile.class);
            UpdateProductReq req = mock(UpdateProductReq.class);
            when(req.getName()).thenReturn(null);
            when(req.getDescription()).thenReturn(null);
            when(req.getThumbnail()).thenReturn(newThumbnail);
            when(req.getCategoryId()).thenReturn(null);

            when(productRepo.findById(1L)).thenReturn(Optional.of(product));
            when(uploadService.upload(newThumbnail, "old-thumb.jpg"))
                    .thenReturn("new-thumb.jpg");

            productService.update(1L, req);

            verify(uploadService, times(1)).delete("old-thumb.jpg");
            verify(uploadService, times(1)).upload(newThumbnail, "old-thumb.jpg");
            assertThat(product.getThumbnail()).isEqualTo("new-thumb.jpg");
        }
        @DisplayName("Should not delete old thumbnail when none exists")
        @Test
        void shouldNotDeleteOldThumbnail_whenNoneExists() {
            product.setThumbnail(null);

            MultipartFile newThumbnail = mock(MultipartFile.class);
            UpdateProductReq req = mock(UpdateProductReq.class);
            when(req.getThumbnail()).thenReturn(newThumbnail);

            when(productRepo.findById(1L)).thenReturn(Optional.of(product));
            when(uploadService.upload(eq(newThumbnail), isNull()))
                    .thenReturn("new-thumb.jpg");

            productService.update(1L, req);

            verify(uploadService, never()).delete(anyString());
            assertThat(product.getThumbnail()).isEqualTo("new-thumb.jpg");
        }
        @DisplayName("Should update category when provided")
        @Test
        void shouldUpdateCategory_whenProvided() {
            UpdateProductReq req = mock(UpdateProductReq.class);
            when(req.getCategoryId()).thenReturn(9L);

            Category newCategory = new Category();
            newCategory.setId(9L);

            when(productRepo.findById(1L)).thenReturn(Optional.of(product));
            when(categoryService.findById(9L)).thenReturn(newCategory);

            productService.update(1L, req);

            assertThat(product.getCategory()).isEqualTo(newCategory);
            verify(categoryService, times(1)).findById(9L);
        }
        @DisplayName("Should throw when product not found")
        @Test
        void shouldThrow_whenProductNotFound() {
            UpdateProductReq req = mock(UpdateProductReq.class);
            when(productRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(1L, req))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(productRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Status")
    class UpdateStatus {
        @DisplayName("Should update status and save")
        @Test
        void shouldUpdateStatusAndSave() {
            UpdateProductStatusReq req = mock(UpdateProductStatusReq.class);
            when(req.getStatus()).thenReturn(ProductStatus.INACTIVE);

            when(productRepo.findById(1L)).thenReturn(Optional.of(product));

            productService.updateStatus(1L, req);

            assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
            verify(productRepo, times(1)).save(product);
        }
        @DisplayName("Should throw when product not found")
        @Test
        void shouldThrow_whenProductNotFound() {
            UpdateProductStatusReq req = mock(UpdateProductStatusReq.class);
            when(productRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateStatus(1L, req))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(productRepo, never()).save(any());
        }
    }
}
