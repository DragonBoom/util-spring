package indi.aop;

import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import indi.data.RestResult;
import indi.util.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 当拦截控制器返回的Result为error时，同步修改响应的状态值(status)
 * 
 * @author DragonBoom
 *
 */
@Aspect
@Component
@Slf4j
public class RestResultAspect {

    public RestResultAspect() {
        log.info("【启用切面】ErrorResultAspect");
    }

    /**
     * 切面：所有indi.controller包下的bean的public方法
     */
    @Pointcut("execution(* indi.controller.*.*(..))")
    private void controllerOperation() {
    }

    @Around("execution(* indi.controller.*.*(..))") // 不能用 execution(public indi.controller.*.*(..))？
    public Object doAround(ProceedingJoinPoint pjp) {
        RestResult<?> result = null;
        try {
            result = (RestResult<?>) pjp.proceed();
        } catch (Throwable e) {
            e = ExceptionUtils.findFirstCause(e);
            e.printStackTrace();
            result = ExceptionUtils.convert(e);
        }
        if (result == null) {
            return null;
        } else {
            response.setStatus(result.getCode());
            return result;
        }
    }

    @Autowired
    private HttpServletResponse response;
}
