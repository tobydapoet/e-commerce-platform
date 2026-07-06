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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Attribute Value Service tests")
class AttributeValueServiceTest {

    @Mock private AttributeValueRepository attributeValueRepo;
    @Mock private ProductVariantAttributeValueRepository productVariantAttributeValueRepo;
    @Mock private AttributeRepository attributeRepo;
    @Mock private AttributeValueMapper mapper;

    @InjectMocks
    private AttributeValueService attributeValueService;

    private Attribute attribute;

    @BeforeEach
    void setUp() {
        attribute = new Attribute();
        attribute.setId(1L);
        attribute.setName("Color");
    }

    @Nested
    @DisplayName("Find All By ID")
    class FindAllById {
        @DisplayName("Should return attribute values")
        @Test
        void shouldReturnAttributeValues() {
            List<Long> ids = List.of(1L, 2L);
            AttributeValue av1 = new AttributeValue();
            av1.setId(1L);
            AttributeValue av2 = new AttributeValue();
            av2.setId(2L);

            when(attributeValueRepo.findAllById(ids)).thenReturn(List.of(av1, av2));

            List<AttributeValue> result = attributeValueService.findAllById(ids);

            assertThat(result).containsExactly(av1, av2);
        }
        @DisplayName("Should return empty list when none match")
        @Test
        void shouldReturnEmptyList_whenNoneMatch() {
            List<Long> ids = List.of(99L);
            when(attributeValueRepo.findAllById(ids)).thenReturn(List.of());

            List<AttributeValue> result = attributeValueService.findAllById(ids);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Create Batch")
    class CreateBatch {
        @DisplayName("Should create values when valid")
        @Test
        void shouldCreateValues_whenValid() {
            Map<Attribute, List<String>> input = Map.of(
                    attribute, List.of("Red", "Blue")
            );

            when(attributeValueRepo.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            List<AttributeValue> result = attributeValueService.createBatch(input);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(AttributeValue::getValue)
                    .containsExactlyInAnyOrder("Red", "Blue");
            assertThat(result.get(0).getAttribute()).isEqualTo(attribute);
        }
        @DisplayName("Should remove duplicate values")
        @Test
        void shouldRemoveDuplicateValues() {
            Map<Attribute, List<String>> input = Map.of(
                    attribute, List.of("Red", "Red", "Blue")
            );

            ArgumentCaptor<List<AttributeValue>> captor = ArgumentCaptor.forClass(List.class);
            when(attributeValueRepo.saveAll(captor.capture()))
                    .thenAnswer(inv -> inv.getArgument(0));

            attributeValueService.createBatch(input);

            assertThat(captor.getValue()).hasSize(2);
        }
        @DisplayName("Should throw when exceeds max values")
        @Test
        void shouldThrow_whenExceedsMaxValues() {
            List<String> tooManyValues = java.util.stream.IntStream.rangeClosed(1, 21)
                    .mapToObj(String::valueOf)
                    .toList();

            Map<Attribute, List<String>> input = Map.of(attribute, tooManyValues);

            assertThatThrownBy(() -> attributeValueService.createBatch(input))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Color")
                    .hasMessageContaining("at most 20 values");

            verifyNoInteractions(attributeValueRepo);
        }
        @DisplayName("Should create values for multiple attributes")
        @Test
        void shouldCreateValues_forMultipleAttributes() {
            Attribute size = new Attribute();
            size.setId(2L);
            size.setName("Size");

            Map<Attribute, List<String>> input = Map.of(
                    attribute, List.of("Red"),
                    size, List.of("S", "M")
            );

            when(attributeValueRepo.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            List<AttributeValue> result = attributeValueService.createBatch(input);

            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Create For Attribute")
    class CreateForAttribute {
        @DisplayName("Should fetch attribute and create values")
        @Test
        void shouldFetchAttributeAndCreateValues() {
            List<String> values = List.of("Red", "Blue");

            when(attributeRepo.findById(1L)).thenReturn(Optional.of(attribute));
            when(attributeValueRepo.saveAll(anyList()))
                    .thenAnswer(inv -> inv.getArgument(0));

            List<AttributeValue> result = attributeValueService.createForAttribute(1L, values);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getAttribute()).isEqualTo(attribute);
            verify(attributeRepo, times(1)).findById(1L);
        }
        @DisplayName("Should throw when attribute not found")
        @Test
        void shouldThrow_whenAttributeNotFound() {
            List<String> values = List.of("Red");

            when(attributeRepo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    attributeValueService.createForAttribute(99L, values))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Attribute not found");

            verifyNoInteractions(attributeValueRepo);
        }
        @DisplayName("Should throw when values exceed max")
        @Test
        void shouldThrow_whenValuesExceedMax() {
            List<String> tooMany = java.util.stream.IntStream.rangeClosed(1, 25)
                    .mapToObj(String::valueOf)
                    .toList();

            when(attributeRepo.findById(1L)).thenReturn(Optional.of(attribute));

            assertThatThrownBy(() ->
                    attributeValueService.createForAttribute(1L, tooMany))
                    .isInstanceOf(BadRequestException.class);

            verifyNoInteractions(attributeValueRepo);
        }
    }

    @Nested
    @DisplayName("Find By ID")
    class FindById {
        @DisplayName("Should return attribute value when exists")
        @Test
        void shouldReturnAttributeValue_whenExists() {
            AttributeValue av = new AttributeValue();
            av.setId(1L);

            when(attributeValueRepo.findById(1L)).thenReturn(Optional.of(av));

            AttributeValue result = attributeValueService.findById(1L);

            assertThat(result).isEqualTo(av);
        }
        @DisplayName("Should throw when not found")
        @Test
        void shouldThrow_whenNotFound() {
            when(attributeValueRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> attributeValueService.findById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Attribute value not found");
        }
    }

    @Nested
    @DisplayName("Find By Attribute ID")
    class FindByAttributeId {
        @DisplayName("Should return mapped list")
        @Test
        void shouldReturnMappedList() {
            AttributeValue av = new AttributeValue();
            av.setId(1L);
            av.setValue("Red");

            AttributeValueRes res = mock(AttributeValueRes.class);

            when(attributeValueRepo.findByAttributeId(1L)).thenReturn(List.of(av));
            when(mapper.toAttributeValueRes(av)).thenReturn(res);

            List<AttributeValueRes> result = attributeValueService.findByAttributeId(1L);

            assertThat(result).containsExactly(res);
        }
        @DisplayName("Should return empty list when no values")
        @Test
        void shouldReturnEmptyList_whenNoValues() {
            when(attributeValueRepo.findByAttributeId(1L)).thenReturn(List.of());

            List<AttributeValueRes> result = attributeValueService.findByAttributeId(1L);

            assertThat(result).isEmpty();
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("Update")
    class Update {
        @DisplayName("Should change value and save")
        @Test
        void shouldChangeValueAndSave() {
            AttributeValue av = new AttributeValue();
            av.setId(1L);
            av.setValue("Old");

            when(attributeValueRepo.findById(1L)).thenReturn(Optional.of(av));

            attributeValueService.update(1L, "New");

            assertThat(av.getValue()).isEqualTo("New");
            verify(attributeValueRepo, times(1)).save(av);
        }
        @DisplayName("Should throw when not found")
        @Test
        void shouldThrow_whenNotFound() {
            when(attributeValueRepo.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> attributeValueService.update(1L, "New"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(attributeValueRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete {
        @DisplayName("Should delete when not in use")
        @Test
        void shouldDelete_whenNotInUse() {
            when(productVariantAttributeValueRepo.existsByAttributeValueId(1L)).thenReturn(false);

            attributeValueService.delete(1L);

            verify(attributeValueRepo, times(1)).deleteById(1L);
        }
        @DisplayName("Should throw when in use")
        @Test
        void shouldThrow_whenInUse() {
            when(productVariantAttributeValueRepo.existsByAttributeValueId(1L)).thenReturn(true);

            assertThatThrownBy(() -> attributeValueService.delete(1L))
                    .isInstanceOf(ResourceInUseException.class)
                    .hasMessageContaining("associated with one or more product variants");

            verify(attributeValueRepo, never()).deleteById(any());
        }
    }
}
