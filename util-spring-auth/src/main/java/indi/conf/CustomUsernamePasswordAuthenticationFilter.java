package indi.conf;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.google.common.io.CharStreams;

import indi.data.SimpleUserDTO;
import indi.exception.WrapperException;
import indi.util.ObjectMapperUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 实现该类以控制Spring Security获取账号/密码的方式
 * 
 * @author DragonBoom
 *
 */
@Slf4j
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static final String AUTH_USER = "auth-user";

	public CustomUsernamePasswordAuthenticationFilter() {
	}

	@Override
	protected String obtainPassword(HttpServletRequest request) {
		SimpleUserDTO dto = getUserDTO(request);
		return dto.getPassword();
	}

	@Override
	protected String obtainUsername(HttpServletRequest request) {
		SimpleUserDTO dto = getUserDTO(request);
		return dto.getUsername();
	}

	/**
	 * 因为需要重写的两个方法，都需要读取requestBody，并且requestBody只能读一次，所以用request.attribute(ThreadLocal)来进行方法间的传参
	 * 
	 * @param request
	 * @return
	 */
	private SimpleUserDTO getUserDTO(HttpServletRequest request) {
		SimpleUserDTO simpleUserDTO = (SimpleUserDTO) request.getAttribute(AUTH_USER);

		if (simpleUserDTO == null) {
			try {
				String requestBody = CharStreams.toString(request.getReader());// once only
				log.debug("CustomUsernamePasswordAuthenticationFilter filte request body {}", requestBody);
				simpleUserDTO = ObjectMapperUtils.getMapper().readValue(requestBody, SimpleUserDTO.class);
				request.setAttribute(AUTH_USER, simpleUserDTO);
			} catch (IOException e) {
				throw new WrapperException(e);// do not affect threadLocal
			}
		}
		return simpleUserDTO;
	}

	@Override
	protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
		authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
	}
}
