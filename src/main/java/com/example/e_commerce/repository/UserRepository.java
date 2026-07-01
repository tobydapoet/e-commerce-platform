package com.example.e_commerce.repository;

import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.constant.UserStatus;
import com.example.e_commerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
    SELECT u FROM User u
    WHERE (:keyword IS NULL OR :keyword = ''
            OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND u.status = :status
      AND (:roleName IS NULL OR EXISTS (
            SELECT 1 FROM UserRole ur
            WHERE ur.user = u AND ur.role.roleName = :roleName
      ))
    """)
    Page<User> search(@Param("keyword") String keyword,
                      @Param("roleName") RoleType roleName,
                      @Param("status") UserStatus status,
                      Pageable pageable);
}
