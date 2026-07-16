package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.CreateProductReq;
import com.example.e_commerce.entity.*;
import com.example.e_commerce.exception.ResourceNotFoundException;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Persistence Service tests")
class ProductPersistenceServiceTest {
    @Mock private ProductRepository productRepo;
    @Mock private ProductVariantAttributeValueService productVariantAttributeService;
    @Mock private ProductVariantService productVariantService;
    @Mock private InventoryService inventoryService;
    @Mock private AttributeService attributeService;
    @Mock private AttributeValueService attributeValueService;
    @Mock private CategoryService categoryService;

    @InjectMocks
    private ProductPersistenceService productPersistenceService;

    private Category category;
    private Attribute colorAttribute;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(5L);

        colorAttribute = new Attribute();
        colorAttribute.setId(1L);
        colorAttribute.setName("Color");
    }


    private CreateProductReq.OptionReq buildOption(String name, String value) {
        CreateProductReq.OptionReq opt = mock(CreateProductReq.OptionReq.class);
        when(opt.getName()).thenReturn(name);
        when(opt.getValue()).thenReturn(value);
        return opt;
    }

    private CreateProductReq.VariantReq buildVariant(
            Integer quantity,
            List<CreateProductReq.OptionReq> options) {

        CreateProductReq.VariantReq variant = mock(CreateProductReq.VariantReq.class);
        lenient().when(variant.getQuantity()).thenReturn(quantity);
        when(variant.getOptions()).thenReturn(options);

        return variant;
    }

    private CreateProductReq buildReq(Long categoryId, List<CreateProductReq.VariantReq> variants) {
        CreateProductReq req = mock(CreateProductReq.class);
        when(req.getName()).thenReturn("T-Shirt");
        when(req.getDescription()).thenReturn("A nice t-shirt");
        when(req.getCategoryId()).thenReturn(categoryId);
        when(req.getVariants()).thenReturn(variants);
        return req;
    }

    @Nested
    @DisplayName("Save Product")
    class SaveProduct {
        @DisplayName("Should save everything correctly")
        @Test
        void shouldSaveEverythingCorrectly() {
            CreateProductReq.OptionReq optRed = buildOption("Color", "Red");
            CreateProductReq.OptionReq optBlue = buildOption("Color", "Blue");

            CreateProductReq.VariantReq variant1Req = buildVariant(
                    10, List.of(optRed));
            CreateProductReq.VariantReq variant2Req = buildVariant(
                    20, List.of(optBlue));

            CreateProductReq req = buildReq(5L, List.of(variant1Req, variant2Req));

            when(categoryService.findById(5L)).thenReturn(category);
            when(productRepo.save(any(Product.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            when(attributeService.create(eq(List.of("Color")), any(Product.class)))
                    .thenReturn(List.of(colorAttribute));

            AttributeValue avRed = new AttributeValue();
            avRed.setAttribute(colorAttribute);
            avRed.setValue("Red");
            AttributeValue avBlue = new AttributeValue();
            avBlue.setAttribute(colorAttribute);
            avBlue.setValue("Blue");

            when(attributeValueService.createBatch(anyMap()))
                    .thenReturn(List.of(avRed, avBlue));

            ProductVariant variant1Entity = new ProductVariant();
            variant1Entity.setId(100L);
            ProductVariant variant2Entity = new ProductVariant();
            variant2Entity.setId(200L);

            when(productVariantService.create(any(Product.class), eq(List.of(variant1Req, variant2Req))))
                    .thenReturn(List.of(variant1Entity, variant2Entity));

            Map<Integer, String> variantImageUrlByIndex = Map.of(1, "url2.jpg");

            Product result = productPersistenceService.saveProduct(
                    req, "thumbnail.jpg", variantImageUrlByIndex);

            assertThat(result.getName()).isEqualTo("T-Shirt");
            assertThat(result.getDescription()).isEqualTo("A nice t-shirt");
            assertThat(result.getThumbnail()).isEqualTo("thumbnail.jpg");
            assertThat(result.getCategory()).isEqualTo(category);

            assertThat(variant1Entity.getImage()).isNull();
            assertThat(variant2Entity.getImage()).isEqualTo("url2.jpg");

            verify(categoryService, times(1)).findById(5L);
            verify(productRepo, times(1)).save(any(Product.class));
            verify(attributeService, times(1))
                    .create(eq(List.of("Color")), any(Product.class));
            verify(productVariantService, times(1))
                    .create(any(Product.class), eq(List.of(variant1Req, variant2Req)));

            ArgumentCaptor<List<Inventory>> inventoryCaptor = ArgumentCaptor.forClass(List.class);
            verify(inventoryService, times(1)).createBatch(inventoryCaptor.capture());
            List<Inventory> inventories = inventoryCaptor.getValue();
            assertThat(inventories).hasSize(2);
            assertThat(inventories.get(0).getProductVariant()).isEqualTo(variant1Entity);
            assertThat(inventories.get(0).getQuantity()).isEqualTo(10);
            assertThat(inventories.get(1).getProductVariant()).isEqualTo(variant2Entity);
            assertThat(inventories.get(1).getQuantity()).isEqualTo(20);

            ArgumentCaptor<List<ProductVariantAttributeValue>> pvavCaptor =
                    ArgumentCaptor.forClass(List.class);
            verify(productVariantAttributeService, times(1)).createBatch(pvavCaptor.capture());
            List<ProductVariantAttributeValue> pvavs = pvavCaptor.getValue();
            assertThat(pvavs).hasSize(2);
            assertThat(pvavs.get(0).getProductVariant()).isEqualTo(variant1Entity);
            assertThat(pvavs.get(0).getAttributeValue()).isEqualTo(avRed);
            assertThat(pvavs.get(1).getProductVariant()).isEqualTo(variant2Entity);
            assertThat(pvavs.get(1).getAttributeValue()).isEqualTo(avBlue);
        }
        @DisplayName("Should extract distinct attribute names")
        @Test
        void shouldExtractDistinctAttributeNames() {
            CreateProductReq.OptionReq colorRed = buildOption("Color", "Red");
            CreateProductReq.OptionReq sizeM = buildOption("Size", "M");
            CreateProductReq.OptionReq colorBlue = buildOption("Color", "Blue");

            CreateProductReq.VariantReq variant1 = buildVariant(
                    5, List.of(colorRed, sizeM));
            CreateProductReq.VariantReq variant2 = buildVariant(
                    5, List.of(colorBlue));

            CreateProductReq req = buildReq(5L, List.of(variant1, variant2));

            when(categoryService.findById(5L)).thenReturn(category);
            when(productRepo.save(any(Product.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Attribute sizeAttribute = new Attribute();
            sizeAttribute.setId(2L);
            sizeAttribute.setName("Size");

            ArgumentCaptor<List<String>> namesCaptor = ArgumentCaptor.forClass(List.class);
            when(attributeService.create(namesCaptor.capture(), any(Product.class)))
                    .thenReturn(List.of(colorAttribute, sizeAttribute));

            AttributeValue avRed = new AttributeValue();
            avRed.setAttribute(colorAttribute);
            avRed.setValue("Red");
            AttributeValue avM = new AttributeValue();
            avM.setAttribute(sizeAttribute);
            avM.setValue("M");
            AttributeValue avBlue = new AttributeValue();
            avBlue.setAttribute(colorAttribute);
            avBlue.setValue("Blue");

            when(attributeValueService.createBatch(anyMap()))
                    .thenReturn(List.of(avRed, avM, avBlue));

            ProductVariant v1Entity = new ProductVariant();
            ProductVariant v2Entity = new ProductVariant();
            when(productVariantService.create(any(Product.class), anyList()))
                    .thenReturn(List.of(v1Entity, v2Entity));

            productPersistenceService.saveProduct(req, "thumb.jpg", Map.of());

            assertThat(namesCaptor.getValue()).containsExactlyInAnyOrder("Color", "Size");
        }
        @DisplayName("Should throw when attribute value missing")
        @Test
        void shouldThrow_whenAttributeValueMissing() {
            CreateProductReq.OptionReq optRed = buildOption("Color", "Red");
            CreateProductReq.VariantReq variantReq = buildVariant(
                    10, List.of(optRed));

            CreateProductReq req = buildReq(5L, List.of(variantReq));

            when(categoryService.findById(5L)).thenReturn(category);
            when(productRepo.save(any(Product.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(attributeService.create(anyList(), any(Product.class)))
                    .thenReturn(List.of(colorAttribute));

            AttributeValue wrongValue = new AttributeValue();
            wrongValue.setAttribute(colorAttribute);
            wrongValue.setValue("Green");

            when(attributeValueService.createBatch(anyMap()))
                    .thenReturn(List.of(wrongValue));

            ProductVariant variantEntity = new ProductVariant();
            when(productVariantService.create(any(Product.class), anyList()))
                    .thenReturn(List.of(variantEntity));

            assertThatThrownBy(() ->
                    productPersistenceService.saveProduct(req, "thumb.jpg", Map.of()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Can't find attribute value for: Color:Red");

            verifyNoInteractions(productVariantAttributeService);
            verifyNoInteractions(inventoryService);
        }
        @DisplayName("Should not set image when no images provided")
        @Test
        void shouldNotSetImage_whenNoImagesProvided() {
            CreateProductReq.OptionReq optRed = buildOption("Color", "Red");
            CreateProductReq.VariantReq variantReq = buildVariant(
                    10, List.of(optRed));

            CreateProductReq req = buildReq(5L, List.of(variantReq));

            when(categoryService.findById(5L)).thenReturn(category);
            when(productRepo.save(any(Product.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(attributeService.create(anyList(), any(Product.class)))
                    .thenReturn(List.of(colorAttribute));

            AttributeValue avRed = new AttributeValue();
            avRed.setAttribute(colorAttribute);
            avRed.setValue("Red");
            when(attributeValueService.createBatch(anyMap()))
                    .thenReturn(List.of(avRed));

            ProductVariant variantEntity = new ProductVariant();
            when(productVariantService.create(any(Product.class), anyList()))
                    .thenReturn(List.of(variantEntity));

            productPersistenceService.saveProduct(req, "thumb.jpg", Map.of());

            assertThat(variantEntity.getImage()).isNull();
        }
        @DisplayName("Should return saved product")
        @Test
        void shouldReturnSavedProduct() {
            CreateProductReq.OptionReq optRed = buildOption("Color", "Red");
            CreateProductReq.VariantReq variantReq = buildVariant(
                    10, List.of(optRed));
            CreateProductReq req = buildReq(5L, List.of(variantReq));

            when(categoryService.findById(5L)).thenReturn(category);

            Product savedProduct = new Product();
            savedProduct.setId(999L);
            when(productRepo.save(any(Product.class))).thenReturn(savedProduct);

            when(attributeService.create(anyList(), any(Product.class)))
                    .thenReturn(List.of(colorAttribute));

            AttributeValue avRed = new AttributeValue();
            avRed.setAttribute(colorAttribute);
            avRed.setValue("Red");
            when(attributeValueService.createBatch(anyMap())).thenReturn(List.of(avRed));

            ProductVariant variantEntity = new ProductVariant();
            when(productVariantService.create(any(Product.class), anyList()))
                    .thenReturn(List.of(variantEntity));

            Product result = productPersistenceService.saveProduct(req, "thumb.jpg", Map.of());

            assertThat(result.getId()).isEqualTo(999L);
        }
    }
}