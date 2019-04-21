package indi.conf;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * 简单的认证失败处理器
 * <p>
 * 将根据相应异常进行翻译并调用HttpServletResponse的sendError返回Spring默认的error视图
 * 
 * @author DragonBoom
 *
 */
public class SimpleErrorAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		if (exception instanceof BadCredentialsException) {
			response.sendError(401, "账号或密码不存在");

		} else if (exception == null) {
			/*
			 * 直接sendError，配合web的配置，将返回RestResult格式的json字符串（登陆请求Accept基本只会是json）
			 */
			response.sendError(500, "登陆服务器发生异常，请稍后再试 Ovo");
		} else {
			response.sendError(400, "登陆错误");
		}
	}

}
