package com.example.e_commerce.filter;

import com.example.e_commerce.entity.User;
import com.example.e_commerce.service.JwtService;
import com.example.e_commerce.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;

    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");

        Claims claims = jwtService.parseToken(token);
        String userId = claims.get("userId", String.class);
        List<String> roles = claims.get("roles", List.class);
        List<String> permissions = claims.get("permissions", List.class);

        User user = userService.findById(UUID.fromString(userId));
        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (roles != null) {
            authorities.addAll(
                    roles.stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .toList()
            );
        }

        if (permissions != null) {
            authorities.addAll(
                    permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList()
            );
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                authorities
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
