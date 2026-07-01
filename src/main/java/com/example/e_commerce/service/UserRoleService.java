package com.example.e_commerce.service;

import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.entity.Role;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.entity.UserRole;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.UnauthorizedException;
import com.example.e_commerce.repository.RoleRepository;
import com.example.e_commerce.repository.UserRepository;
import com.example.e_commerce.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRoleRepository userRoleRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public UserRole updateUserRole(UUID id, RoleType roleType) {
        UserRole userRole = new UserRole();
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UnauthorizedException("User not found!"));
        userRole.setUser(user);
        Role role = roleRepo.findByRoleName(roleType)
                .orElseThrow(() -> new ForbiddenException("Role not found!"));
        userRole.setRole(role);
        return userRoleRepo.save(userRole);
    }
}
