package com.example.e_commerce.service;

import com.example.e_commerce.constant.PermissionName;
import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.entity.Role;
import com.example.e_commerce.entity.Session;
import com.example.e_commerce.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET;

    private Key key;

    private final long accessTokenValidity = 1000 * 60 * 15;
//    private final long accessTokenValidity = 30L * 24 * 3600 * 1000;
    private final long refreshTokenValidity = 30L * 24 * 3600 * 1000;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateAccessToken(Session session) {

        List<RoleType> roles = session.getUser()
                .getUserRoles()
                .stream()
                .map(userRole -> userRole.getRole().getRoleName())
                .toList();

        List<PermissionName> permissions = session.getUser()
                .getUserRoles()
                .stream()
                .flatMap(userRole ->
                        userRole.getRole()
                                .getRolePermissions()
                                .stream()
                )
                .map(rolePermission ->
                        rolePermission.getPermission().getName()
                )
                .distinct()
                .toList();

        return Jwts.builder()
                .setSubject(session.getId().toString())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("userId", session.getUser().getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public LocalDateTime getRefreshTokenExpiry() {
        return LocalDateTime.now().plusSeconds(refreshTokenValidity / 1000);
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractSessionId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }
}