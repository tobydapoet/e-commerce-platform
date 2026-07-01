package com.example.e_commerce.service;

import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.constant.UserStatus;
import com.example.e_commerce.dto.request.UpdateUserReq;
import com.example.e_commerce.dto.response.UserSimpleRes;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.mapper.UserMapper;
import com.example.e_commerce.repository.UserRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private UploadService uploadService;
    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("Nguyen Tung");
        user.setEmail("nguyentung@example.com");
        user.setStatus(UserStatus.ACTIVE);
    }

    @Nested
    class FindByIdTests {

        @Test
        void findById_shouldReturnUser_whenExists() {
            when(userRepo.findById(userId)).thenReturn(Optional.of(user));

            User result = userService.findById(userId);

            assertEquals(user, result);
        }

        @Test
        void findById_shouldThrow_whenNotFound() {
            when(userRepo.findById(userId)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> userService.findById(userId));
        }
    }

    @Nested
    class UpdateUserTests {

        @Test
        void updateUser_shouldThrow_whenUserNotFound() {
            when(userRepo.findById(userId)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> userService.updateUser(userId, new UpdateUserReq()));
        }

        @Test
        void updateUser_shouldUpdateName_whenProvided() {
            UpdateUserReq req = new UpdateUserReq();
            req.setName("New Name");

            when(userRepo.findById(userId)).thenReturn(Optional.of(user));

            userService.updateUser(userId, req);

            assertEquals("New Name", user.getName());
            verify(userRepo).save(user);
        }

        @Test
        void updateUser_shouldNotChangeName_whenNameIsNull() {
            UpdateUserReq req = new UpdateUserReq();
            req.setName(null);

            when(userRepo.findById(userId)).thenReturn(Optional.of(user));

            userService.updateUser(userId, req);

            assertEquals("Nguyen Tung", user.getName());
            verify(userRepo).save(user);
        }

        @Test
        void updateUser_shouldUploadNewAvatar_whenOldAvatarIsNull() {
            UpdateUserReq req = new UpdateUserReq();
            MultipartFile avatarFile = mock(MultipartFile.class);
            req.setAvatar(avatarFile);

            when(userRepo.findById(userId)).thenReturn(Optional.of(user));
            when(uploadService.upload(avatarFile, "user")).thenReturn("new-avatar-url");

            userService.updateUser(userId, req);

            verify(uploadService, never()).delete(any());
            verify(uploadService).upload(avatarFile, "user");
            assertEquals("new-avatar-url", user.getAvatar());
            verify(userRepo).save(user);
        }

        @Test
        void updateUser_shouldDeleteOldAvatar_beforeUploadingNew() {
            user.setAvatar("old-avatar-url");
            UpdateUserReq req = new UpdateUserReq();
            MultipartFile avatarFile = mock(MultipartFile.class);
            req.setAvatar(avatarFile);

            when(userRepo.findById(userId)).thenReturn(Optional.of(user));
            when(uploadService.upload(avatarFile, "user")).thenReturn("new-avatar-url");

            userService.updateUser(userId, req);

            verify(uploadService).delete("old-avatar-url");
            verify(uploadService).upload(avatarFile, "user");
            assertEquals("new-avatar-url", user.getAvatar());
        }

        @Test
        void updateUser_shouldNotTouchAvatar_whenAvatarIsNull() {
            user.setAvatar("existing-avatar-url");
            UpdateUserReq req = new UpdateUserReq();
            req.setAvatar(null);

            when(userRepo.findById(userId)).thenReturn(Optional.of(user));

            userService.updateUser(userId, req);

            verify(uploadService, never()).delete(any());
            verify(uploadService, never()).upload(any(), anyString());
            assertEquals("existing-avatar-url", user.getAvatar());
        }
    }

    @Nested
    class UpdateUserStatusTests {

        @Test
        void updateUserStatus_shouldThrow_whenUserNotFound() {
            when(userRepo.findById(userId)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> userService.updateUserStatus(userId, UserStatus.BLOCKED));
        }

        @Test
        void updateUserStatus_shouldUpdateAndSave_whenStatusIsDifferent() {
            when(userRepo.findById(userId)).thenReturn(Optional.of(user));

            userService.updateUserStatus(userId, UserStatus.BLOCKED);

            assertEquals(UserStatus.BLOCKED, user.getStatus());
            verify(userRepo).save(user);
        }

        @Test
        void updateUserStatus_shouldNotSave_whenStatusIsSame() {
            when(userRepo.findById(userId)).thenReturn(Optional.of(user));

            userService.updateUserStatus(userId, UserStatus.ACTIVE);

            verify(userRepo, never()).save(any());
        }
    }

    @Nested
    class SearchTests {

        @Test
        void search_shouldUseActiveStatus_whenStatusIsNull() {
            Pageable pageable = mock(Pageable.class);
            Page<User> userPage = new PageImpl<>(List.of(user));
            UserSimpleRes res = mock(UserSimpleRes.class);

            when(userRepo.search(eq("tung"), eq(RoleType.CUSTOMER), eq(UserStatus.ACTIVE), eq(pageable)))
                    .thenReturn(userPage);
            when(mapper.toUserSimpleRes(user)).thenReturn(res);

            Page<UserSimpleRes> result = userService.search("tung", RoleType.CUSTOMER, null, pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(res, result.getContent().get(0));
            verify(userRepo).search("tung", RoleType.CUSTOMER, UserStatus.ACTIVE, pageable);
        }

        @Test
        void search_shouldUseProvidedStatus_whenNotNull() {
            Pageable pageable = mock(Pageable.class);
            Page<User> userPage = new PageImpl<>(List.of(user));
            UserSimpleRes res = mock(UserSimpleRes.class);

            when(userRepo.search(eq("tung"), eq(RoleType.ADMIN), eq(UserStatus.BLOCKED), eq(pageable)))
                    .thenReturn(userPage);
            when(mapper.toUserSimpleRes(user)).thenReturn(res);

            Page<UserSimpleRes> result = userService.search("tung", RoleType.ADMIN, UserStatus.BLOCKED, pageable);

            assertEquals(1, result.getTotalElements());
            verify(userRepo).search("tung", RoleType.ADMIN, UserStatus.BLOCKED, pageable);
        }

        @Test
        void search_shouldReturnEmptyPage_whenNoResults() {
            Pageable pageable = mock(Pageable.class);
            Page<User> emptyPage = new PageImpl<>(List.of());

            when(userRepo.search(any(), any(), any(), eq(pageable))).thenReturn(emptyPage);

            Page<UserSimpleRes> result = userService.search("nothing", null, null, pageable);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class CustomerSearchTests {

        @Test
        void customerSearch_shouldAlwaysUseCustomerRole_andActiveStatus() {
            Pageable pageable = mock(Pageable.class);
            Page<User> userPage = new PageImpl<>(List.of(user));
            UserSimpleRes res = mock(UserSimpleRes.class);

            when(userRepo.search(eq("tung"), eq(RoleType.CUSTOMER), eq(UserStatus.ACTIVE), eq(pageable)))
                    .thenReturn(userPage);
            when(mapper.toUserSimpleRes(user)).thenReturn(res);

            Page<UserSimpleRes> result = userService.customerSearch("tung", pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(res, result.getContent().get(0));
            verify(userRepo).search("tung", RoleType.CUSTOMER, UserStatus.ACTIVE, pageable);
        }
    }
}