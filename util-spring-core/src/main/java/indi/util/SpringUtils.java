package indi.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import com.google.common.base.Strings;

import indi.exception.WrapperException;
import lombok.SneakyThrows;

public class SpringUtils {

	public static final <T> T getBean(Class<T> targetClass) {
		return ApplicationContextAwareHelper.getContext().getBean(targetClass);
	}

	@SuppressWarnings("unchecked")
    public static final <T> T getBean(String beanName) {
		return (T) ApplicationContextAwareHelper.getContext().getBean(beanName);
	}
	
    /**
     * 格式化注解。根据Spring的一些机制更新注解。
     * 
     * <p>目前实现了以下功能:
     * <ul>
     * <li>对于有AliasFor注解的方法，将把该注解方法的值赋予AliasFor指定的注解方法</li>
     * </ul>
     *  
     * @param annotation
     */
    @SuppressWarnings("unchecked")
    public static final void formatAnnotation(Annotation annotation) {
	    Class<? extends Annotation> annotationType = annotation.annotationType();
	    Method[] methods = annotationType.getDeclaredMethods();
	    InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);// AnnotationInvocationHandler 
	    
	    // 利用反射修改属性
	    Field declaredField;
        try {
            declaredField = invocationHandler.getClass().getDeclaredField("memberValues");
        } catch (NoSuchFieldException | SecurityException e1) {
            throw new WrapperException(e1);
        }
	    declaredField.setAccessible(true);// 设置可访问private final修饰符修饰的值域 TODO: 这个方法的使用是否有需要注意的地方？
	    Map<String, Object> memberValues;
        try {
            memberValues = (Map<String, Object>) declaredField.get(invocationHandler);
        } catch (IllegalArgumentException | IllegalAccessException e1) {
            throw new WrapperException(e1);
        }
	    for (Method method : methods) {
	        // 找出AliasFor注解的属性，将属性值赋予AliasFor指定的属性
	        AliasFor aliasFor = method.getAnnotation(AliasFor.class);
	        if (aliasFor != null) {
	            String aliasName = Optional.ofNullable(aliasFor.value()).orElse(aliasFor.attribute());// 读AliasFor指定的属性
	            if (aliasName != null) {// 用if != null 的话，之后扩展就直接在后面累加，不必修改原有逻辑
	                Object value = null;
	                try {
	                    value = method.invoke(annotation);
	                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
	                    throw new WrapperException(e);
	                }
	                if (value != null
	                        || (value instanceof String && !Strings.isNullOrEmpty((@Nullable String) value))) {
	                    memberValues.put(aliasName, value);
	                }
	            }
	        }
	        continue;
        }
	}
	
    /**
     * 根据给定的配置文件的路径，获得PropertySource（配置项源，可用于读取配置项）
     * 
     * @param classPathResourcePath
     * @return
     * @since 2022-10-14
     */
    @SneakyThrows
    public static final PropertySource<?> getPropertySource(String classPathResourcePath) {
        DefaultPropertySourceFactory factory = new DefaultPropertySourceFactory();
        PropertySource<?> propertySource = factory.createPropertySource(null,
                new EncodedResource(new ClassPathResource(classPathResourcePath)));
        return propertySource;
    }
}
