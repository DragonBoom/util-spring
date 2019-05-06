package indi.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ConfigurationProperties("util.jpa")
@Getter
@Setter
@ToString
public class UtilJpaProperties {

    /**
     * 打印最终的SQL TODO: implement
     */
    private Boolean showFullSql;
    
    /**
     * 打印含参数的SQL TODO: implement
     */
    private Boolean showSql;
}
