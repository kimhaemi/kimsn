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
 * [Claude 2026-11-19] Spring Security 7 설정으로 재작성
 *
 * - 기존: 모든 경로 permitAll + CSRF 비활성화 → 비로그인으로 회원/문자/설정 API 호출 가능 (보안 이슈 S1)
 * - 변경: 로그인 화면·정적 리소스·오류 화면만 허용, 나머지는 인증 필요. CSRF 보호 활성화.
 *
 * 사용자 조회(UserSecurityService, @Service)와 BCryptPasswordEncoder Bean은
 * Spring Security가 자동으로 DaoAuthenticationProvider에 연결합니다.
 * (그래서 기존의 AuthenticationManagerBuilder 수동 등록 코드와 userSecurityService 필드는 제거)
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity // 활성화. 스프링 시큐리시 필터가 스프링 필터체인에 등록됨.
public class SecurityConfig {

    private final AuthenticationSuccess authenticationSuccess;
    private final AuthenticationFailure authenticationFailure;

    /** [Claude 2026-11-19] 인증 없이 접근 가능한 경로 */
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
        // csrf는 비활성화하고 /css, /index는 접속허용, /user/** url은 USER 권한만 가능으로 설정을 구성한다. 그리고
        // user를 생성해주고, password encoder를 bean으로 선언해준다.
        // [Claude 2026-11-19] 위 주석은 기존 설정 설명. 현재는 CSRF 활성화 + PUBLIC_PATHS 외 전부 인증 필요.

        // ===== [Claude 2026-11-19] 기존 코드 원문 보존 (Security 7에서 불필요하여 주석 처리) 시작 =====
        // // 📌 [안전] 인증 매니저 빌더를 꺼내어 서비스와 인코더를 한 번에 등록합니다. (순환 참조 원천 차단)
        // AuthenticationManagerBuilder authenticationManagerBuilder =
        //     http.getSharedObject(AuthenticationManagerBuilder.class);
        //
        // authenticationManagerBuilder
        //     .userDetailsService(userSecurityService)
        //     .passwordEncoder(passwordEncoder()); // 아래에 정의된 인코더 연결
        //
        // http
        //     .csrf().disable()
        //     .authorizeRequests()
//                .antMatchers("/manage/sms_send_result_nuri2").permitAll() //허용 경로
//            .antMatchers("/static/**").permitAll() //허용 경로
        //     .antMatchers("/**").permitAll()
//            .antMatchers("/").authenticated() //인증
//            .antMatchers("/manage/**").authenticated() //인증
//            .antMatchers("/station/**").authenticated() //인증
//            .antMatchers("/stat/**").authenticated() //인증
        //     .anyRequest().permitAll()
        // ===== [Claude 2026-11-19] 기존 코드 원문 보존 끝 =====

        http
            // [Claude 2026-11-19] CSRF 보호 활성화(기본값). 토큰은 Thymeleaf th:action 폼과 common.js(Ajax 헤더/폼 hidden)에서 전달
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
                .logoutUrl("/logout")               // [Claude 2026-11-19] POST + CSRF 토큰 (header.html 로그아웃 폼)
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "userId")
                .permitAll()
            );
        return http.build();
    }

    // WebSecurityConfig 클래스에 BCryptPasswordEncoder 클래스를 Bean으로 등록해서 Controller에 의존성
    // 주입을 받아서 위와 같이 사용하면 됩니다.
    // 해당 암호화 기능을 이용하지 않는다면 아래 설명할 Spring Security의 로그인 기능을 이용할 수 없습니다.
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 📌 타임리프 레이아웃 활성화를 위해 이 자리에 추가해도 완벽히 작동합니다.
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

}
