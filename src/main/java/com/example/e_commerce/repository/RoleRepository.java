package com.example.e_commerce.repository;

import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<com.example.e_commerce.entity.Role, Long> {
    Optional<Role> findByRoleName(RoleType role);
}
