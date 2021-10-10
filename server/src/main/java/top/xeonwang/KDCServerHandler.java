package top.xeonwang;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Chen Q.
 */
public class KDCServerHandler extends SimpleChannelInboundHandler<MsgProtocol> {

    /**
     * channel group manage all channels
     */
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static ConcurrentHashMap<String, Channel> map = new ConcurrentHashMap<>();
    private static UserKeySaver keySaver = UserKeySaver.getInstance();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.add(channel);
        System.out.println("bind");
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");

    /**
     * whenever a connection is created
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.add(channel);
        System.out.println("client up");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MsgProtocol msg) throws Exception {
        Channel channel = channelHandlerContext.channel();
        System.out.println("get server");
        switch (msg.getStep()) {
            case 0: {
                ByteBuf buf = Unpooled.buffer();
                buf.writeBytes(msg.getContent());

                int length = buf.readInt();

                byte[] temp = new byte[length];
                buf.readBytes(temp);
                String id = new String(temp, StandardCharsets.UTF_8);

                map.put(id, channel);

                byte[] key = DESUtil.generate();
                keySaver.addUserKey(id, key);

                for (int i = 0; i < key.length; i++) {
                    System.out.print(key[i] + " ");
                }
                System.out.println(" ");

                buf.clear();
                MsgProtocol res = new MsgProtocol();
                msg.setStep(0);

                buf.writeInt(key.length);
                buf.writeBytes(key);

                msg.setLength(buf.readableBytes());

                byte[] content = new byte[buf.readableBytes()];
                buf.readBytes(content);
                msg.setContent(content);

                System.out.println("step 0 server over");
                channel.writeAndFlush(msg);
                break;
            }
            case 1: {
                ByteBuf buf = Unpooled.buffer();
                buf.writeBytes(msg.getContent());
                int id1L = buf.readInt();
                byte[] id1Byte = new byte[id1L];
                buf.readBytes(id1Byte);
                String id1 = new String(id1Byte, StandardCharsets.UTF_8);

                int id2L = buf.readInt();
                byte[] id2Byte = new byte[id2L];
                buf.readBytes(id2Byte);
                String id2 = new String(id2Byte, StandardCharsets.UTF_8);

                int n1 = buf.readInt();

                byte[] kb = keySaver.getUserKey(id2);
                byte[] ka = keySaver.getUserKey(id1);

                if (ka != null && kb != null) {
                    byte[] ks = DESUtil.generate();
                    ByteBuf t = Unpooled.buffer();
                    t.writeBytes(ks);
                    t.writeInt(id1Byte.length);
                    t.writeBytes(id1Byte);
                    byte[] st = new byte[t.readableBytes()];
                    t.readBytes(st);
                    byte[] secret1 = DESUtil.encrypt(kb, st);

                    t.clear();
                    t.writeBytes(ks);
                    t.writeInt(id1Byte.length);
                    t.writeBytes(id1Byte);
                    t.writeInt(id2Byte.length);
                    t.writeBytes(id2Byte);
                    t.writeInt(n1);
                    t.writeInt(secret1.length);
                    t.writeBytes(secret1);
                    byte[] st2 = new byte[t.readableBytes()];
                    t.readBytes(st2);

                    byte[] secret0 = DESUtil.encrypt(ka, st2);

                    MsgProtocol msg1 = new MsgProtocol();
                    msg1.setStep(2);
                    msg1.setLength(secret0.length);
                    msg1.setContent(secret0);
                    Channel tc = map.get(id1);
                    tc.writeAndFlush(msg1);
                }


                break;
            }
            case 3:
            case 4:
            case 5:
            case 6:
            case 11:
            case 12:
            case 13:
            case 14: {
                ByteBuf buf = Unpooled.buffer();
                buf.writeBytes(msg.getContent());

                int idtL = buf.readInt();
                byte[] idtByte = new byte[idtL];
                buf.readBytes(idtByte);
                String idt = new String(idtByte, StandardCharsets.UTF_8);
                Channel d = map.get(idt);

                d.writeAndFlush(msg);
                break;
            }

            default:
                System.out.println("error");
                break;
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println("client down");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
