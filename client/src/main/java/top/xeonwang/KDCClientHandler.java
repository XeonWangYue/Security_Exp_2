package top.xeonwang;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import io.netty.channel.SimpleChannelInboundHandler;

import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Random;


/**
 * @author Chen Q.
 */
public class KDCClientHandler extends SimpleChannelInboundHandler<MsgProtocol> {

    public static String filename = null;
    private static Key mKey = new SecretKeySpec(DESUtil.getkey(), "DES");
    private static Key myKey = null;
    private static Key tsKey = null;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MsgProtocol msg) throws Exception {
        System.out.println("get client, step= " + msg.getStep());
        if (msg.getStep() == 0) {
            ByteBuf buf = Unpooled.buffer();

            buf.writeBytes(msg.getContent());
            int keyLen = buf.readInt();
            byte[] keyByte = new byte[keyLen];
            buf.readBytes(keyByte);
            myKey = new SecretKeySpec(keyByte, "DES");
//            int listlen = buf.readInt();
//            byte[] list = new byte[listlen];
//            buf.readBytes(list);
//            idSaver.clear();
//            idSaver.readin(list);
            System.out.println("step 0 client over");
//
            for (int i = 0; i < keyByte.length; i++) {
                System.out.print(myKey.getEncoded()[i] + " ");
            }
            System.out.println("");
        }
//        if(msg.getStep()==10){
//            ByteBuf buf = Unpooled.buffer();
//            buf.writeBytes(msg.getContent());
//
//            int listlen = buf.readInt();
//            byte[] list = new byte[listlen];
//            buf.readBytes(list);
//            idSaver.clear();
//            idSaver.readin(list);
//            System.out.println("step 10 client over");
//        }
        if (msg.getStep() == 2) {
            System.out.println(filename);
            ByteBuf buf = Unpooled.buffer();
            byte[] t1 = DESUtil.decrypt(myKey.getEncoded(), msg.getContent());
            buf.writeBytes(t1);
            byte[] ks = new byte[myKey.getEncoded().length];
            buf.readBytes(ks);

            tsKey = new SecretKeySpec(ks, "DES");

            int id1L, id2L;

            id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);
            String id1 = new String(id1Byte, StandardCharsets.UTF_8);

            id2L = buf.readInt();
            byte[] id2Byte = new byte[id2L];
            buf.readBytes(id2Byte);
            String id2 = new String(id2Byte, StandardCharsets.UTF_8);

            int n1 = buf.readInt();
            int lenst = buf.readInt();
            byte[] secret0 = new byte[lenst];

            buf.readBytes(secret0);

            buf.clear();
            buf.writeInt(id2L);
            buf.writeBytes(id2Byte);

            buf.writeInt(secret0.length);
            buf.writeBytes(secret0);

            buf.writeInt(id1L);
            buf.writeBytes(id1Byte);

            MsgProtocol msg1 = new MsgProtocol();
            msg1.setStep(3);

            msg1.setLength(buf.readableBytes());
            byte[] st = new byte[buf.readableBytes()];
            buf.readBytes(st);

            msg1.setContent(st);
            ctx.channel().writeAndFlush(msg1);
        }
        if (msg.getStep() == 3) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            int l = buf.readInt();
            byte[] secret0 = new byte[l];
            buf.readBytes(secret0);

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            byte[] secret1 = DESUtil.decrypt(myKey.getEncoded(), secret0);
            buf.clear();
            buf.writeBytes(secret1);
            byte[] ks = new byte[myKey.getEncoded().length];
            buf.readBytes(ks);


            tsKey = new SecretKeySpec(ks, "DES");

            System.out.println("send ts");
            for (int i = 0; i < tsKey.getEncoded().length; i++) {
                System.out.print(tsKey.getEncoded()[i] + " ");
            }
            System.out.println(" ");

            Random random = new Random();
            int n2 = random.nextInt();

            System.out.println("send n2 " + n2);
            buf.clear();

            buf.writeInt(n2);

            byte[] n2s = new byte[buf.readableBytes()];
            buf.readBytes(n2s);
            byte[] secret3 = DESUtil.encrypt(tsKey.getEncoded(), n2s);

            buf.clear();

            buf.writeInt(id1L);
            buf.writeBytes(id1Byte);
            buf.writeInt(secret3.length);
            buf.writeBytes(secret3);
            buf.writeInt(id2L);
            buf.writeBytes(id2Byte);

            MsgProtocol msg1 = new MsgProtocol();
            msg1.setStep(4);
            byte[] encode = new byte[buf.readableBytes()];
            msg1.setLength(buf.readableBytes());

            buf.readBytes(encode);
            msg1.setContent(encode);

            ctx.channel().writeAndFlush(msg1);
        }
        if (msg.getStep() == 4) {
            System.out.println(filename);
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            int ns2L = buf.readInt();
            byte[] ns2Byte = new byte[ns2L];
            buf.readBytes(ns2Byte);

            int id2L = buf.readInt();
            byte[] id2Byte = new byte[id2L];
            buf.readBytes(id2Byte);

            buf.clear();
            System.out.println("recv ts");
            for (int i = 0; i < tsKey.getEncoded().length; i++) {
                System.out.print(tsKey.getEncoded()[i] + " ");
            }
            System.out.println(" ");
            byte[] ns2 = DESUtil.decrypt(tsKey.getEncoded(), ns2Byte);
            buf.writeBytes(ns2);
            int n2 = buf.readInt();
            System.out.println("recv n2 " + n2);

            buf.clear();
            int fn2 = FunctionUtil.f0(n2);
            buf.writeInt(fn2);
            byte[] fn2s = new byte[buf.readableBytes()];
            buf.readBytes(fn2s);

            byte[] secret5 = DESUtil.encrypt(tsKey.getEncoded(), fn2s);

            buf.clear();

            buf.writeInt(id2L);
            buf.writeBytes(id2Byte);
            buf.writeInt(secret5.length);
            buf.writeBytes(secret5);
            buf.writeInt(id1L);
            buf.writeBytes(id1Byte);

            byte[] content = new byte[buf.readableBytes()];
            buf.readBytes(content);

            MsgProtocol msg1 = new MsgProtocol();
            msg1.setStep(5);
            msg1.setLength(content.length);
            msg1.setContent(content);

            System.out.println(fn2);

            ctx.channel().writeAndFlush(msg1).sync();

            FileInputStream stream = new FileInputStream(filename);

            byte[] fileByte = stream.readAllBytes();
            byte[] filesecret = DESUtil.encrypt(tsKey.getEncoded(), fileByte);

            MsgProtocol real = new MsgProtocol();
            real.setStep(6);
            buf.clear();

            buf.writeInt(id2L);
            buf.writeBytes(id2Byte);

            buf.writeInt(filesecret.length);
            buf.writeBytes(filesecret);

            buf.writeInt(id1L);
            buf.writeBytes(id1Byte);
            byte[] secret6 = new byte[buf.readableBytes()];
            real.setLength(buf.readableBytes());
            buf.readBytes(secret6);

            real.setContent(secret6);
            ctx.channel().writeAndFlush(real);
        }
        if (msg.getStep() == 5) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte[] id2Byte = new byte[id2L];
            buf.readBytes(id2Byte);

            int fns2L = buf.readInt();
            byte[] fns2Byte = new byte[fns2L];
            buf.readBytes(fns2Byte);

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            byte[] fn2s = DESUtil.decrypt(tsKey.getEncoded(), fns2Byte);
            buf.clear();
            buf.writeBytes(fn2s);
            int fn2 = buf.readInt();
            System.out.println(fn2);
        }
        if (msg.getStep() == 6 || msg.getStep() == 14) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte[] id2Byte = new byte[id2L];
            buf.readBytes(id2Byte);

            int fileL = buf.readInt();
            byte[] secret6 = new byte[fileL];
            buf.readBytes(secret6);

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            byte[] fileByte = DESUtil.decrypt(tsKey.getEncoded(), secret6);
            FileOutputStream stream = new FileOutputStream("1.txt");
            stream.write(fileByte);
            stream.close();
        }
        if (msg.getStep() == 11) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte[] id2Byte = new byte[id2L];
            buf.readBytes(id2Byte);

            int n1 = buf.readInt();

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            System.out.println(n1);

            Random random = new Random();
            int n2 = random.nextInt();
            int fn1 = FunctionUtil.f0(n1);
            byte[] ks = DESUtil.generate();
            tsKey = new SecretKeySpec(ks, "DES");
            System.out.println("send n2: " + n2);
            buf.clear();
            buf.writeBytes(ks)
                    .writeInt(id1L)
                    .writeBytes(id1Byte)
                    .writeInt(id2L)
                    .writeBytes(id2Byte)
                    .writeInt(fn1)
                    .writeInt(n2);
            byte[] secret0 = new byte[buf.readableBytes()];
            buf.readBytes(secret0);
            byte[] secret1 = DESUtil.encrypt(mKey.getEncoded(), secret0);

            buf.clear();
            buf.writeInt(id1L)
                    .writeBytes(id1Byte)
                    .writeInt(secret1.length)
                    .writeBytes(secret1)
                    .writeInt(id2L)
                    .writeBytes(id2Byte);
            MsgProtocol msg1 = new MsgProtocol();
            msg1.setStep(12);
            msg1.setLength(buf.readableBytes());
            byte[] content = new byte[buf.readableBytes()];
            buf.readBytes(content);

            msg1.setContent(content);
            ctx.channel().writeAndFlush(msg1);
        }
        if (msg.getStep() == 12) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            int s1L = buf.readInt();
            byte[] secret1 = new byte[s1L];
            buf.readBytes(secret1);

            int id2L = buf.readInt();
            byte[] id2Byte = new byte[id2L];
            buf.readBytes(id2Byte);

            byte[] secret0 = DESUtil.decrypt(DESUtil.getkey(), secret1);
            buf.clear().writeBytes(secret0);

            byte[] ks = new byte[mKey.getEncoded().length];

            buf.readBytes(ks);

            tsKey = new SecretKeySpec(ks, "DES");

            id1L = buf.readInt();
            buf.readBytes(id1Byte);

            id2L = buf.readInt();
            buf.readBytes(id2Byte);

            int fn1 = buf.readInt();
            int n2 = buf.readInt();

            System.out.println("recv n2: " + n2);

            int fn2 = FunctionUtil.f0(n2);

            System.out.println("send fn2: " + fn2);

            buf.clear();

            buf.writeInt(fn2);

            byte[] fn2Byte = new byte[buf.readableBytes()];
            buf.readBytes(fn2Byte);

            byte[] secret2 = DESUtil.encrypt(tsKey.getEncoded(), fn2Byte);

            buf.clear();
            buf.writeInt(id2L)
                    .writeBytes(id2Byte)
                    .writeInt(secret2.length)
                    .writeBytes(secret2)
                    .writeInt(id1L)
                    .writeBytes(id1Byte);

            byte[] content = new byte[buf.readableBytes()];
            MsgProtocol msg1 = new MsgProtocol();
            msg1.setStep(13);
            msg1.setLength(buf.readableBytes());
            buf.readBytes(content);
            msg1.setContent(content);

            ctx.channel().writeAndFlush(msg1).sync();

            System.out.println("send file");

            FileInputStream stream = new FileInputStream(filename);

            byte[] fileByte = stream.readAllBytes();
            byte[] filesecret = DESUtil.encrypt(tsKey.getEncoded(), fileByte);

            MsgProtocol real = new MsgProtocol();
            real.setStep(14);
            buf.clear();

            buf.writeInt(id2L);
            buf.writeBytes(id2Byte);

            buf.writeInt(filesecret.length);
            buf.writeBytes(filesecret);

            buf.writeInt(id1L);
            buf.writeBytes(id1Byte);

            byte[] secret6 = new byte[buf.readableBytes()];
            real.setLength(buf.readableBytes());
            buf.readBytes(secret6);

            real.setContent(secret6);
            System.out.println("send start");
            ctx.channel().writeAndFlush(real);
        }
        if (msg.getStep() == 13) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            int s2L = buf.readInt();
            byte[] secret2 = new byte[s2L];
            buf.readBytes(secret2);

            int id2L = buf.readInt();
            byte[] id2Byte = new byte[id2L];
            buf.readBytes(id2Byte);

            byte[] fn2Byte = DESUtil.decrypt(tsKey.getEncoded(), secret2);
            int fn2 = buf.clear().writeBytes(fn2Byte).readInt();
            System.out.println("recv fn2: " + fn2);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
