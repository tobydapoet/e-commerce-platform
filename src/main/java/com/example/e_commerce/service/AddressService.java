package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.CreateAddressReq;
import com.example.e_commerce.dto.request.UpdateAddressReq;
import com.example.e_commerce.dto.response.AddressRes;
import com.example.e_commerce.entity.Address;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.exception.ForbiddenException;
import com.example.e_commerce.exception.ResourceNotFoundException;
import com.example.e_commerce.mapper.AddressMapper;
import com.example.e_commerce.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepo;
    private final AddressMapper mapper;

    @Transactional
    public Address create(CreateAddressReq req, User currentUser) {
        addressRepo.clearDefaultAddress(currentUser.getId());

        Address address = new Address();
        address.setFullAddress(req.getFullAddress());
        address.setName(req.getName());
        address.setPhone(req.getPhone());
        address.setUser(currentUser);
        address.setIsDefault(true);

        return addressRepo.save(address);
    }

    public Address findById(Long id) {
        return addressRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    public void delete(UUID userId, Long id) {
        Address address = findById(id);
        if (!address.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to delete this address");
        }
        addressRepo.delete(address);
    }

    public void update(UUID userId, Long id, UpdateAddressReq req) {
        Address address = findById(id);
        if (!address.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to update this address");
        }
        if(req.getFullAddress() != null) {
            address.setFullAddress(req.getFullAddress());
        }
        if(req.getName() != null) {
            address.setName(req.getName());
        }
        if(req.getPhone() != null) {
            address.setPhone(req.getPhone());
        }
        addressRepo.save(address);
    }

    public Page<AddressRes> findByUserId(UUID userId, Pageable pageable) {
        return addressRepo.findByUserId(userId, pageable)
                .map(mapper::toAddressRes);
    }

    public Page<AddressRes> findAll(Pageable pageable) {
        return addressRepo.findAll(pageable)
                .map(mapper::toAddressRes);
    }

    public AddressRes getById(UUID userId, Long id) {
        Address address = findById(id);
        if (!address.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to view this address");
        }

        return mapper.toAddressRes(address);
    }

    public void updateDefault(User currentUser, Long id) {
        Address address = findById(id);
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You don't have permission to view this address");
        }
        addressRepo.clearDefaultAddress(currentUser.getId());
        address.setIsDefault(true);
        addressRepo.save(address);
    }
}
