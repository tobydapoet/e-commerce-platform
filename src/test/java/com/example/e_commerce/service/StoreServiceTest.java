package com.example.e_commerce.service;

import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.constant.StoreStatus;
import com.example.e_commerce.dto.request.CreateStoreReq;
import com.example.e_commerce.dto.request.UpdateStoreReq;
import com.example.e_commerce.dto.response.StoreRes;
import com.example.e_commerce.entity.Store;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.exception.UnauthorizedException;
import com.example.e_commerce.mapper.StoreMapper;
import com.example.e_commerce.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepo;
    @Mock
    private UploadService uploadService;
    @Mock
    private MailService mailService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private StoreMapper mapper;
    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private StoreService storeService;

    private User currentUser;
    private Store store;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setEmail("owner@example.com");

        store = new Store();
        store.setId(1L);
        store.setOwner(currentUser);
        store.setName("My Store");
        store.setPhone("0900000000");
        store.setEmail("store@example.com");
        store.setStatus(StoreStatus.ACTIVE);
    }

    @Nested
    class CreateTests {

        @Test
        void create_shouldSaveStore_andUpdateRoleToSeller_whenSuccessful() {
            CreateStoreReq req = new CreateStoreReq();
            req.setName("New Store");
            req.setPhone("0911111111");
            req.setDescription("desc");

            when(storeRepo.save(any(Store.class))).thenReturn(store);

            Store result = storeService.create(req, currentUser);

            assertNotNull(result);
            verify(storeRepo).save(any(Store.class));
            verify(userRoleService).updateUserRole(currentUser.getId(), RoleType.SELLER);
        }

        @Test
        void create_shouldUseCurrentUserEmail_whenReqEmailIsNull() {
            CreateStoreReq req = new CreateStoreReq();
            req.setName("New Store");
            req.setPhone("0911111111");
            req.setEmail(null);

            when(storeRepo.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

            Store result = storeService.create(req, currentUser);

            assertEquals(currentUser.getEmail(), result.getEmail());
        }

        @Test
        void create_shouldUploadLogoAndBanner_whenProvided() {
            CreateStoreReq req = new CreateStoreReq();
            req.setName("New Store");
            req.setPhone("0911111111");
            req.setLogo(mock(org.springframework.web.multipart.MultipartFile.class));
            req.setBanner(mock(org.springframework.web.multipart.MultipartFile.class));

            when(uploadService.upload(any(), eq("store_logo"))).thenReturn("logo-url");
            when(uploadService.upload(any(), eq("store_banner"))).thenReturn("banner-url");
            when(storeRepo.save(any(Store.class))).thenAnswer(inv -> inv.getArgument(0));

            Store result = storeService.create(req, currentUser);

            assertEquals("logo-url", result.getLogo());
            assertEquals("banner-url", result.getBanner());
        }

        @Test
        void create_shouldNotUpdateRole_whenSaveFails() {
            CreateStoreReq req = new CreateStoreReq();
            req.setName("New Store");
            req.setPhone("0911111111");

            when(storeRepo.save(any(Store.class))).thenReturn(null);

            storeService.create(req, currentUser);

            verify(userRoleService, never()).updateUserRole(any(), any());
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void update_shouldThrow_whenStoreNotFound() {
            when(storeRepo.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> storeService.update(1L, new UpdateStoreReq(), currentUser));
        }

        @Test
        void update_shouldThrowUnauthorized_whenCurrentUserIsNotOwner() {
            User anotherUser = new User();
            anotherUser.setId(UUID.randomUUID());
            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));

            assertThrows(UnauthorizedException.class,
                    () -> storeService.update(1L, new UpdateStoreReq(), anotherUser));
        }

        @Test
        void update_shouldDeleteOldLogo_whenNewLogoProvided() {
            store.setLogo("old-logo-url");
            UpdateStoreReq req = new UpdateStoreReq();
            req.setLogo(mock(org.springframework.web.multipart.MultipartFile.class));

            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));
            when(uploadService.upload(any(), eq("store_logo"))).thenReturn("new-logo-url");

            storeService.update(1L, req, currentUser);

            verify(uploadService).delete("old-logo-url");
            verify(storeRepo).save(store);
            assertEquals("new-logo-url", store.getLogo());
        }

        @Test
        void update_shouldUpdateDescription_whenProvided() {
            UpdateStoreReq req = new UpdateStoreReq();
            req.setDescription("new description");

            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));

            storeService.update(1L, req, currentUser);

            assertEquals("new description", store.getDescription());
            verify(storeRepo).save(store);
        }
    }

    @Nested
    class RequestPhoneUpdateOtpTests {

        @Test
        void requestOtp_shouldThrow_whenStoreNotFound() {
            when(storeRepo.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> storeService.requestPhoneUpdateOtp(1L, "0999999999", currentUser));
        }

        @Test
        void requestOtp_shouldThrowUnauthorized_whenNotOwner() {
            User anotherUser = new User();
            anotherUser.setId(UUID.randomUUID());
            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));

            assertThrows(UnauthorizedException.class,
                    () -> storeService.requestPhoneUpdateOtp(1L, "0999999999", anotherUser));
        }

        @Test
        void requestOtp_shouldThrow_whenCooldownActive() {
            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));
            when(redisTemplate.hasKey("otp:store-phone:cooldown:1")).thenReturn(true);

            assertThrows(IllegalStateException.class,
                    () -> storeService.requestPhoneUpdateOtp(1L, "0999999999", currentUser));
        }

        @Test
        void requestOtp_shouldSendMail_andSaveOtpToRedis_whenSuccessful() {
            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));
            when(redisTemplate.hasKey(anyString())).thenReturn(false);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            storeService.requestPhoneUpdateOtp(1L, "0999999999", currentUser);

            verify(valueOperations).set(
                    eq("otp:store-phone:1"),
                    anyString(),
                    any(Duration.class)
            );

            verify(valueOperations).set(
                    eq("otp:store-phone:pending:1"),
                    eq("0999999999"),
                    any(Duration.class)
            );
        }
    }

    @Nested
    class VerifyAndUpdatePhoneTests {

        @Test
        void verify_shouldThrow_whenOtpNotRequested() {
            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("otp:store-phone:1")).thenReturn(null);

            assertThrows(IllegalStateException.class,
                    () -> storeService.verifyAndUpdatePhone(1L, "123456", currentUser));
        }

        @Test
        void verify_shouldThrow_whenOtpInvalid() {
            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("otp:store-phone:1")).thenReturn("111111");
            when(valueOperations.get("otp:store-phone:pending:1")).thenReturn("0999999999");
            when(redisTemplate.opsForValue().increment("otp:store-phone:attempts:1")).thenReturn(1L);

            assertThrows(IllegalArgumentException.class,
                    () -> storeService.verifyAndUpdatePhone(1L, "222222", currentUser));
        }

        @Test
        void verify_shouldThrow_whenTooManyAttempts() {
            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("otp:store-phone:1")).thenReturn("111111");
            when(valueOperations.get("otp:store-phone:pending:1")).thenReturn("0999999999");
            when(valueOperations.increment("otp:store-phone:attempts:1")).thenReturn(6L);

            assertThrows(IllegalStateException.class,
                    () -> storeService.verifyAndUpdatePhone(1L, "111111", currentUser));

            verify(redisTemplate).delete("otp:store-phone:1");
            verify(redisTemplate).delete("otp:store-phone:pending:1");
        }

        @Test
        void verify_shouldUpdatePhone_andClearRedisKeys_whenOtpCorrect() {
            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("otp:store-phone:1")).thenReturn("111111");
            when(valueOperations.get("otp:store-phone:pending:1")).thenReturn("0999999999");
            when(valueOperations.increment("otp:store-phone:attempts:1")).thenReturn(1L);
            when(storeRepo.save(store)).thenReturn(store);

            Store result = storeService.verifyAndUpdatePhone(1L, "111111", currentUser);

            assertEquals("0999999999", result.getPhone());
            verify(redisTemplate).delete("otp:store-phone:1");
            verify(redisTemplate).delete("otp:store-phone:pending:1");
            verify(redisTemplate).delete("otp:store-phone:attempts:1");
        }
    }

    @Nested
    class UpdateStatusTests {

        @Test
        void updateStatus_shouldThrow_whenStoreNotFound() {
            when(storeRepo.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> storeService.updateStatus(1L, StoreStatus.INACTIVE));
        }

        @Test
        void updateStatus_shouldUpdateAndSave() {
            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));

            storeService.updateStatus(1L, StoreStatus.BLOCKED);

            assertEquals(StoreStatus.BLOCKED, store.getStatus());
            verify(storeRepo).save(store);
        }
    }

    @Nested
    class SearchTests {

        @Test
        void search_shouldReturnMappedPage() {
            Pageable pageable = mock(Pageable.class);
            Page<Store> storePage = new PageImpl<>(List.of(store));
            StoreRes storeRes = mock(StoreRes.class);

            when(storeRepo.search("keyword", pageable)).thenReturn(storePage);
            when(mapper.toStoreRes(store)).thenReturn(storeRes);

            Page<StoreRes> result = storeService.search("keyword", pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(storeRes, result.getContent().get(0));
        }
    }

    @Nested
    class GetMyStoresTests {

        @Test
        void getMyStores_shouldThrow_whenEmpty() {
            when(storeRepo.findByOwnerId(currentUser.getId())).thenReturn(List.of());

            assertThrows(ResourceNotFoundException.class,
                    () -> storeService.getMyStores(currentUser));
        }

        @Test
        void getMyStores_shouldReturnMappedList_whenNotEmpty() {
            StoreRes storeRes = mock(StoreRes.class);
            when(storeRepo.findByOwnerId(currentUser.getId())).thenReturn(List.of(store));
            when(mapper.toStoreRes(store)).thenReturn(storeRes);

            List<StoreRes> result = storeService.getMyStores(currentUser);

            assertEquals(1, result.size());
            assertEquals(storeRes, result.get(0));
        }
    }

    @Nested
    class DeactivateTests {

        @Test
        void deactivate_shouldThrow_whenStoreNotFound() {
            when(storeRepo.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> storeService.deactivate(1L, currentUser));
        }

        @Test
        void deactivate_shouldThrowUnauthorized_whenNotOwner() {
            User anotherUser = new User();
            anotherUser.setId(UUID.randomUUID());
            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));

            assertThrows(UnauthorizedException.class,
                    () -> storeService.deactivate(1L, anotherUser));
        }

        @Test
        void deactivate_shouldSetStatusInactive_andSave() {
            when(storeRepo.findById(1L)).thenReturn(Optional.of(store));

            storeService.deactivate(1L, currentUser);

            assertEquals(StoreStatus.INACTIVE, store.getStatus());
            verify(storeRepo).save(store);
        }
    }
}