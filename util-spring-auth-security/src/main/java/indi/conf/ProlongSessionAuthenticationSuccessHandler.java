package indi.conf;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import lombok.extern.slf4j.Slf4j;

/**
 * 登陆成功后延长Cookie的持续时间
 * 
 * @author DragonBoom
 *
 */
@Slf4j
public class ProlongSessionAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * 大坑，浏览器不要设置退出时清空cookie！！！！！！！！！！！
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 设置延长时间版的 JSESSIONID Cookie
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval((int) TimeUnit.DAYS.toSeconds(30L));// 没必要主动设置Cookie？
//        
//        Cookie cookie = new Cookie("JSESSIONID", session.getId());
//        cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(30L));// 30 d
//        cookie.setPath("/");
//        
//        response.addCookie(cookie);
//        
//        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) authentication;
//        WebAuthenticationDetails details = new WebAuthenticationDetails(request);
//        authenticationToken.setDetails(details);
        
        
        log.debug("Authentication: {}", authentication);
        and(request, response, authentication);
        
    }

    /**
     * 继承后增加逻辑的入口
     */
    protected void and(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws SessionAuthenticationException {
    }
}
