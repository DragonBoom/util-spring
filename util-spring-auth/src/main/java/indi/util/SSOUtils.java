package indi.util;

import java.util.Objects;

import javax.servlet.http.HttpSession;

import indi.data.SSOInfoDTO;

public class SSOUtils {

    public static final String SSO_INFO_ATTRIBUTE = "sso_info";
    
    public static final void setSSOAttribute(HttpSession httpSession, String sessionId, String forward) {
        httpSession.setAttribute(SSOUtils.SSO_INFO_ATTRIBUTE, new SSOInfoDTO(sessionId, forward));
    }
    
    public static final SSOInfoDTO getSSOAttribute(HttpSession httpSession) {
        Objects.requireNonNull(httpSession, "获取SSO属性时session不能为null");
        return (SSOInfoDTO) httpSession.getAttribute(SSO_INFO_ATTRIBUTE);
    }
}
