package cn.viktorxh.fastrpc.core.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 14:46
 */
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {
    private RpcSerializer serializer = new ProtostuffSerializer();

    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object obj;
        try {
            obj = serializer.deserialize(data, genericClass);
            out.add(obj);
        } catch (Exception e) {
            log.error("Decode error: " + e.toString());
        }
    }
}
