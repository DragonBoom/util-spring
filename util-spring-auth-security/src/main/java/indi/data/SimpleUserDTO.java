package indi.data;

import java.util.Collection;

import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import indi.entity.SimpleUserDO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SimpleUserDTO implements UserDetails {
	private static final long serialVersionUID = -5922840796389096527L;
	
	private Collection<? extends GrantedAuthority> authorities;
	private String password;
	private String username;
	private String avatar;// 头像地址
	/*
	 * 以下字段都设为true，无视掉
	 */
	private boolean accountNonExpired = true;
	private boolean accountNonLocked = true;
	private boolean credentialsNonExpired = true;
	private boolean enabled = true;
	
	public SimpleUserDTO (String username, String password, Collection<? extends GrantedAuthority> authorities) {
		this.username = username;
		this.password = password;
		this.authorities = authorities;
	}
	
	public SimpleUserDTO() {
	}
	
	public static SimpleUserDTO of(SimpleUserDO entity) {
		SimpleUserDTO dto = new SimpleUserDTO();
		if (entity != null) {
			BeanUtils.copyProperties(entity, dto);
		}
		return dto;
	}
	
	public static SimpleUserDTO of(SimpleUserDO entity, String... authoritys) {
		SimpleUserDTO dto = of(entity);
		dto.setAuthorities(AuthorityUtils.createAuthorityList(authoritys));
		return dto;
	}
	
	public static SimpleUserDTO of(String username, String... authoritys) {
		SimpleUserDTO dto = new SimpleUserDTO();
		dto.setUsername(username);
		dto.setAuthorities(AuthorityUtils.createAuthorityList(authoritys));
		return dto;
	}

}
