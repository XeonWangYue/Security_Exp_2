package top.xeonwang;

import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.SimpleChannelInboundHandler;


/**
 * @author Chen Q.
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<MsgProtocol> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MsgProtocol s) throws Exception {

    }
}
