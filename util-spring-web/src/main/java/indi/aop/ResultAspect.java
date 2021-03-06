package indi.aop;

import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import indi.data.Result;
import indi.exception.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 拦截控制器返回的Result，当Result.code不为空时，修改响应的状态值(status)为code
 * 
 * @author DragonBoom
 *
 */
@Aspect
@Component
@Slf4j
public class ResultAspect {

    public ResultAspect() {
        log.info("【启用切面】ResultAspect");
    }

    /**
     * 切面：所有indi.controller包下的bean的public方法
     */
    @Pointcut("execution(* indi.controller.*.*(..))")
    private void controllerOperation() {
    }

    @Around("execution(* indi.controller.*.*(..))") // 不能用 execution(public indi.controller.*.*(..))？可用execution(public * indi.controller.*.*(..))
    public Object doAround(ProceedingJoinPoint pjp) {
        Result<?> result = null;
        try {
            result = (Result<?>) pjp.proceed();
        } catch (Throwable e) {
            e = ExceptionUtils.findFirstCause(e);
            e.printStackTrace();
            result = ExceptionUtils.convert(e);
        }
        if (result == null) {
            return null;
        } else {
            if (result.getCode() != null) {
                response.setStatus(result.getCode());
            } else if (result.isError()) {
                response.setStatus(400);// 万能的400
            }
            return result;
        }
    }

    @Autowired
    private HttpServletResponse response;
}
