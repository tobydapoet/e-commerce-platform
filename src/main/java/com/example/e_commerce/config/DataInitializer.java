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
    private static final String STORE_READ = "STORE_READ";

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
        Role admin = roleRepository.findByRoleName(RoleType.ADMIN).orElseThrow();
        Role moderator = roleRepository.findByRoleName(RoleType.MODERATOR).orElseThrow();
        Role seller = roleRepository.findByRoleName(RoleType.SELLER).orElseThrow();
        Role customer = roleRepository.findByRoleName(RoleType.CUSTOMER).orElseThrow();

        List<Permission> allPermissions = permissionRepository.findAll();
        Set<String> existingMappings = rolePermissionRepository.findAll()
                .stream()
                .map(mapping -> mapping.getRole().getId() + ":" + mapping.getPermission().getId())
                .collect(Collectors.toSet());

        List<RolePermission> mappings = new ArrayList<>();

        for (Permission p : allPermissions) {

            String name = p.getName().name();

            addMappingIfMissing(mappings, existingMappings, admin, p);

            if (name.equals("USER_READ")
                    || name.equals("USER_UPDATE")
                    || name.equals("ADDRESS_READ_ALL")
                    || name.startsWith("PRODUCT_")
                    || name.startsWith("PRODUCT_VARIANT_")
                    || name.startsWith("ATTRIBUTE_")
                    || name.startsWith("ATTRIBUTE_VALUE_")
                    || name.startsWith("CATEGORY_")
                    || name.equals(STORE_READ)
                    || name.equals("ORDER_READ")) {

                addMappingIfMissing(mappings, existingMappings, moderator, p);
            }

            if (name.startsWith("PRODUCT_")
                    || name.startsWith("PRODUCT_VARIANT_")
                    || name.startsWith("ATTRIBUTE_")
                    || name.startsWith("ATTRIBUTE_VALUE_")
                    || name.startsWith("ORDER_STORE_")
                    || name.startsWith("INVENTORY_")
                    || name.startsWith("COUPON_")
                    || name.equals(STORE_READ)
                    || name.equals("STORE_UPDATE")
                    || name.equals("STORE_DELETE")) {

                addMappingIfMissing(mappings, existingMappings, seller, p);
            }

            if (name.equals("USER_SELF_READ")
                    || name.equals("USER_SELF_UPDATE")
                    || name.equals("ADDRESS_READ")
                    || name.equals("ADDRESS_CREATE")
                    || name.equals("ADDRESS_UPDATE")
                    || name.equals("ADDRESS_DELETE")
                    || name.startsWith("CART_")
                    || name.equals("PRODUCT_READ")
                    || name.equals("PRODUCT_VARIANT_READ")
                    || name.equals("CATEGORY_READ")
                    || name.equals(STORE_READ)
                    || name.equals("STORE_CREATE")
                    || name.equals("ORDER_READ")
                    || name.equals("ORDER_CREATE")
                    || name.equals("REVIEW_READ")
                    || name.equals("REVIEW_CREATE")
                    || name.equals("REVIEW_DELETE")
                    || name.startsWith("WISHLIST_")) {

                addMappingIfMissing(mappings, existingMappings, customer, p);
            }
        }

        rolePermissionRepository.saveAll(mappings);
    }

    private void addMappingIfMissing(
            List<RolePermission> mappings,
            Set<String> existingMappings,
            Role role,
            Permission permission
    ) {
        String key = role.getId() + ":" + permission.getId();
        if (existingMappings.add(key)) {
            mappings.add(new RolePermission(null, role, permission));
        }
    }
}
