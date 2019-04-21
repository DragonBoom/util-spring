package indi.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ConfigurationProperties(prefix = "security")
@Getter
@Setter
@ToString
public class SecurityProperties {

	/**
	 * 配置登陆成功后转发的地址
	 */
	private String successForwardUrl="/";
	
	private SSO sso;
	
	@Getter
	@Setter
	@ToString
	public static class SSO {
		
		/**
		 * 配置 SSO 认证服务器的地址 
		 */
		private String serverLocation;
		
		private String biz;
	}
	
}
