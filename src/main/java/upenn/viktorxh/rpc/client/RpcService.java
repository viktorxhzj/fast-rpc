package upenn.viktorxh.rpc.client;

import lombok.extern.slf4j.Slf4j;
import upenn.viktorxh.rpc.commons.RpcFuture;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 3:38
 */
@Slf4j
public class RpcService implements InvocationHandler {

    private RpcClient rpcClient;
    private Class<?> targetService;

    public RpcService(RpcClient rpcClient, Class<?> targetService) {
        this.rpcClient = rpcClient;
        this.targetService = targetService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        /*
        * 1 向rpcClient传入调用相关的参数
        * 2 使用rpcClient内部线程池包装Request
        * 3 阻塞等待结果返回
        * */
        RpcFuture rpcFuture = rpcClient.submitRequest(targetService, method, args);
        if (rpcFuture == null) {
            return null;
        }
        return rpcFuture.get();
    }

    public RpcFuture asyncCall(String methodName, Object... args) {

        return rpcClient.submitRequest(targetService, methodName, args);
    }
}
