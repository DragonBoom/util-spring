package indi.conf;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import indi.dao.SimpleUserDao;
import indi.data.SimpleUserDTO;
import indi.entity.SimpleUserDO;
import lombok.extern.slf4j.Slf4j;

/**
 * 实现该类以控制Security获取用户详情（用于验证密码）的逻辑
 * 
 * @author DragonBoom
 *
 */
@Slf4j
public class SimpleUserDetailsService implements UserDetailsService{

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		SimpleUserDO simpleUserEntity = simpleUserDao.createQuery().with("username", username).singleResult();
		log.debug("{} {}", username, simpleUserEntity);
		if (simpleUserEntity == null) {
			log.debug("user not found");
			throw new UsernameNotFoundException("用户不存在");
		}
		// 为用户设置权限
		
		
		return SimpleUserDTO.of(simpleUserEntity, "ROLE_USER");
	}

	@Autowired
	private SimpleUserDao simpleUserDao;
	@Autowired
	protected EntityManager entityManager;
}
