package top.xeonwang;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class CAServerHandler extends SimpleChannelInboundHandler<MsgProtocol> {
    private static KeyPair keyPair;
    private static UserKeySaver keySaver = UserKeySaver.getInstance();

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static ConcurrentHashMap<String, Channel> map = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, byte[]> cas = new ConcurrentHashMap<>();

    static {
        try {
            keyPair = RSAUtil.generateKeyPair();
        } catch (EncryptException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.add(channel);
        System.out.println("bind");
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

                buf.clear();
                MsgProtocol msg1 = new MsgProtocol();

                int pbcaL = keyPair.getPublic().getEncoded().length;
                byte[] pbcaByte = keyPair.getPublic().getEncoded();

                for (int i = 0; i < 10; i++) {
                    System.out.print(keyPair.getPublic().getEncoded()[i] + " ");
                }
                System.out.println(" ");

                buf.writeInt(length)
                        .writeBytes(temp)
                        .writeInt(pbcaL)
                        .writeBytes(pbcaByte);
                byte[] content = new byte[buf.readableBytes()];
                msg1.setStep(0);
                msg1.setLength(buf.readableBytes());
                buf.readBytes(content);
                msg1.setContent(content);
                channel.writeAndFlush(msg1);
                break;
            }
            case 1: {
                ByteBuf buf = Unpooled.buffer();
                buf.writeBytes(msg.getContent());

                int id1L = buf.readInt();
                byte[] id1Byte = new byte[id1L];
                buf.readBytes(id1Byte);
                String id1 = new String(id1Byte, StandardCharsets.UTF_8);

                int pub1L = buf.readInt();
                byte[] pub1Byte = new byte[pub1L];
                buf.readBytes(pub1Byte);

                keySaver.addUserKey(id1, pub1Byte);
                System.out.println("format " + keyPair.getPublic().getFormat());

                long t = System.currentTimeMillis();

                buf.clear();
                buf.writeByte((byte) 1)
                        .writeInt(id1L)
                        .writeBytes(id1Byte)
                        .writeInt(pub1L)
                        .writeBytes(pub1Byte)
                        .writeLong(t);
                byte[] secret1 = new byte[buf.readableBytes()];
                buf.readBytes(secret1);
                System.out.println("secret 1 length: " + secret1.length);
                byte[] secret0 = RSAUtil.encrypt(keyPair.getPrivate(), secret1);
                byte[] secretx = RSAUtil.decrypt(keyPair.getPublic(), secret0);


                cas.put(id1, secret0);

                buf.clear();
                MsgProtocol msg1 = new MsgProtocol();
                msg1.setStep(1);
                msg1.setLength(secret0.length);
                msg1.setContent(secret0);

                channel.writeAndFlush(msg1);
                break;
            }
            case 2:
            case 3:
            case 4:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            {
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
                break;
        }
    }
}
