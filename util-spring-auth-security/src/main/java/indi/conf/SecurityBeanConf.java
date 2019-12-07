package indi.conf;

import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import indi.properties.SecurityProperties;

/**
 * 主要用于配置Bean的配置类，与{@link WebSecurityConf}区分开来
 * 
 * @author DragonBoom
 *
 */
@Configuration
public class SecurityBeanConf {

	/**
	 * 配置管理UserDetails的Bean
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public SimpleUserDetailsService simpleUserDetailsService() {
		return new SimpleUserDetailsService();
	}

	/**
	 * 配置文件解析类
	 */
	@Bean
	@ConditionalOnMissingBean
	public SecurityProperties securityProperties() {
		return new SecurityProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public AuthenticationFailureHandler failureHandler() {
		return new SimpleErrorAuthenticationFailureHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	public AuthenticationSuccessHandler successHandler(SecurityProperties securityProperties) {
		String successForwardUrl = securityProperties.getSuccessForwardUrl();
		Objects.requireNonNull(successForwardUrl, "配置项 security.success-forward-url 不能为空");
		return new ProlongSessionAuthenticationSuccessHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	public AuthenticationEntryPoint SSOEntryPoint() {
		return new SSOEntryPoint();
	}

}
