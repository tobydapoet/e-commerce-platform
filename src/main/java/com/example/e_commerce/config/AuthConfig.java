package com.example.e_commerce.config;

import com.example.e_commerce.filter.JwtFilter;
import com.example.e_commerce.handler.OauthHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class AuthConfig {
    private final OauthHandler oauthHandler;
    private final JwtFilter jwtFilter;
    private static final String UNAUTHORIZED_RESPONSE = """
        {
          "code":"UNAUTHORIZED",
          "message":"Authentication required",
          "errors":null
        }
        """;

    private static final String FORBIDDEN_RESPONSE = """
        {
          "code":"FORBIDDEN",
          "message":"Access denied",
          "errors":null
        }
        """;

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            http
                    .csrf(csrf -> csrf
                            // CSRF is still enabled by default. It is ignored only for token-issuing
                            // endpoints and external webhooks because they do not use browser cookies.
                            .ignoringRequestMatchers(
                                    "/auth/**",
                                    "/webhooks/**"
                            )
                    )

                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/auth/**",
                                    "/oauth2/**",
                                    "/login/**",
                                    "/google/**",
                                    "/webhooks/**",
                                    "/error",
                                    "/test/**"
                            ).permitAll()
                            .anyRequest().authenticated()
                    )

                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint((request, response, ex) -> {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json");
                                response.getWriter().write(UNAUTHORIZED_RESPONSE);
                            })
                            .accessDeniedHandler((request, response, ex) -> {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json");
                                response.getWriter().write(FORBIDDEN_RESPONSE);
                            })
                    )

                    .oauth2Login(oauth2 -> oauth2
                            .loginPage("/login")
                            .successHandler(oauthHandler)
                            .authorizationEndpoint(auth -> auth.baseUri("/oauth2/authorize"))
                            .redirectionEndpoint(redirect -> redirect.baseUri("/login/oauth2/code/*"))
                    )

                    .formLogin(AbstractHttpConfigurer::disable)

                    .logout(logout -> logout
                            .logoutUrl("/auth/logout")
                            .permitAll()
                    );

            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        } catch (Exception ex) {
            throw new BeanCreationException("securityFilterChain", "Failed to configure security filter chain", ex);
        }
    }
}
