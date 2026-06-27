package com.example.e_commerce.handler;

import com.example.e_commerce.dto.request.LoginGoogleReq;
import com.example.e_commerce.dto.response.LoginRes;
import com.example.e_commerce.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OauthHandler implements AuthenticationSuccessHandler {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) auth.getPrincipal();
        LoginGoogleReq userReq = new LoginGoogleReq();
        userReq.setEmail(oAuth2User.getAttribute("email"));
        userReq.setName(oAuth2User.getAttribute("name"));
        userReq.setAvatarUrl(oAuth2User.getAttribute("picture"));
        userReq.setProvidedId(oAuth2User.getAttribute("sub"));

        LoginRes loginRes = authService.loginWithGoogle(userReq);

        res.setContentType("application/json");
        res.getWriter().write(objectMapper.writeValueAsString(loginRes));
        res.getWriter().flush();
    }
}
