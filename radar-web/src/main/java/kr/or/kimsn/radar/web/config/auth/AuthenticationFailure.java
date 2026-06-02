package kr.or.kimsn.radar.web.config.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailure  extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "";

        if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 맞지 않습니다. 다시 확인하세요.";
        } else if (exception instanceof InternalAuthenticationServiceException) {
            errorMessage = "내부적으로 발생한 시스템 문제로 인해 요청을 처리할 수 없습니다. 관리자에게 문의하세요.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "계정이 존재하지 않습니다. 회원가입 진행 후 로그인 하세요.";
        } else if (exception instanceof AuthenticationCredentialsNotFoundException) {
            errorMessage = "인증 요청이 거부되었습니다. 관리자에게 문의하세요.";
        } else {
            errorMessage = "알 수 없는 이유로 로그인에 실패하였습니다. 관리자에게 문의하세요.";
        }

        System.out.println("errorMessage: " + errorMessage);

        // 쿼리 파라미터로 오류 메시지를 전달하거나 세션에 저장하는 방식 고려
//        request.getSession().setAttribute("errorMessage", errorMessage);
//        response.sendRedirect("/login?error=true&errorMessage="+errorMessage);
        // 📌 [안전] 한글 메시지는 세션에 저장하여 톰캣 헤더 영역과 분리합니다.
        request.getSession().setAttribute("errorMessage", errorMessage);

        // 📌 [안전] URL에는 영문과 기호만 들어가므로 0~255 범위를 벗어나지 않습니다.
        setDefaultFailureUrl("/login?error=true");

        super.onAuthenticationFailure(request, response, exception);
    }
}
