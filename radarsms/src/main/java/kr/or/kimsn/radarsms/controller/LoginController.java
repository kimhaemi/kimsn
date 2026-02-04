package kr.or.kimsn.radarsms.controller;

import javax.servlet.http.Cookie;
// import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

// import kr.or.kimsn.radarsms.service.MembersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {

    // private final MembersService membersService;
    // private final PasswordEncoder passwordEncoder;

    // 로그인 화면
    @RequestMapping(value = "/login", method = { RequestMethod.GET, RequestMethod.POST })
    // @GetMapping({ "/login" })
    public String login(HttpServletRequest req, HttpServletResponse res, ModelMap model) {
        String referrer = req.getHeader("Referer");
        req.getSession().setAttribute("prevPage", referrer);
        model.addAttribute("error", req.getAttribute("errorMessage"));
        // public String login() {
        return "views/login";
    }

    // 로그인 process
    // @PostMapping({ "/loginProc" })
    // public String loginProc(HttpServletRequest req, HttpServletResponse res,
    // ModelMap model) {

    // log.info("dfdsfsdfsfsfd");
    // HttpSession session = req.getSession();
    // String userId = req.getParameter("userId");
    // String password = req.getParameter("pwd");

    // MembersDto user = membersService.getUserId(userId);
    // log.info("user :::: " + user);

    // if( user == null ){
    // model.addAttribute("error", "등록된 사용자가 없습니다.");
    // return "views/login";
    // // return false;
    // }

    // log.info("userId :::: " + userId);
    // log.info("password :::: " + password);

    // if(!password.equals("") && passwordEncoder.matches(password,
    // user.getPassword())){
    // log.info("password success");
    // session.setAttribute("loginUser", user);
    // Cookie cookie = new Cookie("userId", String.valueOf(user.getMemberId()));
    // cookie.setMaxAge(60 * 1); // 쿠키 유효 시간 : 1시간
    // res.addCookie(cookie);

    // return "redirect:/";
    // } else {
    // model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
    // }

    // return "views/login";
    // }

    // 로그 아웃
    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("logout===========================");
        HttpSession session = request.getSession();
        session.invalidate();
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        log.info("session :: " + session);

        return "views/login";
    }

    // error page
    @GetMapping("/common/error")
    public String error() {
        log.info("dfsfsdfsfsdfsdfsdfsdfsfsdf");
        return "views/common/error";
    }
}
