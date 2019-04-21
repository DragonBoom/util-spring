package indi.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import indi.scanner.DiskScanner;

@Configuration
public class SpringStaticConf {

    @Bean
    public DiskScanner scanner() {
        return new DiskScanner();
    }
}
