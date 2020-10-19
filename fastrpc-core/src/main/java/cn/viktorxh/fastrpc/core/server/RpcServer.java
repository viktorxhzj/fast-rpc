package cn.viktorxh.fastrpc.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import cn.viktorxh.fastrpc.core.commons.RpcRequest;
import cn.viktorxh.fastrpc.core.commons.RpcResponse;
import cn.viktorxh.fastrpc.core.serialization.RpcDecoder;
import cn.viktorxh.fastrpc.core.serialization.RpcEncoder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 21:47
 */
@Slf4j
public class RpcServer {
    private String hostname;
    private int port;

    private volatile Map<String, Class<?>> interfaceMapping;

    private class RpcServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            final RpcRequest request = (RpcRequest) msg;
            ctx.channel().eventLoop().execute(() -> {
                try {
                    String className = request.getClassName();
                    String methodName = request.getMethodName();
                    Class<?> implClass = interfaceMapping.get(className);
                    Object implInstance = implClass.newInstance();
                    Method method = implClass.getDeclaredMethod(methodName, request.getMethodParamTypes());
                    Object result = method.invoke(implInstance, request.getMethodParams());
                    RpcResponse response = new RpcResponse()
                            .setStatus((byte) 0x01)
                            .setChannelIndex(request.getChannelIndex())
                            .setUuid(request.getUuid())
                            .setResult(result);

                    ctx.writeAndFlush(response).sync();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
            log.warn("anomal closure of channel by client");
        }
    }


    public RpcServer(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        interfaceMapping = new HashMap<>();
    }

    public void registerService(Class<?> clazz) {
        Class<?>[] superInterfaces = clazz.getInterfaces();
        interfaceMapping.put(superInterfaces[0].getName(), clazz);
    }

    // block here
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new RpcEncoder(RpcResponse.class)) // outbound
                                    .addLast(new RpcDecoder(RpcRequest.class)) // inbound
                                    .addLast(new RpcServerHandler()); // inbound
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true);

            ChannelFuture future = serverBootstrap.bind(hostname, port).sync();
            // main thread block here
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bossGroup.shutdownGracefully().sync();
                workGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
