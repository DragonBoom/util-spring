package indi.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.fasterxml.jackson.databind.ObjectMapper;

import indi.util.ApplicationContextAwareHelper;
import indi.util.ObjectMapperUtils;

@Configuration
@EnableAspectJAutoProxy
public class SpringCoreConf {

	@Bean
	public ApplicationContextAwareHelper applicationContextAwareHelper() {
		return new ApplicationContextAwareHelper();
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		return ObjectMapperUtils.getMapper();
	}
}
