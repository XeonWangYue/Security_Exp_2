package top.xeonwang;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author Chen Q.
 */
public class Exp2ClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("Decoder", new MessageDecoder());
        pipeline.addLast("Encoder", new MessageEncoder());
        pipeline.addLast("Executor", new CAClientHandler());
//        pipeline.addLast("Executor", new KDCClientHandler());

    }
}
