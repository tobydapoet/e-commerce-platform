package com.example.e_commerce.service;

import com.example.e_commerce.dto.request.SessionReq;
import com.example.e_commerce.entity.Session;
import com.example.e_commerce.entity.User;
import com.example.e_commerce.repository.SessionRepository;
import com.example.e_commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepo;
    private final UserRepository userRepo;

    public Session create(SessionReq req) {
        Session session = new Session();
        session.setToken(req.getToken());
        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        session.setUser(user);
        session.setExpiresAt(req.getExpiresAt());
        sessionRepo.save(session);
        return session;
    }
}
