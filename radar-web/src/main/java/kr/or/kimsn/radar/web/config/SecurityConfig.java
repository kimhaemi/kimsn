package kr.or.kimsn.radar.web.config;

import kr.or.kimsn.radar.web.config.auth.UserSecurityService;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import kr.or.kimsn.radar.web.config.auth.AuthenticationFailure;
import kr.or.kimsn.radar.web.config.auth.AuthenticationSuccess;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity // 활성화. 스프링 시큐리시 필터가 스프링 필터체인에 등록됨.
public class SecurityConfig {

    private final AuthenticationSuccess authenticationSuccess;
    private final AuthenticationFailure authenticationFailure;
    private final UserSecurityService userSecurityService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
        return authConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // csrf는 비활성화하고 /css, /index는 접속허용, /user/** url은 USER 권한만 가능으로 설정을 구성한다. 그리고
        // user를 생성해주고, password encoder를 bean으로 선언해준다.

        // 📌 [안전] 인증 매니저 빌더를 꺼내어 서비스와 인코더를 한 번에 등록합니다. (순환 참조 원천 차단)
        AuthenticationManagerBuilder authenticationManagerBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
            .userDetailsService(userSecurityService)
            .passwordEncoder(passwordEncoder()); // 아래에 정의된 인코더 연결

        http
            .csrf().disable()
            .authorizeRequests()
//                .antMatchers("/manage/sms_send_result_nuri2").permitAll() //허용 경로
//            .antMatchers("/static/**").permitAll() //허용 경로
            .antMatchers("/**").permitAll()
//            .antMatchers("/").authenticated() //인증
//            .antMatchers("/manage/**").authenticated() //인증
//            .antMatchers("/station/**").authenticated() //인증
//            .antMatchers("/stat/**").authenticated() //인증
            .anyRequest().permitAll()
            .and()
            .formLogin()
            .loginPage("/login")
            .loginProcessingUrl("/loginProc")
            .usernameParameter("userId")
            .passwordParameter("pwd")
            .successHandler(authenticationSuccess)
            .failureHandler(authenticationFailure)
            .permitAll()
            .and()
            .logout()
            .permitAll()
        ;
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
