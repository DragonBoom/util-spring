package indi.conf;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import indi.scanner.DiskScanner;

@Configuration
public class SpringStaticConf {
    private static final int DEFAULT_MAX_ROUTE = Integer.MAX_VALUE;
    private static final int DEFAULT_MAX_PER_ROUTE = Integer.MAX_VALUE;

    @Bean
    public DiskScanner scanner() {
        return new DiskScanner();
    }
    
    @Bean
    public HttpClient poolingHttpClient() {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(DEFAULT_MAX_ROUTE);
        manager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
        return HttpClientBuilder.create().setConnectionManager(manager).build();
    }
}
