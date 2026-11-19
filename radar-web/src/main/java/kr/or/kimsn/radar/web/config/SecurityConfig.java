package kr.or.kimsn.radar.web.config;

import kr.or.kimsn.radar.web.config.auth.AuthenticationFailure;
import kr.or.kimsn.radar.web.config.auth.AuthenticationSuccess;
import lombok.RequiredArgsConstructor;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 7 설정 (2026-11-19 재작성)
 *
 * - 기존: 모든 경로 permitAll + CSRF 비활성화 → 비로그인으로 회원/문자/설정 API 호출 가능 (보안 이슈 S1)
 * - 변경: 로그인 화면·정적 리소스·오류 화면만 허용, 나머지는 인증 필요. CSRF 보호 활성화.
 *
 * 사용자 조회(UserSecurityService, @Service)와 BCryptPasswordEncoder Bean은
 * Spring Security가 자동으로 DaoAuthenticationProvider에 연결합니다.
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationSuccess authenticationSuccess;
    private final AuthenticationFailure authenticationFailure;

    /** 인증 없이 접근 가능한 경로 */
    private static final String[] PUBLIC_PATHS = {
        "/login",
        "/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico",
        "/error", "/common/error"
    };

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
        return authConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보호 활성화(기본값). 토큰은 Thymeleaf th:action 폼과 common.js(Ajax 헤더/폼 hidden)에서 전달
            .csrf(csrf -> { })
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/loginProc")
                .usernameParameter("userId")
                .passwordParameter("pwd")
                .successHandler(authenticationSuccess)
                .failureHandler(authenticationFailure)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")               // POST + CSRF 토큰 (header.html 로그아웃 폼)
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "userId")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 📌 타임리프 레이아웃 활성화
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

}
