package indi.data;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SSOInfoDTO implements Serializable {// 实现Serializable接口以表示支持序列化，从而得以序列化后存放到redis中
    private static final long serialVersionUID = 5922840796389096555L;
    
    private String sessionId;
    private String forward;// SSO成功后访问的路径
}
