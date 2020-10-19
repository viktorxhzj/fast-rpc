package cn.viktorxh.fastrpc.core.client;

import lombok.extern.slf4j.Slf4j;
import cn.viktorxh.fastrpc.core.commons.RpcFuture;
import cn.viktorxh.fastrpc.core.commons.RpcMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 3:38
 */
@Slf4j
public class RpcService implements InvocationHandler {
    private static final Class<?> ANNOTATION = RpcMethod.class;

    private RpcClient rpcClient;
    private Class<?> targetService;

    public RpcService(RpcClient rpcClient, Class<?> targetService) {
        this.rpcClient = rpcClient;
        this.targetService = targetService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == ANNOTATION) {
                RpcFuture rpcFuture = rpcClient.submitRequest(targetService, method, args);
                return rpcFuture.get();
            }
        }
        return null;
    }

    public RpcFuture asyncCall(String methodName, Object... args) {
        return rpcClient.submitRequest(targetService, methodName, args);
    }
}
