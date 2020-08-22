package upenn.viktorxh.rpc.commons;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import java.util.*;

/**
 * @author Hezijian Xiao
 * @date 2020/8/22 14:38
 */
public class RpcConnection {
    private volatile boolean closed;
    private volatile EventLoopGroup eventLoopGroup;
    private volatile Channel channel;
    private volatile Set<UUID> requestIdSet;

    public RpcConnection(EventLoopGroup eventLoopGroup, Channel channel) {
        this.eventLoopGroup = eventLoopGroup;
        this.channel = channel;
        this.requestIdSet = new HashSet<>();
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public boolean isClosed() {
        return closed;
    }

    public Channel getChannel() {
        return channel;
    }

    public Set<UUID> getRequestIdSet() {
        return requestIdSet;
    }

    public void addRequestId(UUID uuid) {
        requestIdSet.add(uuid);
    }

    public void removeRequestId(UUID uuid) {
        requestIdSet.remove(uuid);
    }

    public void close() {
        try {
            channel.close().sync();
            eventLoopGroup.shutdownGracefully();
            closed = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
