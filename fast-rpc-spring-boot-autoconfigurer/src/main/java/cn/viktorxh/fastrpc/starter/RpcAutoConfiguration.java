package cn.viktorxh.fastrpc.starter;

import cn.viktorxh.fastrpc.core.client.RpcClient;
import cn.viktorxh.fastrpc.core.server.RpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Hezijian Xiao
 * @date 2020/9/1 19:11
 */
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcAutoConfiguration {

    @Autowired
    private RpcProperties rpcProperties;

    @Bean
    public RpcClient rpcClient() {
        return new RpcClient(rpcProperties.getHostname(), rpcProperties.getPort());
    }

    @Bean
    public RpcServer rpcServer() {
        return new RpcServer(rpcProperties.getHostname(), rpcProperties.getPort());
    }

}
