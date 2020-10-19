package cn.viktorxh.fastrpc.core.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 21:45
 */
@Slf4j
public class RpcEncoder extends MessageToByteEncoder {
    private RpcSerializer serializer = new ProtostuffSerializer();
    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {

        if (genericClass.isInstance(msg)) {
            try {
                byte[] data = serializer.serialize(msg);
                out.writeInt(data.length);
                out.writeBytes(data);
            } catch (Exception e) {
//                log.error("Encode error: " + e.toString());
                e.printStackTrace();
            }
        }
    }
}
