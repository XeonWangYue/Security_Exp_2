package top.xeonwang;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class MessageDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int type = byteBuf.readInt();
        int len = byteBuf.readInt();
        System.out.println("decoder " + len + " type " + type);
        byte[] content = new byte[len];
        byteBuf.readBytes(content);
        MsgProtocol msg = new MsgProtocol();
        msg.setStep(type);
        msg.setLength(len);
        msg.setContent(content);
        list.add(msg);
    }
}
