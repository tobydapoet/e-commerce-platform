package com.example.e_commerce.service;

import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.dto.request.LoginGoogleReq;
import com.example.e_commerce.dto.request.LoginReq;
import com.example.e_commerce.dto.request.RegisterReq;
import com.example.e_commerce.dto.response.LoginRes;
import com.example.e_commerce.entity.Role;
import com.example.e_commerce.entity.Session;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.UserRole;
import com.example.e_commerce.exception.UnauthorizedException;
import com.example.e_commerce.repository.RoleRepository;
import com.example.e_commerce.repository.SessionRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private UserRoleRepository userRoleRepo;

    @Mock
    private SessionRepository sessionRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UploadService uploadService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldCreateUser_WhenEmailNotExists() {
        RegisterReq req = new RegisterReq();
        req.setName("Nguyen Van A");
        req.setEmail("test@gmail.com");
        req.setPassword("123456");

        Role role = new Role();
        role.setRoleName(RoleType.CUSTOMER);

        when(userRepo.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(roleRepo.findByRoleName(RoleType.CUSTOMER))
                .thenReturn(Optional.of(role));

        authService.register(req);

        verify(userRepo, times(1)).save(any());
        verify(userRoleRepo, times(1)).save(any());

        verify(passwordEncoder)
                .encode(req.getPassword());

        verify(roleRepo)
                .findByRoleName(RoleType.CUSTOMER);
    }

    @Test
    void register_ShouldThrowException_WhenCustomerRoleNotFound() {

        RegisterReq req = new RegisterReq();
        req.setName("Nguyen Van A");
        req.setEmail("test@gmail.com");
        req.setPassword("123456");

        when(userRepo.existsByEmail(req.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-password");

        when(roleRepo.findByRoleName(RoleType.CUSTOMER))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(req)
        );

        assertEquals("Customer role not found", exception.getMessage());

        verify(userRepo, times(1)).save(any());
        verify(userRoleRepo, never()).save(any());

        verify(roleRepo, times(1))
                .findByRoleName(RoleType.CUSTOMER);
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        RegisterReq req = new RegisterReq();
        req.setName("Nguyen Van A");
        req.setEmail("tnkoko123@gmail.com");
        req.setPassword("123456");

        Role role = new Role();
        role.setRoleName(RoleType.CUSTOMER);

        when(userRepo.existsByEmail(req.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(req)
        );

        assertEquals("This email is already used!", exception.getMessage());

        verify(userRepo, never()).save(any());
        verify(userRoleRepo, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(roleRepo, never()).findByRoleName(any());
    }

    @Test
    void register_ShouldCreateUser_WhenAvatarIsNull() {
        RegisterReq req = new RegisterReq();
        req.setName("Nguyen Van A");
        req.setEmail("test@gmail.com");
        req.setPassword("123456");
        req.setAvatar(null);

        Role role = new Role();
        role.setRoleName(RoleType.CUSTOMER);

        when(userRepo.existsByEmail(req.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-password");

        when(roleRepo.findByRoleName(RoleType.CUSTOMER))
                .thenReturn(Optional.of(role));

        authService.register(req);

        verify(uploadService, never())
                .upload(any(), anyString());

        verify(userRepo).save(any());
        verify(userRoleRepo).save(any());

    }

    @Test
    void register_ShouldUploadAvatar_WhenAvatarProvided() {
        RegisterReq req = new RegisterReq();
        req.setName("Nguyen Van A");
        req.setEmail("test@gmail.com");
        req.setPassword("123456");

        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "avatar.png",
                "image/png",
                "hello".getBytes()
        );

        req.setAvatar(avatar);

        Role role = new Role();
        role.setRoleName(RoleType.CUSTOMER);

        when(uploadService.upload(avatar, "user"))
                .thenReturn("avatar.png");
        when(userRepo.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(roleRepo.findByRoleName(RoleType.CUSTOMER))
                .thenReturn(Optional.of(role));

        authService.register(req);

        verify(uploadService).upload(avatar, "user");

        verify(userRepo).save(any());
        verify(userRoleRepo).save(any());
    }

    @Test
    void login_ShouldReturnTokens_WhenCredentialsValid() {

        LoginReq req = new LoginReq();
        req.setEmail("test@gmail.com");
        req.setPassword("123456");

        User user = new User();
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        user.setId(userId);
        user.setEmail(req.getEmail());
        user.setPassword("encoded-password");

        Session session = new Session();
        session.setId(10L);
        session.setUser(user);

        when(userRepo.findByEmail(req.getEmail()))
                .thenReturn(user);

        when(passwordEncoder.matches(req.getPassword(), user.getPassword()))
                .thenReturn(true);

        when(sessionRepo.save(any(Session.class)))
                .thenReturn(session);

        when(jwtService.generateRefreshToken())
                .thenReturn("refreshToken");

        when(jwtService.generateAccessToken(any()))
                .thenReturn("accessToken");

        LoginRes res = authService.login(req);

        assertEquals("accessToken", res.accessToken());
        assertEquals("refreshToken", res.refreshToken());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIncorrect() {
        LoginReq req = new LoginReq();
        req.setEmail("test@gmail.com");
        req.setPassword("123456");

        User user = new User();
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        user.setId(userId);
        user.setEmail(req.getEmail());
        user.setPassword("encoded-password");

        when(userRepo.findByEmail(req.getEmail()))
                .thenReturn(user);
        when(passwordEncoder.matches(req.getPassword(), user.getPassword()))
                .thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(req)
        );
        assertEquals("Invalid email or password!", ex.getMessage());
        verify(userRepo, never()).save(any());
    }

    @Test
    void login_ShouldThrowException_WhenEmailNotFound() {
        LoginReq req = new LoginReq();
        req.setEmail("notfound@gmail.com");
        req.setPassword("123456");

        when(userRepo.findByEmail(req.getEmail()))
                .thenReturn(null);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(req)
        );
        assertEquals("Invalid email or password!", ex.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(sessionRepo, never()).save(any());
    }

    @Test
    void refreshToken_ShouldReturnNewAccessToken_WhenSessionValid() {
        String refreshToken = "refreshToken";

        Session session = new Session();
        session.setId(1L);
        session.setRevoked(false);
        session.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(sessionRepo.findByToken(refreshToken))
                .thenReturn(Optional.of(session));

        when(jwtService.generateAccessToken(session))
                .thenReturn("newAccessToken");

        String result = authService.refreshToken(refreshToken);

        assertEquals("newAccessToken", result);

        verify(sessionRepo).findByToken(refreshToken);
        verify(jwtService).generateAccessToken(session);
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenInvalid() {

        String token = "invalid";

        when(sessionRepo.findByToken(token))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken(token)
        );

        assertEquals("Invalid refresh token", ex.getMessage());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenSessionRevoked() {

        String token = "refreshToken";

        Session session = new Session();
        session.setRevoked(true);

        when(sessionRepo.findByToken(token))
                .thenReturn(Optional.of(session));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken(token)
        );

        assertEquals("Session revoked", ex.getMessage());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenSessionExpired() {

        String token = "refreshToken";

        Session session = new Session();
        session.setRevoked(false);
        session.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(sessionRepo.findByToken(token))
                .thenReturn(Optional.of(session));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken(token)
        );

        assertEquals("Session expired", ex.getMessage());
    }

    @Test
    void logout_ShouldRevokeSession_WhenTokenValid() {

        String token = "accessToken";
        Long sessionId = 1L;

        Session session = new Session();
        session.setId(sessionId);
        session.setRevoked(false);

        when(jwtService.extractSessionId(token))
                .thenReturn(sessionId);

        when(sessionRepo.findById(sessionId))
                .thenReturn(Optional.of(session));

        authService.logout(token);

        assertTrue(session.getRevoked());

        verify(sessionRepo).save(session);
    }

    @Test
    void logout_ShouldThrowException_WhenSessionNotFound() {

        String token = "invalidToken";
        Long sessionId = 1L;

        when(jwtService.extractSessionId(token))
                .thenReturn(sessionId);

        when(sessionRepo.findById(sessionId))
                .thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> authService.logout(token)
        );

        assertEquals("Invalid token!", ex.getMessage());

        verify(sessionRepo, never()).save(any());
    }

    @Test
    void loginWithGoogle_ShouldCreateUser_WhenUserNotExists() {

        LoginGoogleReq req = new LoginGoogleReq();
        req.setEmail("test@gmail.com");
        req.setName("Test");
        req.setAvatarUrl("avatar.png");
        req.setProvidedId("google-123");

        User user = new User();
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        user.setId(userId);
        user.setEmail(req.getEmail());

        Role role = new Role();
        role.setRoleName(RoleType.CUSTOMER);

        when(userRepo.findByEmail(req.getEmail()))
                .thenReturn(null);

        when(roleRepo.findByRoleName(RoleType.CUSTOMER))
                .thenReturn(Optional.of(role));

        when(jwtService.generateRefreshToken())
                .thenReturn("refreshToken");

        when(jwtService.generateAccessToken(any()))
                .thenReturn("accessToken");

        LoginRes res = authService.loginWithGoogle(req);

        assertEquals("refreshToken", res.refreshToken());
        assertEquals("accessToken", res.accessToken());

        verify(userRepo).save(any(User.class));
        verify(userRoleRepo).save(any(UserRole.class));
        verify(sessionRepo).save(any(Session.class));
    }

    @Test
    void loginWithGoogle_ShouldNotCreateUser_WhenUserExists() {

        LoginGoogleReq req = new LoginGoogleReq();
        req.setEmail("test@gmail.com");

        User existingUser = new User();
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        existingUser.setId(userId);
        existingUser.setEmail(req.getEmail());

        Role role = new Role();
        role.setRoleName(RoleType.CUSTOMER);

        when(userRepo.findByEmail(req.getEmail()))
                .thenReturn(existingUser);

        when(jwtService.generateRefreshToken())
                .thenReturn("refreshToken");

        when(jwtService.generateAccessToken(any()))
                .thenReturn("accessToken");

        authService.loginWithGoogle(req);

        verify(userRepo, never()).save(any(User.class));
        verify(userRoleRepo, never()).save(any(UserRole.class));
    }

    @Test
    void loginWithGoogle_ShouldThrowException_WhenRoleNotFound() {

        LoginGoogleReq req = new LoginGoogleReq();
        req.setEmail("test@gmail.com");

        when(userRepo.findByEmail(req.getEmail()))
                .thenReturn(null);

        when(roleRepo.findByRoleName(RoleType.CUSTOMER))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.loginWithGoogle(req)
        );

        assertEquals("Customer role not found", ex.getMessage());

        verify(userRepo).save(any(User.class));
        verify(userRoleRepo, never()).save(any(UserRole.class));
    }
}
