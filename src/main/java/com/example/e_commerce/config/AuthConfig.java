package com.example.e_commerce.config;

import com.example.e_commerce.filter.JwtFilter;
import com.example.e_commerce.handler.OauthHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/oauth2/**",
                                "/login/**",
                                "/google/**",
                                "/error",
                                "/test/**"
                        )
                        .permitAll()

                        .anyRequest()
                        .authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("""
                                {
                                  "code":"UNAUTHORIZED",
                                  "message":"Authentication required",
                                  "errors":null
                                }
                            """);
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("""
                            {
                              "code":"FORBIDDEN",
                              "message":"Access denied",
                              "errors":null
                            }
                            """);
                        })
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")

                        .successHandler(oauthHandler)

                        .authorizationEndpoint(auth -> auth
                                .baseUri("/oauth2/authorize")
                        )

                        .redirectionEndpoint(redirect -> redirect
                                .baseUri("/login/oauth2/code/*")
                        )
                )

                .formLogin(AbstractHttpConfigurer::disable)

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .permitAll()
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}