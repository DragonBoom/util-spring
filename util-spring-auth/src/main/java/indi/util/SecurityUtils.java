package indi.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * SpringSecurity 的工具类
 * <br>
 * 额外扩展了一些功能
 * 
 * @author DragonBoom
 *
 */
public class SecurityUtils {
    
    /**
     * 根据权限令牌判断用户是否已经登陆
     */
    public static boolean isLogined(Authentication authentication) {
        // 若用户拥有任一匿名权限外的权限令牌，就视为已登陆
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return true;
    }
}
