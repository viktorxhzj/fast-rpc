package upenn.viktorxh.rpc.client;

import upenn.viktorxh.rpc.commons.RpcRequest;
import upenn.viktorxh.rpc.commons.RpcResponse;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 23:32
 */
public interface RpcHook {
    void beforeRpc(RpcRequest rpcRequest);
    void afterRpc(RpcResponse rpcResponse);
}
