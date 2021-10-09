package top.xeonwang;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author Chen Q.
 */
public class NettyClient {
    private int port;
    private String ip;

    public NettyClient(String ip, int port) {
        this.port = port;
        this.ip = ip;
    }

    public void run() {
        EventLoopGroup eventGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
        System.out.println("...client ok");
        ChannelFuture cf = null;
        try {
            cf = bootstrap.connect(ip, port).sync();
            Channel channel = cf.channel();

            MsgProtocol msgProtocol = new MsgProtocol();


            Scanner sc = new Scanner(System.in);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                ClientCommand.excute(line, channel);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        String ip = null;
        int port;
        if (args.length > 1) {
            ip = args[0];
        } else {
            ip = "127.0.0.1";
        }
        if (args.length > 2) {
            port = Integer.valueOf(args[1]);
        }
        else{
            port = 8099;
        }
        new NettyClient(ip, port).run();
    }
}
