package top.xeonwang;

import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.StandardCharsets;


/**
 * @author Chen Q.
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<MsgProtocol> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MsgProtocol msg) throws Exception {
        String s = new String(msg.getContent(), StandardCharsets.UTF_8);
        System.out.println(s);
    }
}
