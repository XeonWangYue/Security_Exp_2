package top.xeonwang;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author Chen Q.
 */
public class Exp2Server {
    private int port;

    public Exp2Server(int port) {
        this.port = port;
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_SNDBUF, 1024 * 20)
                .option(ChannelOption.SO_RCVBUF, 1024 * 20)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new Exp2ServerInitializer());
        System.out.println("netty 服务启动");
        ChannelFuture cf = null;
        try {
            cf = bootstrap.bind(port).sync();
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) {
        new Exp2Server(8099).run();
    }
}
