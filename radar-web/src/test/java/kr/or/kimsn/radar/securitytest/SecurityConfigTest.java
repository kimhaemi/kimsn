package kr.or.kimsn.radar.securitytest;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import kr.or.kimsn.radar.data.domain.Roles;
import kr.or.kimsn.radar.data.dto.MembersDto;
import kr.or.kimsn.radar.web.config.SecurityConfig;
import kr.or.kimsn.radar.web.config.auth.AuthenticationFailure;
import kr.or.kimsn.radar.web.config.auth.AuthenticationSuccess;
import kr.or.kimsn.radar.web.config.auth.PrincipalDetails;
import kr.or.kimsn.radar.web.config.auth.UserSecurityService;
import kr.or.kimsn.radar.web.controller.LoginController;

/**
 * Spring Security 설정(SecurityConfig) 동작 검증 — DB 없이 실행 가능 (2026-11-19, 6.3-C / 보안 S1).
 *
 * <p>테스트 구성
 * <ul>
 *   <li>@WebMvcTest: 웹 계층(MVC, Security 필터, Thymeleaf)만 띄움. DB/JPA는 띄우지 않음</li>
 *   <li>실제 운영 코드: SecurityConfig, AuthenticationSuccess/Failure, LoginController, login.html 템플릿</li>
 *   <li>가짜(Mock): UserSecurityService (DB 회원 조회 대신 테스트 계정 반환)</li>
 *   <li>패키지를 kr.or.kimsn.radar.securitytest 로 분리하고 내부 TestApp 을 설정으로 사용 →
 *       RadarWebApplication(@EnableJpaRepositories)이 로드되지 않아 DB 없이 실행됨</li>
 * </ul>
 * 실행: ./gradlew :radar-web:test
 */
@WebMvcTest
@Import({ SecurityConfig.class, AuthenticationSuccess.class, AuthenticationFailure.class, LoginController.class })
class SecurityConfigTest {

    /** 테스트 전용 최소 설정. 이 클래스가 있으면 @WebMvcTest 가 RadarWebApplication 을 찾지 않는다. */
    @SpringBootConfiguration
    static class TestApp {
    }

    private static final String TEST_ID = "tester";
    private static final String TEST_PW = "test-password-1234";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserSecurityService userSecurityService;

    @BeforeEach
    void setUpUser() {
        MembersDto member = new MembersDto();
        member.setMemberId(TEST_ID);
        member.setPassword(new BCryptPasswordEncoder().encode(TEST_PW));
        member.setRoles(Roles.USER);
        // 테스트 계정(tester)만 존재하고, 그 외 아이디는 "사용자 없음" (실제 UserSecurityService 동작과 동일)
        given(userSecurityService.loadUserByUsername(anyString())).willAnswer(invocation -> {
            if (TEST_ID.equals(invocation.getArgument(0))) {
                return new PrincipalDetails(member);
            }
            throw new UsernameNotFoundException("not found");
        });
    }

    // ---------------------------------------------------------------- 1. 공개 경로

    @Test
    @DisplayName("C-01 로그인 화면은 비로그인으로 열리고, CSRF hidden 토큰과 /loginProc action 이 포함된다")
    void loginPageIsPublicAndHasCsrfToken() throws Exception {
        mvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("name=\"_csrf\"")))
            .andExpect(content().string(containsString("action=\"/loginProc\"")));
    }

    @Test
    @DisplayName("C-02 정적 리소스(css)는 비로그인으로 접근 가능하다")
    void staticResourceIsPublic() throws Exception {
        mvc.perform(get("/css/radar/common.css"))
            .andExpect(status().isOk());
    }

    // ---------------------------------------------------------------- 2. 비로그인 차단 (S1)

    @Test
    @DisplayName("C-03 비로그인으로 관리 화면(GET) 접근 시 /login 으로 이동한다")
    void protectedPageRedirectsToLogin() throws Exception {
        mvc.perform(get("/manage/station_status"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("C-04 비로그인 + CSRF 토큰 없는 POST(회원 삭제 API)는 403 으로 거부된다")
    void anonymousPostWithoutCsrfIsForbidden() throws Exception {
        mvc.perform(post("/users/admin_user_del"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("C-05 비로그인 + CSRF 토큰 있는 POST(회원 삭제 API)도 /login 으로 이동한다 (API 실행 안 됨)")
    void anonymousPostWithCsrfRedirectsToLogin() throws Exception {
        mvc.perform(post("/users/admin_user_del").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    // ---------------------------------------------------------------- 3. 로그인 사용자

    @Test
    @DisplayName("C-06 로그인 사용자라도 CSRF 토큰 없는 POST 는 403 으로 거부된다")
    void authenticatedPostWithoutCsrfIsForbidden() throws Exception {
        mvc.perform(post("/users/admin_user_del").with(user(TEST_ID)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("C-07 로그인 사용자 + CSRF 토큰 POST 는 보안 필터를 통과한다 (테스트에 컨트롤러가 없어 404)")
    void authenticatedPostWithCsrfPassesSecurity() throws Exception {
        mvc.perform(post("/users/admin_user_del").with(user(TEST_ID)).with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("C-08 로그인 사용자의 관리 화면(GET)은 보안 필터를 통과한다 (테스트에 컨트롤러가 없어 404)")
    void authenticatedGetPassesSecurity() throws Exception {
        mvc.perform(get("/manage/station_status").with(user(TEST_ID)))
            .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- 4. 로그인 처리 (/loginProc)

    @Test
    @DisplayName("C-09 올바른 아이디/비밀번호 + CSRF → / 로 이동하고 userId 쿠키가 설정된다 (AuthenticationSuccess)")
    void loginSuccess() throws Exception {
        mvc.perform(post("/loginProc").with(csrf()).param("userId", TEST_ID).param("pwd", TEST_PW))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(cookie().value("userId", TEST_ID));
    }

    @Test
    @DisplayName("C-10 틀린 비밀번호 → /login?error=true 로 이동한다 (AuthenticationFailure)")
    void loginFailWrongPassword() throws Exception {
        mvc.perform(post("/loginProc").with(csrf()).param("userId", TEST_ID).param("pwd", "wrong"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @DisplayName("C-11 없는 아이디 → /login?error=true 로 이동한다")
    void loginFailUnknownUser() throws Exception {
        mvc.perform(post("/loginProc").with(csrf()).param("userId", "nobody").param("pwd", TEST_PW))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @DisplayName("C-12 CSRF 토큰 없이 로그인 요청 → 403 (외부 사이트의 위조 로그인 차단)")
    void loginWithoutCsrfIsForbidden() throws Exception {
        mvc.perform(post("/loginProc").param("userId", TEST_ID).param("pwd", TEST_PW))
            .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- 5. 로그아웃

    @Test
    @DisplayName("C-13 로그아웃(POST + CSRF) → /login?logout 으로 이동하고 userId 쿠키가 삭제된다")
    void logout() throws Exception {
        mvc.perform(post("/logout").with(user(TEST_ID)).with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?logout"))
            .andExpect(cookie().maxAge("userId", 0));
    }

    @Test
    @DisplayName("C-14 CSRF 토큰 없는 로그아웃 요청 → 403 (강제 로그아웃 공격 차단)")
    void logoutWithoutCsrfIsForbidden() throws Exception {
        mvc.perform(post("/logout").with(user(TEST_ID)))
            .andExpect(status().isForbidden());
    }
}
