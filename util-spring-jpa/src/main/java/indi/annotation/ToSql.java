package indi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 本注解用于标记将要生成SQL语句的实体类属性
 * 
 * @author DragonBoom
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToSql {
    
    @AliasFor("comment")
    String value() default "";

    /**
     * 字段的注释
     */
    @AliasFor("value")
    String comment() default "";
}
