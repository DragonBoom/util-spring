package indi.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

import com.fasterxml.jackson.databind.ObjectMapper;

import indi.properties.SecurityProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * Security Web配置类<br>
 * 因为引入了SpringSecurity后就会自动载入相关配置类，因此不能简单地通过配置类来配置是否启用整个SpringSecurity模块
 * 
 * @author DragonBoom
 */
@EnableWebSecurity // <-| contains @Configuration
@Slf4j
public class WebSecurityConf extends WebSecurityConfigurerAdapter {

    // http 配置
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /*
         * 通用配置
         */

        // add filter
        // UsernamePasswordAuthenticationFilter
        /*
         * 自定义该拦截器后，相关的所有配置项都不能再照HttpSecurity的模式配置了， HttpSecurity的配置只会影响到实际并不会生效的原拦截器。。。
         */
        UsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter = new CustomUsernamePasswordAuthenticationFilter();
        // 配置登陆请求的匹配器(Matcher)
        customUsernamePasswordAuthenticationFilter
                .setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/auth", "POST"));

        // 配置 AuthenticationManager （用于验证并颁发权限令牌）
        customUsernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager);// important !!!

        // 配置登陆成功后转发的地址
        log.debug("Use AuthenticationSuccessHandler: {}", successHandler);
        customUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);// 登陆成功处理
        log.debug("Use AuthenticationFailureHandle: {}", failureHandler);
        // 配置登陆失败处理器，登陆失败时将被调用
        customUsernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(failureHandler);// 登录失败处理

        // SessionAuthenticationStrategy config 通过该类修改登陆成功的session逻辑
        // TODO 暂时不管

        /*
         * 配置认证入口 AuthenticationEntryPoint
         * 
         * 即配置 login from
         */

        // 设置所有请求的验证入口
        // 若只需要 403，可考虑使用 Http403ForbiddenEntryPoint
        http.exceptionHandling().defaultAuthenticationEntryPointFor(authEntryPoint, AnyRequestMatcher.INSTANCE);


        http
            .exceptionHandling()
                .defaultAuthenticationEntryPointFor(authEntryPoint, AnyRequestMatcher.INSTANCE)
                .and()
            .addFilterAt(customUsernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf()// csrf
                .disable()// 禁用跨域访问防护，暂时不考虑这个问题
//			.formLogin()// 登陆表单 由于自定义了拦截器，该配置事实上已失效
//				.loginPage("/login")
//				.failureForwardUrl("/error")
//				.loginProcessingUrl("/api/auth")
//				.successForwardUrl("/login/success")
//				.and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation()
                        .migrateSession()// 启用session迁移，登陆成功后修改sessionId（默认启用）
                    .and()
                ;

        /**
         * 其他配置
         */
        webSecurityCustomHelper.configure(http);
    }

    // 权限配置
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)// 使用自定义的方式管理用户权限
                .passwordEncoder(NoOpPasswordEncoder.getInstance())// 默认使用了Bcrypt加密 TODO 待了解
                .and();
    }

    /**
     * 用spring security提供的方式，将authencation manager 注册为bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 配置用于辅助自定义Seucrity的bean
     * 
     * @param authenticationManager
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public WebSecurityCustomHelper webSecurityCustomHelper(AuthenticationManager authenticationManager) {
        return new BasicWebSecurityCustomHelper();
    }
    
    @FunctionalInterface
    public interface WebSecurityCustomHelper {
        void configure(HttpSecurity http) throws Exception;
    }
    
    public static class BasicWebSecurityCustomHelper implements WebSecurityCustomHelper {

        /**
         * 重写实现该方法以自定义WebSecurity的逻辑
         */
        @Override
        public void configure(HttpSecurity http) throws Exception {
            log.info("启用Security的默认配置，任何请求都需要认证信息！");
            http
                .authorizeRequests()
                    .antMatchers("/redirect.html").permitAll()// 借助js实现跨域重定向
                    .anyRequest().authenticated();
        }
    }

    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private WebSecurityCustomHelper webSecurityCustomHelper;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AuthenticationFailureHandler failureHandler;
    @Autowired
    private AuthenticationSuccessHandler successHandler;
    @Autowired
    private AuthenticationEntryPoint authEntryPoint;

    @Autowired
    private ObjectMapper objectMapper;
}
