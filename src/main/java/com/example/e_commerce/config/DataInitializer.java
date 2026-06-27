package com.example.e_commerce.config;

import com.example.e_commerce.constant.PermissionName;
import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.entity.Permission;
import com.example.e_commerce.entity.Role;
import com.example.e_commerce.entity.RolePermission;
import com.example.e_commerce.repository.RoleRepository;
import com.example.e_commerce.repository.PermissionRepository;
import com.example.e_commerce.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedRoles();
        seedPermissions();
        seedRolePermissions();
    }

    private void seedRoles() {

        Set<RoleType> existing = roleRepository.findAll()
                .stream()
                .map(com.example.e_commerce.entity.Role::getRoleName)
                .collect(Collectors.toSet());

        List<Role> newRoles = Arrays.stream(RoleType.values())
                .filter(role -> !existing.contains(role))
                .map(role -> {
                    Role r = new Role();
                    r.setRoleName(role);
                    r.setDescription(role.name());
                    return r;
                })
                .toList();

        roleRepository.saveAll(newRoles);
    }

    private void seedPermissions() {

        Set<PermissionName> existing = permissionRepository.findAll()
                .stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        List<Permission> newPermissions = Arrays.stream(PermissionName.values())
                .filter(p -> !existing.contains(p))
                .map(p -> {
                    Permission permission = new Permission();
                    permission.setName(p);
                    return permission;
                })
                .toList();

        permissionRepository.saveAll(newPermissions);
    }

    private void seedRolePermissions() {

        if (rolePermissionRepository.count() > 0) return;

        Role admin = roleRepository.findByRoleName(RoleType.ADMIN).orElseThrow();
        Role moderator = roleRepository.findByRoleName(RoleType.MODERATOR).orElseThrow();
        Role seller = roleRepository.findByRoleName(RoleType.SELLER).orElseThrow();
        Role customer = roleRepository.findByRoleName(RoleType.CUSTOMER).orElseThrow();

        List<Permission> allPermissions = permissionRepository.findAll();

        List<RolePermission> mappings = new ArrayList<>();

        for (Permission p : allPermissions) {

            String name = p.getName().name();

            mappings.add(new RolePermission(null, admin, p));

            if (name.startsWith("USER_")
                    || name.startsWith("PRODUCT_")
                    || name.startsWith("CATEGORY_")
                    || name.equals("ORDER_READ")) {

                mappings.add(new RolePermission(null, moderator, p));
            }

            if (name.startsWith("PRODUCT_")
                    || name.startsWith("ORDER_")
                    || name.startsWith("INVENTORY_")) {

                mappings.add(new RolePermission(null, seller, p));
            }

            if (name.equals("PRODUCT_READ")
                    || name.equals("ORDER_READ")
                    || name.equals("ORDER_CREATE")
                    || name.startsWith("WISHLIST_")) {

                mappings.add(new RolePermission(null, customer, p));
            }
        }

        rolePermissionRepository.saveAll(mappings);
    }
}