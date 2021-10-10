package top.xeonwang;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author Chen Q.
 */
public class Exp2Client {
    private int port;
    private String ip;

    public Exp2Client(String ip, int port) {
        this.port = port;
        this.ip = ip;
    }

    public ChannelFuture run() {
        EventLoopGroup eventGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_SNDBUF, 1024 * 20)
                .option(ChannelOption.SO_RCVBUF, 1024 * 20)
                .handler(new Exp2ClientInitializer());
        ChannelFuture cf = null;
        try {
            cf = bootstrap.connect(ip, port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (cf == null) ? null : cf;
    }
}
