package cn.viktorxh.fastrpc.core.commons;

import lombok.extern.slf4j.Slf4j;
import cn.viktorxh.fastrpc.core.client.RpcHook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 1:14
 */
@Slf4j
public class RpcFuture {

    /*
    * 0x00 initialized
    * 0x01 succeeded(completed)
    * 0x02 failed(completed)
    * 0x03 failed due to timeout(completed)
    *
    * after being woken up, RpcFuture must be in completed status.
    */
    private volatile byte status;

    private volatile RpcRequest request;
    private volatile RpcResponse response;

    private volatile List<RpcHook> listenersList;

    public RpcFuture() {
        listenersList = new ArrayList<>();
    }

    public void setRequest(RpcRequest request) {
        this.request = request;
        fireBeforeRpc();
    }

    public RpcRequest getRequest() {
        return request;
    }

    public void setResponse(RpcResponse response) {
        status = response.getStatus();
        this.response = response;
        int a = 1;
        RpcResponse r = response;

        int b = 2;
        signal();
        fireAfterRpc();
    }

    private void signal() {
        synchronized (this) {
            notify();
        }
    }

    public boolean isSuccess() {
        return status == 1;
    }

    public boolean isDone() {
        return status != 0;
    }

    public Object getNow() {
        switch (status) {
            case 0x00:
                log.info("RPC not completed");
                break;
            case 0x01:
                return response.getResult();
            case 0x02:
                log.info("RPC failed");
                break;
            case 0x03:
                log.info("RPC failed due to connection times out");
                break;
        }
        return null;
    }

    public Object get() {
        try {
            await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return getNow();
    }

    public Object get(long timeout) {
        try {
            if (await(timeout)) {
                return getNow();
            }
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public RpcFuture sync() throws InterruptedException {
        await();
        return this;
    }

    private void await() throws InterruptedException {
        if (status == 0) {
            synchronized (this) {
                if (status == 0) {
                    wait();
                }
            }
        }
    }

    private boolean await(long timeoutMillis) throws InterruptedException {
        if (status == 0) {
            synchronized (this) {
                if (status == 0) {
                    wait(timeoutMillis);
                }
            }
        }
        return status != 0;
    }

    private void fireBeforeRpc() {
        for (RpcHook listener :
                listenersList) {
            listener.beforeRpc(request);
        }
    }

    private void fireAfterRpc() {
        for (RpcHook listener :
                listenersList) {
            listener.afterRpc(response);
        }
    }

    public RpcFuture removeRpcHooks(RpcHook[] rpcHooks) {
        for (RpcHook listener :
                rpcHooks) {
            listenersList.remove(listener);
        }
        return this;
    }

    public RpcFuture removeRpcHook(RpcHook rpcHook) {
        listenersList.remove(rpcHook);
        return this;
    }

    public RpcFuture addRpcHooks(RpcHook[] rpcHooks) {
        Collections.addAll(listenersList, rpcHooks);
        return this;
    }

    public RpcFuture addRpcHook(RpcHook rpcHook) {
        listenersList.add(rpcHook);
        return this;
    }


}
