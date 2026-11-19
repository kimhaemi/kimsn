package kr.or.kimsn.radar.securitytest;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.View;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import kr.or.kimsn.radar.data.domain.Roles;
import kr.or.kimsn.radar.data.dto.MembersDto;
import kr.or.kimsn.radar.web.config.SecurityConfig;
import kr.or.kimsn.radar.web.config.auth.AuthenticationFailure;
import kr.or.kimsn.radar.web.config.auth.AuthenticationSuccess;
import kr.or.kimsn.radar.web.config.auth.PrincipalDetails;
import kr.or.kimsn.radar.web.config.auth.UserSecurityService;
import kr.or.kimsn.radar.web.controller.LoginController;

/* Claude 주석 (2026-11-19): 새로 작성한 파일 (이 파일의 모든 주석은 Claude 작성)
 *
 * Spring Security 설정(SecurityConfig) 동작 검증 — DB 없이 실행 가능 (6.3-C / 보안 S1 / ADMIN 권한 6.8-K).
 *
 * <p>테스트 구성
 * <ul>
 *   <li>@WebMvcTest: 웹 계층(MVC, Security 필터, Thymeleaf)만 띄움. DB/JPA는 띄우지 않음</li>
 *   <li>실제 운영 코드: SecurityConfig, AuthenticationSuccess/Failure, LoginController, PrincipalDetails,
 *       login.html·error/403.html 템플릿</li>
 *   <li>가짜(Mock): UserSecurityService (DB 회원 조회 대신 테스트 계정 tester(USER), admin1(ADMIN) 반환)</li>
 *   <li>패키지를 kr.or.kimsn.radar.securitytest 로 분리하고 내부 TestApp 을 설정으로 사용 →
 *       RadarWebApplication(@EnableJpaRepositories)이 로드되지 않아 DB 없이 실행됨</li>
 * </ul>
 * 실행: ./gradlew :radar-web:test
 */
@WebMvcTest
@Import({ SecurityConfig.class, AuthenticationSuccess.class, AuthenticationFailure.class, LoginController.class })
class SecurityConfigTest {

    /* Claude 주석: 테스트 전용 최소 설정. 이 클래스가 있으면 @WebMvcTest 가 RadarWebApplication 을 찾지 않는다. */
    @SpringBootConfiguration
    static class TestApp {
    }

    private static final String TEST_ID = "tester";          /* Claude 주석: 일반 사용자 (Roles.USER) */
    private static final String TEST_PW = "test-password-1234";
    private static final String ADMIN_ID = "admin1";         /* Claude 주석: 관리자 (Roles.ADMIN) */
    private static final String ADMIN_PW = "admin-password-5678";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UserSecurityService userSecurityService;

    private static MembersDto member(String id, String rawPw, Roles role) {
        MembersDto m = new MembersDto();
        m.setMemberId(id);
        m.setPassword(new BCryptPasswordEncoder().encode(rawPw));
        m.setRoles(role);
        return m;
    }

    /* Claude 주석: 실제 PrincipalDetails 와 같은 권한 이름("USER"/"ADMIN")을 가진 요청 사용자 */
    private static UserRequestPostProcessor asUser() {
        return user(TEST_ID).authorities(() -> "USER");
    }

    private static UserRequestPostProcessor asAdmin() {
        return user(ADMIN_ID).authorities(() -> "ADMIN");
    }

    @BeforeEach
    void setUpUser() {
        Map<String, MembersDto> accounts = Map.of(
            TEST_ID, member(TEST_ID, TEST_PW, Roles.USER),
            ADMIN_ID, member(ADMIN_ID, ADMIN_PW, Roles.ADMIN));
        /* Claude 주석: 위 2개 계정만 존재하고, 그 외 아이디는 "사용자 없음" (실제 UserSecurityService 동작과 동일) */
        given(userSecurityService.loadUserByUsername(anyString())).willAnswer(invocation -> {
            MembersDto m = accounts.get(invocation.<String>getArgument(0));
            if (m != null) {
                return new PrincipalDetails(m);
            }
            throw new UsernameNotFoundException("not found");
        });
    }

    /* Claude 주석: ---------------------------------------------------------------- 1. 공개 경로 */

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

    /* Claude 주석: ---------------------------------------------------------------- 2. 비로그인 차단 (S1) */

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

    /* Claude 주석: ---------------------------------------------------------------- 3. 로그인 사용자 (USER) */

    @Test
    @DisplayName("C-06 로그인 사용자라도 CSRF 토큰 없는 POST(설정 변경 API)는 403 으로 거부된다")
    void authenticatedPostWithoutCsrfIsForbidden() throws Exception {
        mvc.perform(post("/manage/station_status_modify").with(asUser()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("C-07 로그인 사용자 + CSRF 토큰 POST(설정 변경 API)는 보안 필터를 통과한다 (테스트에 컨트롤러가 없어 404)")
    void authenticatedPostWithCsrfPassesSecurity() throws Exception {
        mvc.perform(post("/manage/station_status_modify").with(asUser()).with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("C-08 로그인 사용자의 관리 화면(GET)은 보안 필터를 통과한다 (테스트에 컨트롤러가 없어 404)")
    void authenticatedGetPassesSecurity() throws Exception {
        mvc.perform(get("/manage/station_status").with(asUser()))
            .andExpect(status().isNotFound());
    }

    /* Claude 주석: ---------------------------------------------------------------- 4. 로그인 처리 (/loginProc) */

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

    /* Claude 주석: ---------------------------------------------------------------- 5. 로그아웃 */

    @Test
    @DisplayName("C-13 로그아웃(POST + CSRF) → /login?logout 으로 이동하고 userId 쿠키가 삭제된다")
    void logout() throws Exception {
        mvc.perform(post("/logout").with(asUser()).with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?logout"))
            .andExpect(cookie().maxAge("userId", 0));
    }

    @Test
    @DisplayName("C-14 CSRF 토큰 없는 로그아웃 요청 → 403 (강제 로그아웃 공격 차단)")
    void logoutWithoutCsrfIsForbidden() throws Exception {
        mvc.perform(post("/logout").with(asUser()))
            .andExpect(status().isForbidden());
    }

    /* Claude 주석: ---------------------------------------------------------------- 6. ADMIN 권한 (/users/**) */

    @Test
    @DisplayName("K-01 USER 계정으로 사용자 관리 화면(GET /users/admin_user) → 403")
    void userCannotOpenUserAdminPage() throws Exception {
        mvc.perform(get("/users/admin_user").with(asUser()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("K-02 USER 계정으로 회원 생성 API(POST /users/admin_user_create + CSRF) → 403 (USER가 ADMIN 계정을 만들 수 없음)")
    void userCannotCreateMember() throws Exception {
        mvc.perform(post("/users/admin_user_create").with(asUser()).with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("K-03 USER 계정으로 회원 수정·삭제 API → 403")
    void userCannotModifyOrDeleteMember() throws Exception {
        mvc.perform(post("/users/admin_user_modify").with(asUser()).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/users/admin_user_del").with(asUser()).with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("K-04 ADMIN 계정은 사용자 관리 화면·API 모두 보안 필터 통과 (테스트에 컨트롤러가 없어 404)")
    void adminCanAccessUserAdmin() throws Exception {
        mvc.perform(get("/users/admin_user").with(asAdmin()))
            .andExpect(status().isNotFound());
        mvc.perform(get("/users/admin_user_form").with(asAdmin()))
            .andExpect(status().isNotFound());
        mvc.perform(post("/users/admin_user_create").with(asAdmin()).with(csrf()))
            .andExpect(status().isNotFound());
        mvc.perform(post("/users/admin_user_del").with(asAdmin()).with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("K-05 ADMIN 계정도 그 외 화면(관리 화면) 사용 가능 (404 = 보안 통과)")
    void adminCanAccessOtherPages() throws Exception {
        mvc.perform(get("/manage/station_status").with(asAdmin()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("K-06 실제 로그인 흐름: admin1(ADMIN)로 /loginProc 로그인 후 세션으로 /users/admin_user → 통과(404)")
    void realLoginAdminCanAccessUserAdmin() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/loginProc").with(csrf())
                .param("userId", ADMIN_ID).param("pwd", ADMIN_PW))
            .andExpect(redirectedUrl("/"))
            .andReturn().getRequest().getSession(false);
        mvc.perform(get("/users/admin_user").session(session))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("K-07 실제 로그인 흐름: tester(USER)로 로그인 후 세션으로 /users/admin_user → 403")
    void realLoginUserCannotAccessUserAdmin() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/loginProc").with(csrf())
                .param("userId", TEST_ID).param("pwd", TEST_PW))
            .andExpect(redirectedUrl("/"))
            .andReturn().getRequest().getSession(false);
        mvc.perform(get("/users/admin_user").session(session))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("K-08 PrincipalDetails 권한 이름이 Roles 이름과 같다 (ADMIN→\"ADMIN\", USER→\"USER\") — hasAuthority(\"ADMIN\") 전제 확인")
    void principalAuthorityNames() {
        String admin = new PrincipalDetails(member("a", "x", Roles.ADMIN)).getAuthorities()
            .stream().map(GrantedAuthority::getAuthority).findFirst().orElse(null);
        String usr = new PrincipalDetails(member("u", "x", Roles.USER)).getAuthorities()
            .stream().map(GrantedAuthority::getAuthority).findFirst().orElse(null);
        assertEquals("ADMIN", admin);
        assertEquals("USER", usr);
    }

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    /* Claude 주석: 테스트 환경(MockMvc)에는 DispatcherServlet Bean이 없어 Boot의 오류 화면 연결(conventionErrorViewResolver)이
         만들어지지 않는다. 그래서 오류 화면 "연결"은 실제 실행에서 확인(문서 8.2)하고, 여기서는 템플릿 자체가 정상 렌더링되는지 확인한다 */
    @Test
    @DisplayName("K-09 권한 없음(403) 오류 화면 템플릿 error/403.html 이 Thymeleaf로 정상 렌더링된다")
    void forbiddenErrorTemplateRenders() throws Exception {
        View view = thymeleafViewResolver.resolveViewName("error/403", Locale.KOREAN);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/users/admin_user");
        MockHttpServletResponse res = new MockHttpServletResponse();
        view.render(Map.of(), req, res);
        String html = res.getContentAsString();
        assertTrue(html.contains("403 - Forbidden"), "403 문구");
        assertTrue(html.contains("관리자(ADMIN) 계정으로 로그인하세요"), "안내 문구");
    }
}
