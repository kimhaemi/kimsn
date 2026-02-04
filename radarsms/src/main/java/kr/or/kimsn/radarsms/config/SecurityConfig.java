package kr.or.kimsn.radarsms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import kr.or.kimsn.radarsms.config.auth.AuthenticationFailure;
import kr.or.kimsn.radarsms.config.auth.AuthenticationSuccess;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity // 활성화. 스프링 시큐리시 필터가 스프링 필터체인에 등록됨.
public class SecurityConfig {

    private final AuthenticationSuccess authenticationSuccess;
    private final AuthenticationFailure authenticationFailure;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
        return authConfiguration.getAuthenticationManager();
    }

    @Bean
    // @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // csrf는 비활성화하고 /css, /index는 접속허용, /user/** url은 USER 권한만 가능으로 설정을 구성한다. 그리고
        // user를 생성해주고, password encoder를 bean으로 선언해준다.

        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/").authenticated()
                .antMatchers("/manage/**").authenticated()
                .antMatchers("/station/**").authenticated()
                .antMatchers("/stat/**").authenticated()
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/loginProc")
                .usernameParameter("userId")
                .passwordParameter("pwd")
                .successHandler(authenticationSuccess)
                // .defaultSuccessUrl("/", true)
                .failureHandler(authenticationFailure)
                // .failureUrl("/error")
                .permitAll()
                // .successForwardUrl("/")
                .and()
                .logout()
                .permitAll()
        // .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        // .logoutSuccessUrl("/login")

        // http
        // .csrf(AbstractHttpConfigurer::disable) // CSRF 공격에 대한 방어를 해제 합니다.
        // // .cors((cors)-> cors.disable())
        // //resources(css, js 등) 의 경우 securityContext 등에 대한 조회가 불필요 하므로 disable
        // // .requestMatchers(matchers -> matchers.antMatchers( "/**"))
        // //인증/인가 제외
        // .authorizeHttpRequests((authz)-> authz
        // // .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll() //JSP 등 컨트롤러에서
        // 화면 파일명을 리턴해 화면을 바꾸는 경우
        // // .antMatchers("/manager/**").permitAll()
        // // .antMatchers("/users/**").permitAll()
        // // .antMatchers("/station/**").permitAll()
        // // .antMatchers("/stat/**").permitAll()
        // .anyRequest().authenticated() // 어떠한 요청이라도 인증필요
        // )
        // .formLogin(login-> login
        // .loginPage("/login")
        // // .loginProcessingUrl("/loginProc") // [B] submit 받을 url
        // // .usernameParameter("userId") // [C] submit할 아이디
        // // .passwordParameter("password") // [D] submit할 비밀번호
        // .defaultSuccessUrl("/")
        // // .successHandler(authenticationSuccess)
        // // .failureUrl("/common/error")
        // // .permitAll()
        // )
        // .logout((logout) -> logout
        // .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        // .logoutSuccessUrl("/login")
        // .invalidateHttpSession(true))
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

    // WebSecurityCustomizer를 통해 Spring Security를 적용하지 않을 리소스를 설정한다.
    @Bean
    // @Order(1)
    public SecurityFilterChain exceptionSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .requestMatchers((matchers) -> matchers.antMatchers("/static/**"))
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll())
                .requestCache().disable()
                .securityContext().disable()
                .sessionManagement().disable();

        return http.build();
    }

    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    // CorsConfiguration configuration = new CorsConfiguration();
    // configuration.setAllowCredentials(false); // 쿠키를 받을건지
    // configuration.setAllowedOrigins(Arrays.asList("http://localhost:2023"));
    // configuration.setAllowedMethods(Arrays.asList("GET", "POST"));

    // configuration.addAllowedHeader("*");

    // UrlBasedCorsConfigurationSource source = new
    // UrlBasedCorsConfigurationSource();
    // source.registerCorsConfiguration("/**", configuration);
    // return source;

    // }
}
