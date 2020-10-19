package cn.viktorxh.fastrpc.core.commons;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 1:36
 */
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = -2710594476684710651L;

    // 0x01 succeeded
    // 0x02 failed
    // 0x03 failed due to timeout
    private byte status;
    private UUID uuid;

    private int channelIndex;

    private Object result;

    public RpcResponse() {
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    public RpcResponse setChannelIndex(int channelIndex) {
        this.channelIndex = channelIndex;
        return this;
    }

    public RpcResponse setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public RpcResponse setResult(Object result) {
        this.result = result;
        return this;
    }

    public UUID getUuid() {
        return uuid;
    }

    public RpcResponse setStatus(byte status) {
        this.status = status;
        return this;
    }

    public byte getStatus() {
        return status;
    }

    public Object getResult() {
        return result;
    }
}
