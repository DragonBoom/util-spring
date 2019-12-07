package indi.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ConfigurationProperties("file")
@Component
@Getter
@Setter
@ToString
public class FileProperties {
    /**上传持久化路径*/
    private String uploadPath;
}
