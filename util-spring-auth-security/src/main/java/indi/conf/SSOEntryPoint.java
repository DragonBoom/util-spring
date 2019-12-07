package indi.conf;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import indi.properties.SecurityProperties;
import indi.properties.SecurityProperties.SSO;
import lombok.extern.slf4j.Slf4j;

/**
 * SSO 情景下的认证入口 <br>
 * 将请求+需要的参数转发到SSO认证服务器
 * 
 * @author DragonBoom
 *
 */
@Slf4j
public class SSOEntryPoint implements AuthenticationEntryPoint {
	private static final String API_PATH = "/api/sso/key";

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
	    sendRedirect2SSO(request, response);
	}
	
	protected boolean isValid(String key) {
		return key != null;// TODO
	}
	
	protected void sendRedirect2SSO(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// 校验
	    SSO ssoProperties = securityProperties.getSso();
		String ssoServerLocation = null;
		if (ssoProperties == null || (ssoServerLocation = ssoProperties.getServerLocation()) == null) {
			throw new IllegalArgumentException("已启用SSO，配置项 sucurity.sso.server-location 不能为空");
		}
		// 获取Session，若不存在Session，则创建并使用一个新的Session
		HttpSession session = request.getSession(true);
		
		// 获取原本想访问的路径，等完成登陆后会再访问该路径
		String host = request.getServerName();
		int port = request.getServerPort();
		String path = request.getServletPath();

		// 拼接url
		String redirectUrl = new StringBuilder(ssoServerLocation)
				.append(API_PATH)
				.append("?sessionId=").append(session.getId())
				.append("&forward=").append("http://").append(host).append(":").append(port).append(path)
				.toString();

		log.debug("重定向到指定路径  {}", redirectUrl);

		response.sendRedirect(redirectUrl); // 不能直接重定向，跨域重定向会拿不到cookie? TODO test
	}

	@Autowired
	SecurityProperties securityProperties;

}
