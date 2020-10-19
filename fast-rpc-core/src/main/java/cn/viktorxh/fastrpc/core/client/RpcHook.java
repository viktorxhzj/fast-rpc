package cn.viktorxh.fastrpc.core.client;

import cn.viktorxh.fastrpc.core.commons.RpcRequest;
import cn.viktorxh.fastrpc.core.commons.RpcResponse;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 23:32
 */
public interface RpcHook {
    void beforeRpc(RpcRequest rpcRequest);
    void afterRpc(RpcResponse rpcResponse);
}
