package com.example.e_commerce.service;

import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.constant.UserStatus;
import com.example.e_commerce.dto.request.UpdateUserReq;
import com.example.e_commerce.dto.response.UserSimpleRes;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.mapper.UserMapper;
import com.example.e_commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final UploadService uploadService;
    private final UserMapper mapper;

    public User findById(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void updateUser(UUID id, UpdateUserReq req) {
        User user = findById(id);
        if(req.getAvatar() != null) {
            if(user.getAvatar() != null) {
                uploadService.delete(user.getAvatar());
            }
            String avatar = uploadService.upload(req.getAvatar(),"user");
            user.setAvatar(avatar);
        }
        if (req.getName() != null) {
            user.setName(req.getName());
        }
        userRepo.save(user);
    }

    public void updateUserStatus(UUID id, UserStatus status) {
        User user = findById(id);
        if(status != user.getStatus()) {
            user.setStatus(status);
            userRepo.save(user);
        }
    }

    public Page<UserSimpleRes> search(String keyword, RoleType roleName, UserStatus status, Pageable pageable) {
        UserStatus effectiveStatus = (status != null) ? status : UserStatus.ACTIVE;
        return userRepo.search(keyword, roleName, effectiveStatus, pageable)
                .map(mapper::toUserSimpleRes);
    }

    public Page<UserSimpleRes> customerSearch(String keyword, Pageable pageable) {
        return userRepo.search(keyword, RoleType.CUSTOMER, UserStatus.ACTIVE, pageable)
                .map(mapper::toUserSimpleRes);
    }
}
