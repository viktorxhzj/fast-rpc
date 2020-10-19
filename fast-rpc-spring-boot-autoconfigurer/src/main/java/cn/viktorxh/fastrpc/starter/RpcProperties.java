package cn.viktorxh.fastrpc.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Hezijian Xiao
 * @date 2020/9/1 19:07
 */
@ConfigurationProperties(prefix = "fastrpc")
@Data
public class RpcProperties {
    private String hostname;
    private int port;
}
