package cn.viktorxh.fastrpc.core.serialization;

/**
 * @author Hezijian Xiao
 * @date 2020/8/29 9:54
 */
public interface RpcSerializer {

    <T> byte[] serialize(T obj);

    <T> Object deserialize(byte[] bytes, Class<T> clazz);
}
