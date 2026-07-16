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
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.exception.UnauthorizedException;
import com.example.e_commerce.repository.RoleRepository;
import com.example.e_commerce.repository.SessionRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.repository.UserRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final SessionRepository sessionRepo;
    private final UserRoleRepository userRoleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UploadService uploadService;

    @Transactional
    public LoginRes login(LoginReq req) {

        User existingUser = userRepo.findByEmail(req.getEmail());

        if (existingUser == null ||
                !passwordEncoder.matches(req.getPassword(), existingUser.getPassword())) {
            throw new UnauthorizedException("Invalid email or password!");
        }

        Session session = new Session();
        session.setUser(existingUser);
        session.setRevoked(false);
        session.setExpiresAt(jwtService.getRefreshTokenExpiry());

        String refreshToken = jwtService.generateRefreshToken();

        session.setToken(refreshToken);

        sessionRepo.save(session);

        String accessToken = jwtService.generateAccessToken(session);

        return new LoginRes(accessToken, refreshToken);
    }

    public void logout(String accessToken) {

        Long sessionId = jwtService.extractSessionId(accessToken);

        Session session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new UnauthorizedException("Invalid token!"));

        session.setRevoked(true);

        sessionRepo.save(session);
    }

    public String refreshToken(String refreshToken) {
        Session session = sessionRepo.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (session.getRevoked()) {
            throw new UnauthorizedException("Session revoked");
        }

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Session expired");
        }

        return jwtService.generateAccessToken(session);
    }

    public void register(RegisterReq req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new ResourceNotFoundException("This email is already used!");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setName(req.getName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        if(req.getAvatar() != null && !req.getAvatar().isEmpty()) {
            String avatar = uploadService.upload(req.getAvatar(),"user");
            user.setAvatar(avatar);
        }

        userRepo.save(user);

        Role customerRole = roleRepo.findByRoleName(RoleType.CUSTOMER)
                .orElseThrow(() -> new ForbiddenException("Customer role not found"));
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(customerRole);

        userRoleRepo.save(userRole);
    }

    @Transactional
    public LoginRes loginWithGoogle(LoginGoogleReq req) {
        User user = userRepo.findByEmail(req.getEmail());
        if (user == null) {
            user = new User();
            user.setEmail(req.getEmail());
            user.setAvatar(req.getAvatarUrl());
            user.setProviderId(req.getProvidedId());
            user.setName(req.getName());
            userRepo.save(user);
            Role customerRole = roleRepo.findByRoleName(RoleType.CUSTOMER)
                    .orElseThrow(() -> new ForbiddenException("Customer role not found"));
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(customerRole);

            userRoleRepo.save(userRole);
        }
        Session session = new Session();
        session.setUser(user);
        session.setRevoked(false);
        session.setExpiresAt(jwtService.getRefreshTokenExpiry());

        String refreshToken = jwtService.generateRefreshToken();

        session.setToken(refreshToken);
        sessionRepo.save(session);

        String accessToken = jwtService.generateAccessToken(session);

        return new LoginRes(accessToken, refreshToken);
    }
}
