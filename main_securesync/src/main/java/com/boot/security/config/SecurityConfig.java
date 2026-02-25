package com.boot.security.config;

import com.boot.security.provider.JwtAuthenticationFilter;
import com.boot.security.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 컨트롤러의 @PreAuthorize 등을 활성화합니다.
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. 공통 허용 경로
                        .requestMatchers("/api/users/join", "/api/users/login", "/api/users/reset-password").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/files/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/notices/files/**").permitAll()

                        // 2. 관리자/매니저 전용 권한
                        .requestMatchers(HttpMethod.DELETE, "/api/assets/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyRole("ADMIN", "MANAGER")
                        // 🌟 여기서 /api/notices/** DELETE 막던 것을 지웠습니다!

                        // 3. 기안서(Documents) 권한
                        .requestMatchers(HttpMethod.DELETE, "/api/documents/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/documents/**").authenticated()
                        .requestMatchers("/api/documents/**").authenticated()

                        // 4. 기타 권한
                        .requestMatchers("/api/projects/**").hasAnyRole("USER", "ADMIN", "MANAGER")
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/assets/**").authenticated()
                        .requestMatchers("/api/security/**").authenticated()
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers("/api/schedules/**").authenticated()

                        // 5. 🌟 새롭게 추가된 공지사항(게시판) 전체 접근 허용 (상세 권한은 Service에서 체크)
                        .requestMatchers("/api/notices/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}