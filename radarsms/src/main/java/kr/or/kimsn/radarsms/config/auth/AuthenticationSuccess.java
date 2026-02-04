package kr.or.kimsn.radarsms.config.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccess implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        // AuthenticationException 발생 시 사용자의 요청 정보가 담긴 캐시가 세션에 저장됨 -> 로그인 성공 시 redirect
        // RequestCache requestCache = new HttpSessionRequestCache();
        // SavedRequest savedRequest = requestCache.getRequest(request, response);
        // String redirectUrl = savedRequest.getRedirectUrl();

        String userId = request.getParameter("userId");

        HttpSession session = request.getSession();
        session.setAttribute("userId", userId);
        Cookie cookie = new Cookie("userId", userId);
        cookie.setMaxAge(60 * 60); // 쿠키 유효 시간 : 1시간
        response.addCookie(cookie);

        response.sendRedirect("/");

        // log.info("loginsuccess");
        // response.sendRedirect("/");
    }

}
