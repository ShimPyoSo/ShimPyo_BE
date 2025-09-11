package com.example.shimpyo.config;

import com.example.shimpyo.domain.auth.jwt.JwtAuthenticationEntryPoint;
import com.example.shimpyo.domain.auth.jwt.JwtAuthenticationFilter;
import com.example.shimpyo.domain.auth.jwt.JwtTokenFilter;
import com.example.shimpyo.domain.auth.jwt.JwtTokenProvider;
import com.example.shimpyo.domain.auth.repository.UserAuthRepository;
import com.example.shimpyo.domain.auth.service.AuthService;
import com.example.shimpyo.domain.auth.service.RedisService;
import com.example.shimpyo.domain.utils.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtils cookieUtils;
    private final RedisService redisService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new JwtAuthenticationFilter(jwtTokenProvider, cookieUtils, authenticationManager, redisService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.DELETE, "/api/user/auth").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/user/auth/password").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/tourlist/reviews").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/user/auth/password").permitAll()
                        .requestMatchers(
                                "/api/user/auth/**",
                                "/api/main/recommends",
                                "/api/tourlist/reviews",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/tourlist/category",
                                "/api/search",
                                "/api/search/autocomplete",
                                "/api/tourlist/detail",
                                "/api/course/share",
                                "/health-check")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtTokenFilter, JwtAuthenticationFilter.class)
                .addFilterAt(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://www.shimpyo.site"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}