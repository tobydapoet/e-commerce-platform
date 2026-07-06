package com.example.e_commerce.service;

import com.example.e_commerce.constant.RoleType;
import com.example.e_commerce.constant.StoreStatus;
import com.example.e_commerce.dto.request.CreateStoreReq;
import com.example.e_commerce.dto.request.UpdatePhoneReq;
import com.example.e_commerce.dto.request.UpdateStoreReq;
import com.example.e_commerce.dto.response.StoreRes;
import com.example.e_commerce.entity.Store;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.BadRequestException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.exception.UnauthorizedException;
import com.example.e_commerce.mapper.StoreMapper;
import com.example.e_commerce.repository.StoreRepository;
import com.example.e_commerce.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepo;
    private final UploadService uploadService;
    private final MailService mailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final StoreMapper mapper;
    private final UserRoleService userRoleService;

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    @Transactional
    public Store create(CreateStoreReq req, User currentUser) {
        Store store = new Store();
        store.setName(req.getName());
        store.setPhone(req.getPhone());
        store.setOwner(currentUser);
        if(req.getDescription() != null) {
            store.setDescription(req.getDescription());
        }
        if(req.getLogo() != null) {
            String logo = uploadService.upload(req.getLogo(), "store_logo");
            store.setLogo(logo);
        }
        if(req.getBanner() != null) {
            String banner = uploadService.upload(req.getBanner(), "store_banner");
            store.setBanner(banner);
        }
        if(req.getEmail() == null) {
            store.setEmail(currentUser.getEmail());
        }
        else {
            store.setEmail(req.getEmail());
        }
        Store savedStore = storeRepo.save(store);
        userRoleService.updateUserRole(currentUser.getId(), RoleType.SELLER);
        return savedStore;
    }

    public Store findById(Long storeId) {
        return storeRepo.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found."));
    }

    public void update(Long storeId,UpdateStoreReq req, User currentUser) {
        Store store = findById(storeId);
        if (!store.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to update this store!");
        }
        if(req.getName() != null) {
            store.setName(store.getName());
        }
        if(req.getDescription() != null) {
            store.setDescription(req.getDescription());
        }
        if(req.getLogo() != null) {
            if(store.getLogo() != null) {
                uploadService.delete(store.getLogo());
            }
            String logo = uploadService.upload(req.getLogo(), "store_logo");
            store.setLogo(logo);
        }
        if(req.getBanner() != null) {
            if(store.getBanner() != null) {
                uploadService.delete(store.getBanner());
            }
            String banner = uploadService.upload(req.getBanner(), "store_banner");
            store.setBanner(banner);
        }
        storeRepo.save(store);
    }

    public void requestPhoneUpdateOtp(Long storeId, UpdatePhoneReq req, User currentUser) {
        Store store = findById(storeId);

        if (!store.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to update this store!");
        }

        String cooldownKey = "otp:store-phone:cooldown:" + storeId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new BadRequestException("Please wait before requesting another OTP");
        }

        String otp = OtpUtil.generateOtp();
        String otpKey = "otp:store-phone:" + storeId;
        String pendingKey = "otp:store-phone:pending:" + storeId;
        String attemptsKey = "otp:store-phone:attempts:" + storeId;

        redisTemplate.opsForValue().set(otpKey, otp, OTP_TTL);
        redisTemplate.opsForValue().set(pendingKey, req.getNewPhone(), OTP_TTL);
        redisTemplate.opsForValue().set(cooldownKey, "1", RESEND_COOLDOWN);
        redisTemplate.delete(attemptsKey);

        mailService.sendOtp(currentUser.getEmail(), otp);
    }

    @Transactional
    public Store verifyAndUpdatePhone(Long storeId, String otpInput, User currentUser) {
        Store store = findById(storeId);
        if (!store.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to update this store!");
        }

        String otpKey = "otp:store-phone:" + storeId;
        String pendingKey = "otp:store-phone:pending:" + storeId;
        String attemptsKey = "otp:store-phone:attempts:" + storeId;

        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        String pendingPhone = redisTemplate.opsForValue().get(pendingKey);

        if (storedOtp == null || pendingPhone == null) {
            throw new BadRequestException("OTP expired or not requested. Please request a new one.");
        }

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptsKey, OTP_TTL);
        }
        if (attempts != null && attempts > MAX_VERIFY_ATTEMPTS) {
            redisTemplate.delete(otpKey);
            redisTemplate.delete(pendingKey);
            throw new BadRequestException("Too many failed attempts. Please request a new OTP.");
        }

        if (!storedOtp.equals(otpInput)) {
            throw new BadRequestException("Invalid OTP");
        }

        store.setPhone(pendingPhone);
        Store saved = storeRepo.save(store);

        redisTemplate.delete(otpKey);
        redisTemplate.delete(pendingKey);
        redisTemplate.delete(attemptsKey);

        return saved;
    }

    public void updateStatus(Long storeId, StoreStatus status) {
        Store store = findById(storeId);
        store.setStatus(status);
        storeRepo.save(store);
    }

    public Page<StoreRes> search(String keyword, Pageable pageable) {
        return storeRepo.search(keyword, pageable).map(mapper::toStoreRes);
    }

    public List<StoreRes> getMyStores(User currentUser) {
        List<Store> stores = storeRepo.findByOwnerId(currentUser.getId());
        if (stores.isEmpty()) {
            throw new ResourceNotFoundException("You don't have any stores yet");
        }
        return stores.stream().map(mapper::toStoreRes).toList();
    }

    public void deactivate(Long storeId, User currentUser) {
        Store store = findById(storeId);
        if (!store.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to deactivate this store!");
        }
        store.setStatus(StoreStatus.INACTIVE);
        storeRepo.save(store);
    }
}
