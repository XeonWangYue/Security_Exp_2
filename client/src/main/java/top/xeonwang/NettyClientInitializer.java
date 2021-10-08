package top.xeonwang;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author Chen Q.
 */
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("Decoder",new MessageDecoder());
        pipeline.addLast("Encoder", new MessageEncoder());
        pipeline.addLast(new NettyClientHandler());
    }
}
