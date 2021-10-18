package top.xeonwang;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author Chen Q.
 */
public class Exp2ServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("Decoder", new MessageDecoder());
        pipeline.addLast("Encoder", new MessageEncoder());
//        pipeline.addLast(new KDCServerHandler());
        pipeline.addLast(new CAServerHandler());
    }
}
