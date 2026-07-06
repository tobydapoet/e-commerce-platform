package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.CreateAddressReq;
import com.example.e_commerce.dto.request.UpdateAddressReq;
import com.example.e_commerce.dto.response.AddressRes;
import com.example.e_commerce.entity.Address;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.AddressMapper;
import com.example.e_commerce.repository.AddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@DisplayName("Address Service tests")
public class AddressServiceTest {
    @Mock
    private AddressRepository addressRepo;

    @Mock
    private AddressMapper mapper;

    @InjectMocks
    private AddressService addressService;

    @Nested
    @DisplayName("Create")
    class CreateTests {
        @DisplayName("Create should save address successfully")
        @Test
        void create_ShouldSaveAddressSuccessfully() {
            UUID userId = UUID.randomUUID();
    
            User user = new User();
            user.setId(userId);
    
            CreateAddressReq req = new CreateAddressReq();
            req.setName("Nguyen Van A");
            req.setPhone("0123456789");
            req.setFullAddress("Ha Noi");
    
            when(addressRepo.save(any(Address.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
    
            Address result = addressService.create(req, user);
    
            verify(addressRepo).clearDefaultAddress(userId);
            verify(addressRepo).save(any(Address.class));
    
            assertEquals("Nguyen Van A", result.getName());
            assertEquals("0123456789", result.getPhone());
            assertEquals("Ha Noi", result.getFullAddress());
            assertTrue(result.getIsDefault());
            assertEquals(user, result.getUser());
        }
    }

    @Nested
    @DisplayName("Delete")
    class DeleteTests {
        @DisplayName("Delete should delete when owner")
        @Test
        void delete_ShouldDelete_WhenOwner() {
            UUID userId = UUID.randomUUID();
    
            User user = new User();
            user.setId(userId);
    
            Address address = new Address();
            address.setUser(user);
    
            when(addressRepo.findById(1L))
                    .thenReturn(Optional.of(address));
    
            addressService.delete(userId, 1L);
    
            verify(addressRepo).delete(address);
        }
        @DisplayName("Delete should throw when not found")
        @Test
        void delete_ShouldThrow_WhenNotFound() {
            UUID userId = UUID.randomUUID();

            when(addressRepo.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> addressService.delete(userId, 1L)
            );

            verify(addressRepo, never()).delete(any());
        }
        @DisplayName("Delete should throw when not owner")
        @Test
        void delete_ShouldThrow_WhenNotOwner() {
            UUID userId = UUID.randomUUID();
            User owner = new User();
            owner.setId(UUID.randomUUID());
    
            Address address = new Address();
            address.setUser(owner);
    
            when(addressRepo.findById(1L))
                    .thenReturn(Optional.of(address));
    
            assertThrows(ForbiddenException.class,
                    () -> addressService.delete(userId , 1L));
    
            verify(addressRepo, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Update")
    class UpdateTests {
        @DisplayName("Update should update successfully")
        @Test
        void update_ShouldUpdateSuccessfully() {
    
            UUID userId = UUID.randomUUID();
    
            User user = new User();
            user.setId(userId);
    
            Address address = new Address();
            address.setUser(user);
    
            UpdateAddressReq req = new UpdateAddressReq();
            req.setName("New Name");
            req.setPhone("0999");
            req.setFullAddress("Da Nang");
    
            when(addressRepo.findById(1L))
                    .thenReturn(Optional.of(address));
    
            addressService.update(userId, 1L, req);
    
            verify(addressRepo).save(address);
    
            assertEquals("New Name", address.getName());
            assertEquals("0999", address.getPhone());
            assertEquals("Da Nang", address.getFullAddress());
        }
        @DisplayName("Update should throw resource not found exception when address not found")
        @Test
        void update_ShouldThrowResourceNotFoundException_WhenAddressNotFound() {
            UUID userId = UUID.randomUUID();
    
            UpdateAddressReq req = new UpdateAddressReq();
            req.setName("New Name");
    
            when(addressRepo.findById(1L))
                    .thenReturn(Optional.empty());
    
            assertThrows(ResourceNotFoundException.class,
                    () -> addressService.update(userId, 1L, req));
    
            verify(addressRepo, never()).save(any(Address.class));
        }
        @DisplayName("Update should throw forbidden exception when user is not owner")
        @Test
        void update_ShouldThrowForbiddenException_WhenUserIsNotOwner() {
            UUID ownerId = UUID.randomUUID();
            UUID anotherUserId = UUID.randomUUID();
    
            User owner = new User();
            owner.setId(ownerId);
    
            Address address = new Address();
            address.setUser(owner);
    
            UpdateAddressReq req = new UpdateAddressReq();
            req.setName("New Name");
    
            when(addressRepo.findById(1L))
                    .thenReturn(Optional.of(address));
    
            assertThrows(ForbiddenException.class,
                    () -> addressService.update(anotherUserId, 1L, req));
    
            verify(addressRepo, never()).save(any(Address.class));
        }
    }

    @Nested
    @DisplayName("Get By ID")
    class GetByIdTests {
        @DisplayName("Get by ID should return response")
        @Test
        void getById_ShouldReturnResponse() {
    
            UUID userId = UUID.randomUUID();
    
            User user = new User();
            user.setId(userId);
    
            Address address = new Address();
            address.setUser(user);
    
            AddressRes response =
                    new AddressRes(
                            1L,
                            "A",
                            null,
                            "0123",
                            "Ha Noi",
                            true,
                            null,
                            null);
    
            when(addressRepo.findById(1L))
                    .thenReturn(Optional.of(address));
    
            when(mapper.toAddressRes(address))
                    .thenReturn(response);
    
            AddressRes result = addressService.getById(userId, 1L);
    
            assertEquals(response, result);
    
            verify(mapper).toAddressRes(address);
        }
    }

    @Nested
    @DisplayName("Update Default")
    class UpdateDefaultTests {
        @DisplayName("Update default should update successfully")
        @Test
        void updateDefault_ShouldUpdateSuccessfully() {
    
            UUID userId = UUID.randomUUID();
    
            User user = new User();
            user.setId(userId);
    
            Address address = new Address();
            address.setUser(user);
    
            when(addressRepo.findById(1L))
                    .thenReturn(Optional.of(address));
    
            addressService.updateDefault(user, 1L);
    
            verify(addressRepo).clearDefaultAddress(userId);
    
            assertTrue(address.getIsDefault());
        }
    }

    @Nested
    @DisplayName("Find By User ID")
    class FindByUserIdTests {
        @DisplayName("Find by user ID should return page")
        @Test
        void findByUserId_ShouldReturnPage() {
    
            UUID userId = UUID.randomUUID();
    
            Address address = new Address();
    
            AddressRes dto =
                    new AddressRes(
                            1L,
                            "A",
                            null,
                            "0123",
                            "HN",
                            true,
                            null,
                            null);
    
            Page<Address> page =
                    new PageImpl<>(List.of(address));
    
            when(addressRepo.findByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(page);
    
            when(mapper.toAddressRes(address))
                    .thenReturn(dto);
    
            Page<AddressRes> result =
                    addressService.findByUserId(userId, PageRequest.of(0, 10));
    
            assertEquals(1, result.getTotalElements());
    
            verify(mapper).toAddressRes(address);
        }
    }
}
