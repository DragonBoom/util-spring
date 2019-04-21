import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

class Helper {

	@Test
	void test() {
		UsernamePasswordAuthenticationFilter f;
		AbstractUserDetailsAuthenticationProvider provider;// 目前达到的验证用户权限的终点，可能涉及将权限信息保存起来的逻辑
		HttpServletRequest request;
		SecurityContextHolder holder;
	}

}
