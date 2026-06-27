package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.AddressReq;
import com.example.e_commerce.entity.Address;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepo;
    private final UserService userService;

    public Address create(AddressReq req) {
        Address address = new Address();
        address.setAddress(req.getAddress());
        address.setName(req.getName());
        address.setPhone(req.getPhone());
        User user = userService.findById(req.getUserId());
        address.setUser(user);
        return addressRepo.save(address);
    }
}
