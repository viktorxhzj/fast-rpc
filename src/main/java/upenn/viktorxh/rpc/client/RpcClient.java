package upenn.viktorxh.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import upenn.viktorxh.rpc.commons.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 3:37
 */
@Slf4j
public class RpcClient {
    private static final int CONNECTIONS = 8;
    private static final RpcResponse IDLE_RESPONSE = new RpcResponse().setStatus((byte) 0x03);
    private static final RpcResponse FAIL_RESPONSE = new RpcResponse().setStatus((byte) 0x02);

    private String hostname;
    private int port;
    private volatile RpcConnection[] rpcConnections;
    private int connectionIdx = -1; // index for connections round-robin
    private Map<Class<?>, Object> syncServiceMap;
    private Map<Class<?>, RpcService> asyncServiceMap;
    private volatile Map<UUID, RpcFuture> futureMap;
    private ThreadPoolExecutor executor;
    private AtomicInteger at = new AtomicInteger(0);

    private class RpcClientHandler extends ChannelInboundHandlerAdapter {
        // eventloop thread
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            final RpcResponse response = (RpcResponse) msg;

            // a successful reponse
            ctx.channel().eventLoop().execute(() -> {
                UUID uuid = response.getUuid();
                // retrieve the corresponding rpcFuture
                RpcFuture rpcFuture = futureMap.remove(uuid);
                // detach the rpcRequest from the connection
                int index = response.getChannelIndex();
                rpcConnections[index].removeRequestId(uuid);

                rpcFuture.setResponse(response);
            });
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                log.info("connection timesout, closing");
                int i = 0;
                // find all the rpcfuture related to this channel
                for (; i < CONNECTIONS; i++) {
                    if (rpcConnections[i].getChannel() == ctx.channel()) {
                        Set<UUID> requestIdSet = rpcConnections[i].getRequestIdSet();
                        for (UUID uuid : requestIdSet) {
                            RpcFuture failedFuture = futureMap.remove(uuid);
                            failedFuture.setResponse(IDLE_RESPONSE);
                        }
                        requestIdSet.clear();
                        break;
                    }
                }
                rpcConnections[i].close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
            log.warn("anomal closure of channel by server");
        }
    }

    // main
    public RpcClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        syncServiceMap = new HashMap<>();
        asyncServiceMap = new HashMap<>();
        futureMap = new ConcurrentHashMap<>();
        executor = new ThreadPoolExecutor(4, 8,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        initNettyClient(0);
    }

    public RpcClient(String hostname, int port, long idleTime) {
        this.hostname = hostname;
        this.port = port;
        syncServiceMap = new HashMap<>();
        asyncServiceMap = new HashMap<>();
        futureMap = new ConcurrentHashMap<>();
        executor = new ThreadPoolExecutor(4, 8,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        initNettyClient(idleTime);
    }

    // main
    private void initNettyClient(long idleTime) {
        rpcConnections = new RpcConnection[CONNECTIONS];
        for (int i = 0; i < CONNECTIONS; i++) {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap()
                        .group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline()
                                        .addLast(new IdleStateHandler(idleTime, idleTime, idleTime, TimeUnit.MILLISECONDS))
                                        .addLast(new RpcEncoder())
                                        .addLast(new RpcDecoder())
                                        .addLast(new RpcClientHandler());
                            }
                        });

                Channel channel = bootstrap.connect(hostname, port).sync().channel();
                rpcConnections[i] = new RpcConnection(workerGroup, channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // main
    public <T> T syncService(Class<T> service) {
        if (!syncServiceMap.containsKey(service)) {
            syncServiceMap.put(service, Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service}, new RpcService(this, service)));
        }

        return (T) syncServiceMap.get(service);
    }

    public RpcService asyncService(Class<?> service) {
        if (!asyncServiceMap.containsKey(service)) {
            asyncServiceMap.put(service, new RpcService(this, service));
        }

        return asyncServiceMap.get(service);
    }

    // main
    public void shutdown() {
        for (int i = 0; i < CONNECTIONS; i++) {
            rpcConnections[i].close();
        }
        executor.shutdown();
    }

    private boolean noAvailableConnection() {
        for (int i = 0; i < CONNECTIONS; i++) {
            connectionIdx = (++connectionIdx) % CONNECTIONS;
            if (!rpcConnections[connectionIdx].isClosed()) {
                return false;
            }
        }
        log.warn("all connections have been closed, RPC request can't be submitted anymore");
        return true;
    }

    RpcFuture submitRequest(final Class<?> targetService, final Method method, final Object[] args) {
        final RpcFuture rpcFuture = new RpcFuture();
        if (noAvailableConnection()) {
            rpcFuture.setResponse(FAIL_RESPONSE);
            return rpcFuture;
        }
        final int index = connectionIdx;

        // executor thread
        executor.submit(() -> {
            RpcRequest request = new RpcRequest()
                    .setChannelIndex(index)
                    .setClassName(targetService.getName())
                    .setMethodName(method.getName())
                    .setMethodParamsAndType(args);
            submitToChannel(rpcFuture, index, request);
        });
        return rpcFuture;
    }

    RpcFuture submitRequest(final Class<?> targetService, final String methodName, final Object[] args) {
        final RpcFuture rpcFuture = new RpcFuture();
        if (noAvailableConnection()) {
            rpcFuture.setResponse(FAIL_RESPONSE);
            return rpcFuture;
        }
        final int index = connectionIdx;
        // executor thread
        executor.submit(() -> {
            RpcRequest request = new RpcRequest()
                    .setChannelIndex(index)
                    .setClassName(targetService.getName())
                    .setMethodName(methodName)
                    .setMethodParamsAndType(args);
            submitToChannel(rpcFuture, index, request);
        });
        return rpcFuture;
    }

    private void submitToChannel(RpcFuture rpcFuture, int index, RpcRequest request) {
        UUID uuid = request.getUuid();
        rpcFuture.setRequest(request);
        futureMap.put(uuid, rpcFuture);

        try {
            rpcConnections[index].addRequestId(uuid);
            rpcConnections[index].getChannel().writeAndFlush(request).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
