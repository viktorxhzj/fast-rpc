package upenn.viktorxh.rpc.commons;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 1:36
 */
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 3391297713150902382L;

    private volatile byte status;
    private volatile UUID uuid;


    private int channelIndex;
    /* full class name */
    private String className;
    /* method to be called */
    private String methodName;
    /* method parameters */
    private Object[] methodParams;
    /* method parameters type */
    private Class<?>[] methodParamTypes;

    public RpcRequest() {
        uuid = UUID.randomUUID();
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    public RpcRequest setChannelIndex(int channelIndex) {
        this.channelIndex = channelIndex;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public RpcRequest setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public RpcRequest setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public byte getStatus() {
        return status;
    }

    public RpcRequest setStatus(byte status) {
        this.status = status;
        return this;
    }

    public RpcRequest setMethodParamsAndType(Object... args) {
        methodParams = args;
        setMethodParamTypes();
        return this;
    }

    private void setMethodParamTypes() {
        int len = methodParams.length;
        methodParamTypes = new Class<?>[len];
        for (int i = 0; i < len; i++) {
            methodParamTypes[i] = methodParams[i].getClass();
        }
    }

    public Object[] getMethodParams() {
        return methodParams;
    }

    public Class<?>[] getMethodParamTypes() {
        return methodParamTypes;
    }
}
