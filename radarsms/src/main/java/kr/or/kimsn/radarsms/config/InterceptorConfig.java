package kr.or.kimsn.radarsms.config;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class InterceptorConfig implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Cookie[] cookies = request.getCookies();
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect("/login");
        }
        // if (cookies == null) {
        // response.sendRedirect("/login");
        // return false;
        // }
        // String servletPath = request.getRequestURI();
        // try {
        // if (!(servletPath.contains(".css")
        // || servletPath.contains(".js")
        // || servletPath.contains(".map")
        // || servletPath.contains(".jpg")
        // || servletPath.contains(".jpeg")
        // || servletPath.contains(".png")
        // || servletPath.contains(".gif")
        // || servletPath.contains(".ico")
        // || servletPath.contains(".zip")
        // || servletPath.contains(".cur")
        // || servletPath.contains(".eot")
        // || servletPath.contains(".ttf")
        // || servletPath.contains(".woff")
        // || servletPath.contains(".swf")
        // || servletPath.contains(".pdf")
        // || servletPath.contains(".css")
        // || servletPath.contains(".mp4")
        // || servletPath.contains(".ogv")
        // ) && servletPath.lastIndexOf(".") < 0) {

        // }
        // } catch (Exception e) {
        // // logger.error("Interceptor Error : [{}]", e.getMessage());
        // }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        // String servletPath = request.getRequestURI();
        // //index 페이지 default 페이지 설정
        // if ("/".equals(servletPath)) {
        // modelAndView.addObject("defaultSuccessUrl", defaultSuccessUrl);
        // }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
    }

}
