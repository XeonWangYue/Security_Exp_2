package top.xeonwang;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class MessageDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int len = byteBuf.readInt();
        byte[] content = new byte[len];
        byteBuf.readBytes(content);
        MsgProtocol msg = new MsgProtocol();
        msg.setLength(len);
        msg.setContent(content);
        list.add(msg);
    }
}
