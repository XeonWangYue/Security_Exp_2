package top.xeonwang;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

public class CAClientHandler extends SimpleChannelInboundHandler<MsgProtocol> {
    private static KeyPair keyPair;
    private static Key capub = null;
    private static Key distpub = null;
    private static Key tsKey = null;
    public static byte[] ca = null;
    public static String filename = null;
    private static volatile byte[] filebuf = new byte[1024 * 1024 * 100];

    static {
        try {
            keyPair = RSAUtil.generateKeyPair();
        } catch (EncryptException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MsgProtocol msg) throws Exception {
        if (msg.getStep() == 0) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            int capubL = buf.readInt();
            byte[] caByte = new byte[capubL];
            buf.readBytes(caByte);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(caByte);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            capub = keyFactory.generatePublic(keySpec);

//            capub = (Key) KeyFactory.getInstance("RSA")
//                    .generatePublic(new X509EncodedKeySpec(caByte));
            for (int i = 0; i < 10; i++) {
                System.out.print(capub.getEncoded()[i] + " ");
            }
            System.out.println(" ");

            buf.clear();

            byte[] myPub = keyPair.getPublic().getEncoded();
            buf.writeInt(id1L)
                    .writeBytes(id1Byte)
                    .writeInt(myPub.length)
                    .writeBytes(myPub);
            byte[] content = new byte[buf.readableBytes()];
            MsgProtocol msg1 = new MsgProtocol();

            msg1.setStep(1);
            msg1.setLength(buf.readableBytes());
            buf.readBytes(content);
            msg1.setContent(content);

            ctx.channel().writeAndFlush(msg1);
        }
        if (msg.getStep() == 1) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            ca = msg.getContent();
            System.out.println("ca length: " + ca.length);
            System.out.println(buf.readLong());
        }
        if (msg.getStep() == 2) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            int cal = buf.readInt();
            byte[] cas = new byte[cal];
            buf.readBytes(cas);

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            byte[] secret1 = RSAUtil.decrypt(capub, cas);

            buf.clear().writeBytes(secret1);

            byte[] rubbish = new byte[1];
            buf.readBytes(rubbish);

            int id1Lca = buf.readInt();
            byte[] id1caByte = new byte[id1Lca];
            buf.readBytes(id1caByte);

            if (new String(id1Byte, StandardCharsets.UTF_8).compareTo(new String(id1caByte, StandardCharsets.UTF_8)) == 0) {
                System.out.println("用户验证成功！");
            }

            int id1pubL = buf.readInt();
            byte[] id1pubByte = new byte[id1pubL];
            buf.readBytes(id1pubByte);

            long tca = buf.readLong();
            if (tca < System.currentTimeMillis()) {
                System.out.println("时间戳验证成功！");
            }

            MsgProtocol msg1 = new MsgProtocol();
            msg1.setStep(3);

            buf.clear();

            buf.writeInt(id1L)
                    .writeBytes(id1Byte)
                    .writeInt(ca.length)
                    .writeBytes(ca)
                    .writeInt(id2L)
                    .writeBytes(id2Byte);

            byte[] content = new byte[buf.readableBytes()];

            msg1.setLength(buf.readableBytes());
            buf.readBytes(content);
            msg1.setContent(content);

            System.out.println("a->b success");
            ctx.channel().writeAndFlush(msg1);
        }
        if (msg.getStep() == 3) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            int cal = buf.readInt();
            byte[] cas = new byte[cal];
            buf.readBytes(cas);

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            byte[] secret1 = RSAUtil.decrypt(capub, cas);

            buf.clear().writeBytes(secret1);

            byte[] rubbish = new byte[1];
            buf.readBytes(rubbish);

            int id2Lca = buf.readInt();
            byte[] id2caByte = new byte[id2Lca];
            buf.readBytes(id2caByte);

            if (new String(id2Byte, StandardCharsets.UTF_8).compareTo(new String(id2caByte, StandardCharsets.UTF_8)) == 0) {
                System.out.println("用户验证成功！");
            }

            int id2pubL = buf.readInt();
            byte[] id2pubByte = new byte[id2pubL];
            buf.readBytes(id2pubByte);

            long tca = buf.readLong();
            if (tca < System.currentTimeMillis()) {
                System.out.println("时间戳验证成功！");
            }
            System.out.println("start send");

            buf.clear();


            FileInputStream stream = new FileInputStream(filename);
            byte[] file = stream.readAllBytes();
            System.out.println("origin " + file.length);
            long timestart = System.currentTimeMillis();
            System.out.println("encrpyt start: " + timestart);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(id2pubByte);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Key id2pub = keyFactory.generatePublic(keySpec);


            byte[] secret2 = RSAUtil.encrypt(id2pub, file);
//            byte[] secret2 = file;
            System.out.println("secret2 " + secret2.length);


            int j = 0;
            int size = 32 * 1024;
            int rest = 0;

            while ((rest = (secret2.length - j * size)) > 0) {
                MsgProtocol msg1 = new MsgProtocol();

                int len = size > rest ? rest : size;
                buf.clear();

                buf.writeInt(id2L)
                        .writeBytes(id2Byte)
                        .writeLong(timestart)
                        .writeInt(j)
                        .writeInt(len);
                byte[] temp = new byte[len];
                for (int i = 0; i < len; i++) {
                    temp[i] = secret2[j * size + i];
                }
                buf.writeBytes(temp);

                byte[] content = new byte[buf.readableBytes()];

                msg1.setStep(4);
                msg1.setLength(buf.readableBytes());
                buf.readBytes(content);
                msg1.setContent(content);

                System.out.println("length file: " + msg1.getLength());

                System.out.println(j);
                ctx.channel().writeAndFlush(msg1);
                j++;
            }
        }
        if (msg.getStep() == 4) {
            int size = 32 * 1024;
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            long timestart = buf.readLong();

            int j = buf.readInt();
            int filelength = buf.readInt();
            byte file[] = new byte[filelength];
            buf.readBytes(file);
            System.out.println("j: " + j);
            System.out.println("this piece length " + filelength);
            for (int i = 0; i < filelength; i++) {
                filebuf[i + size * j] = file[i];
            }

            if (filelength < size) {
                byte[] d = new byte[j * size + filelength];
                for (int i = 0; i < d.length; i++) {
                    d[i] = filebuf[i];
                }
                System.out.println("get length " + d.length);
                byte[] secret3 = RSAUtil.decrypt(keyPair.getPrivate(), d);

//                byte[] secret3 = d;
                System.out.println("secret length " + secret3.length);
                long timeend = System.currentTimeMillis();
                System.out.println("start - end: " + (timeend - timestart));
                FileOutputStream stream = new FileOutputStream("1.bmp");
                stream.write(secret3);
                stream.close();
            }
        }
        if (msg.getStep() == 10) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            int cal = buf.readInt();
            byte[] cas = new byte[cal];
            buf.readBytes(cas);

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            byte[] secret1 = RSAUtil.decrypt(capub, cas);

            buf.clear().writeBytes(secret1);

            byte[] rubbish = new byte[1];
            buf.readBytes(rubbish);

            int id1Lca = buf.readInt();
            byte[] id1caByte = new byte[id1Lca];
            buf.readBytes(id1caByte);

            if (new String(id1Byte, StandardCharsets.UTF_8).compareTo(new String(id1caByte, StandardCharsets.UTF_8)) == 0) {
                System.out.println("用户验证成功！");
            }

            int id1pubL = buf.readInt();
            byte[] id1pubByte = new byte[id1pubL];
            buf.readBytes(id1pubByte);

            long tca = buf.readLong();
            if (tca < System.currentTimeMillis()) {
                System.out.println("时间戳验证成功！");
            }


            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(id1pubByte);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            distpub = keyFactory.generatePublic(keySpec);


            MsgProtocol msg1 = new MsgProtocol();
            msg1.setStep(11);


            buf.clear().
                    writeInt(id1L)
                    .writeBytes(id1Byte)
                    .writeInt(ca.length)
                    .writeBytes(ca)
                    .writeInt(id2L)
                    .writeBytes(id2Byte);

            byte[] content = new byte[buf.readableBytes()];

            msg1.setLength(buf.readableBytes());
            buf.readBytes(content);
            msg1.setContent(content);
            ctx.channel().writeAndFlush(msg1);
        }
        if (msg.getStep() == 11) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            int cal = buf.readInt();
            byte[] cas = new byte[cal];
            buf.readBytes(cas);

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            byte[] secret1 = RSAUtil.decrypt(capub, cas);

            buf.clear().writeBytes(secret1);

            byte[] rubbish = new byte[1];
            buf.readBytes(rubbish);

            int id2Lca = buf.readInt();
            byte[] id2caByte = new byte[id2Lca];
            buf.readBytes(id2caByte);

            if (new String(id2Byte, StandardCharsets.UTF_8).compareTo(new String(id2caByte, StandardCharsets.UTF_8)) == 0) {
                System.out.println("用户验证成功！");
            }

            int id2pubL = buf.readInt();
            byte[] id2pubByte = new byte[id2pubL];
            buf.readBytes(id2pubByte);

            long tca = buf.readLong();
            if (tca < System.currentTimeMillis()) {
                System.out.println("时间戳验证成功！");
            }

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(id2pubByte);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            distpub = keyFactory.generatePublic(keySpec);

            Random random = new Random();
            int n1 = random.nextInt();
            System.out.println("n1 " + n1);
            buf.clear();
            buf.writeInt(n1)
                    .writeInt(id1L)
                    .writeBytes(id1Byte);
            byte[] secret0 = new byte[buf.readableBytes()];
            buf.readBytes(secret0);

            secret1 = RSAUtil.encrypt(distpub, secret0);


            buf.clear()
                    .writeInt(id2L)
                    .writeBytes(id2Byte)
                    .writeInt(secret1.length)
                    .writeBytes(secret1)
                    .writeInt(id1L)
                    .writeBytes(id1Byte);
            MsgProtocol msg1 = new MsgProtocol();

            msg1.setStep(12);
            byte[] content = new byte[buf.readableBytes()];
            msg1.setLength(buf.readableBytes());
            buf.readBytes(content);
            msg1.setContent(content);
            ctx.channel().writeAndFlush(msg1);
        }
        if (msg.getStep() == 12) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            int s1L = buf.readInt();
            byte[] secret1 = new byte[s1L];
            buf.readBytes(secret1);


            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            byte[] secret0 = RSAUtil.decrypt(keyPair.getPrivate(), secret1);
            buf.clear().writeBytes(secret0);
            int n1 = buf.readInt();
            System.out.println("n1 " + n1);
            Random random = new Random();
            int n2 = random.nextInt();
            System.out.println("n2 " + n2);
            buf.clear();

            buf.writeInt(n1);
            buf.writeInt(n2);

            byte[] secret2 = new byte[buf.readableBytes()];
            buf.readBytes(secret2);
            byte[] secret3 = RSAUtil.encrypt(distpub, secret2);

            buf.clear()
                    .writeInt(id1L)
                    .writeBytes(id1Byte)
                    .writeInt(secret3.length)
                    .writeBytes(secret3)
                    .writeInt(id2L)
                    .writeBytes(id2Byte);
            MsgProtocol msg1 = new MsgProtocol();

            msg1.setStep(13);
            byte[] content = new byte[buf.readableBytes()];
            msg1.setLength(buf.readableBytes());
            buf.readBytes(content);
            msg1.setContent(content);
            ctx.channel().writeAndFlush(msg1);
        }
        if (msg.getStep() == 13) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            int s1L = buf.readInt();
            byte[] secret3 = new byte[s1L];
            buf.readBytes(secret3);

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            byte[] secret2 = RSAUtil.decrypt(keyPair.getPrivate(), secret3);
            buf.clear().writeBytes(secret2);
            int n1 = buf.readInt();
            int n2 = buf.readInt();
            System.out.println("n1 " + n1 + " n2 " + n2);

            buf.clear().writeInt(n2);
            byte[] secret4 = new byte[buf.readableBytes()];
            buf.readBytes(secret4);

            byte[] secret5 = RSAUtil.encrypt(distpub, secret4);
            buf.clear()
                    .writeInt(id2L)
                    .writeBytes(id2Byte)
                    .writeInt(secret5.length)
                    .writeBytes(secret5)
                    .writeInt(id1L)
                    .writeBytes(id1Byte);

            MsgProtocol msg1 = new MsgProtocol();
            msg1.setStep(14);
            byte[] content = new byte[buf.readableBytes()];
            msg1.setLength(buf.readableBytes());
            buf.readBytes(content);
            msg1.setContent(content);
            ctx.channel().writeAndFlush(msg1).sync();

            tsKey = new SecretKeySpec(DESUtil.generate(), "DES");
            byte[] secret6 = RSAUtil.encrypt(distpub, RSAUtil.encrypt(keyPair.getPrivate(), tsKey.getEncoded()));
            for (int i = 0; i < 8; i++) {
                System.out.print(tsKey.getEncoded()[i] + " ");
            }
            System.out.println(" ");
            buf.clear()
                    .writeInt(id2L)
                    .writeBytes(id2Byte)
                    .writeInt(secret6.length)
                    .writeBytes(secret6)
                    .writeInt(id1L)
                    .writeBytes(id1Byte);
            MsgProtocol msg2 = new MsgProtocol();
            msg2.setStep(15);
            content = new byte[buf.readableBytes()];
            msg2.setLength(buf.readableBytes());
            buf.readBytes(content);
            msg2.setContent(content);
            ctx.channel().writeAndFlush(msg2);


            //--------------------------------
            System.out.println("start send");

            FileInputStream stream = new FileInputStream(filename);
            byte[] file = stream.readAllBytes();
            System.out.println("origin " + file.length);
            long timestart = System.currentTimeMillis();
            System.out.println("encrpyt start: " + timestart);

            byte[] files = DESUtil.encrypt(tsKey.getEncoded(), file);
            System.out.println("files " + files.length);

            int j = 0;
            int size = 32 * 1024;
            int rest = 0;

            while ((rest = (files.length - j * size)) > 0) {
                msg1 = new MsgProtocol();

                int len = size > rest ? rest : size;
                buf.clear();

                buf.writeInt(id2L)
                        .writeBytes(id2Byte)
                        .writeLong(timestart)
                        .writeInt(j)
                        .writeInt(len);
                byte[] temp = new byte[len];
                for (int i = 0; i < len; i++) {
                    temp[i] = files[j * size + i];
                }
                buf.writeBytes(temp);

                content = new byte[buf.readableBytes()];

                msg1.setStep(16);
                msg1.setLength(buf.readableBytes());
                buf.readBytes(content);
                msg1.setContent(content);

                System.out.println("length file: " + msg1.getLength());

                System.out.println(j);
                ctx.channel().writeAndFlush(msg1);
                j++;
            }
        }
        if (msg.getStep() == 14) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            int s1L = buf.readInt();
            byte[] secret5 = new byte[s1L];
            buf.readBytes(secret5);

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);


            byte[] secret4 = RSAUtil.decrypt(keyPair.getPrivate(), secret5);
            buf.clear().writeBytes(secret4);
            int n2 = buf.readInt();
            System.out.println(" n2 " + n2);
        }
        if (msg.getStep() == 15) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            int s6L = buf.readInt();
            byte[] secret6 = new byte[s6L];
            buf.readBytes(secret6);

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            byte[] ks = RSAUtil.decrypt(distpub, RSAUtil.decrypt(keyPair.getPrivate(), secret6));
            for (int i = 0; i < 8; i++) {
                System.out.print(ks[i] + " ");
            }
            System.out.println(" ");
            tsKey = new SecretKeySpec(ks, "DES");
        }
        if (msg.getStep() == 16) {
            int size = 32 * 1024;
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            long timestart = buf.readLong();

            int j = buf.readInt();
            int filelength = buf.readInt();
            byte file[] = new byte[filelength];
            buf.readBytes(file);
            System.out.println("j: " + j);
            System.out.println("this piece length " + filelength);
            for (int i = 0; i < filelength; i++) {
                filebuf[i + size * j] = file[i];
            }

            if (filelength < size) {
                byte[] d = new byte[j * size + filelength];
                for (int i = 0; i < d.length; i++) {
                    d[i] = filebuf[i];
                }
                System.out.println("get length " + d.length);
                byte[] secret3 = DESUtil.decrypt(tsKey.getEncoded(), d);

//                byte[] secret3 = d;
                System.out.println("secret length " + secret3.length);
                long timeend = System.currentTimeMillis();
                System.out.println("start - end: " + (timeend - timestart));
                FileOutputStream stream = new FileOutputStream("1.bmp");
                FileInputStream stream1 = new FileInputStream("F:\\CodeCache\\is\\test_pic.bmp");
                stream.write(secret3);
                stream.close();
            }
        }
        if (msg.getStep() == 21) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            int cal = buf.readInt();
            byte[] cas = new byte[cal];
            buf.readBytes(cas);

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            byte[] secret1 = RSAUtil.decrypt(capub, cas);

            buf.clear().writeBytes(secret1);

            byte[] rubbish = new byte[1];
            buf.readBytes(rubbish);

            int id1Lca = buf.readInt();
            byte[] id1caByte = new byte[id1Lca];
            buf.readBytes(id1caByte);

            if (new String(id1Byte, StandardCharsets.UTF_8).compareTo(new String(id1caByte, StandardCharsets.UTF_8)) == 0) {
                System.out.println("用户验证成功！");
            }

            int id1pubL = buf.readInt();
            byte[] id1pubByte = new byte[id1pubL];
            buf.readBytes(id1pubByte);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(id1pubByte);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Key id1pub = keyFactory.generatePublic(keySpec);

            distpub = id1pub;

            long tca = buf.readLong();
            if (tca < System.currentTimeMillis()) {
                System.out.println("时间戳验证成功！");
            }

            MsgProtocol msg1 = new MsgProtocol();
            msg1.setStep(22);

            buf.clear();

            buf.writeInt(id1L)
                    .writeBytes(id1Byte)
                    .writeInt(ca.length)
                    .writeBytes(ca)
                    .writeInt(id2L)
                    .writeBytes(id2Byte);

            byte[] content = new byte[buf.readableBytes()];

            msg1.setLength(buf.readableBytes());
            buf.readBytes(content);
            msg1.setContent(content);

            System.out.println("a->b success");
            ctx.channel().writeAndFlush(msg1);
        }
        if (msg.getStep() == 22) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            int cal = buf.readInt();
            byte[] cas = new byte[cal];
            buf.readBytes(cas);

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            byte[] secret1 = RSAUtil.decrypt(capub, cas);

            buf.clear().writeBytes(secret1);

            byte[] rubbish = new byte[1];
            buf.readBytes(rubbish);

            int id2Lca = buf.readInt();
            byte[] id2caByte = new byte[id2Lca];
            buf.readBytes(id2caByte);

            if (new String(id2Byte, StandardCharsets.UTF_8).compareTo(new String(id2caByte, StandardCharsets.UTF_8)) == 0) {
                System.out.println("用户验证成功！");
            }

            int id2pubL = buf.readInt();
            byte[] id2pubByte = new byte[id2pubL];
            buf.readBytes(id2pubByte);

            long tca = buf.readLong();
            if (tca < System.currentTimeMillis()) {
                System.out.println("时间戳验证成功！");
            }

            System.out.println("start send");

            buf.clear();


            FileInputStream stream = new FileInputStream(filename);
            byte[] file = stream.readAllBytes();

            byte[] md5 = MD5Util.MD5(file);
            byte[] md5s = RSAUtil.encrypt(keyPair.getPrivate(), md5);

            ByteBuf tb = Unpooled.buffer();

            tb.writeInt(md5s.length)
                    .writeBytes(md5s)
                    .writeBytes(file);

            byte[] trans = new byte[tb.readableBytes()];
            tb.readBytes(trans);

            System.out.println("origin " + trans.length);

            long timestart = System.currentTimeMillis();
            System.out.println("encrpyt start: " + timestart);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(id2pubByte);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Key id2pub = keyFactory.generatePublic(keySpec);
            distpub = id2pub;

            byte[] secret2 = RSAUtil.encrypt(id2pub, trans);
//            byte[] secret2 = file;
            System.out.println("secret2 " + secret2.length);


            int j = 0;
            int size = 32 * 1024;
            int rest = 0;

            while ((rest = (secret2.length - j * size)) > 0) {
                MsgProtocol msg1 = new MsgProtocol();

                int len = size > rest ? rest : size;
                buf.clear();

                buf.writeInt(id2L)
                        .writeBytes(id2Byte)
                        .writeLong(timestart)
                        .writeInt(j)
                        .writeInt(len);
                byte[] temp = new byte[len];
                for (int i = 0; i < len; i++) {
                    temp[i] = secret2[j * size + i];
                }
                buf.writeBytes(temp);

                byte[] content = new byte[buf.readableBytes()];

                msg1.setStep(23);
                msg1.setLength(buf.readableBytes());
                buf.readBytes(content);
                msg1.setContent(content);

                System.out.println("length file: " + msg1.getLength());

                System.out.println(j);
                ctx.channel().writeAndFlush(msg1);
                j++;
            }
        }
        if (msg.getStep() == 23) {
            int size = 32 * 1024;
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            long timestart = buf.readLong();

            int j = buf.readInt();
            int filelength = buf.readInt();
            byte file[] = new byte[filelength];
            buf.readBytes(file);
            System.out.println("j: " + j);
            System.out.println("this piece length " + filelength);
            for (int i = 0; i < filelength; i++) {
                filebuf[i + size * j] = file[i];
            }

            if (filelength < size) {
                byte[] d = new byte[j * size + filelength];
                for (int i = 0; i < d.length; i++) {
                    d[i] = filebuf[i];
                }
                System.out.println("get length " + d.length);
                byte[] secret3 = RSAUtil.decrypt(keyPair.getPrivate(), d);

//                byte[] secret3 = d;
                System.out.println("secret length " + secret3.length);
                long timeend = System.currentTimeMillis();
                System.out.println("start - end: " + (timeend - timestart));

                ByteBuf tb = Unpooled.buffer();

                tb.writeBytes(secret3);
                int md5L = tb.readInt();
                byte[] md5 = new byte[md5L];

                tb.readBytes(md5);
                byte[] md5d = RSAUtil.decrypt(distpub, md5);

                int fileL = tb.readableBytes();
                byte[] fileB = new byte[fileL];
                tb.readBytes(fileB);
                byte[] md5s = MD5Util.MD5(fileB);
                boolean flag = true;
                System.out.println("MD5 confirming");
                for (int i = 0; i < md5s.length; i++) {
                    if (md5s[i] != md5d[i]) {
                        System.out.print(md5s[i] + " ");
                        flag = false;
                    }
                }
                if (flag) {
                    System.out.println("md5 check!");
                }

                FileOutputStream stream = new FileOutputStream("1.bmp");
                stream.write(fileB);
                stream.close();
            }
        }

        if (msg.getStep() == 31) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            int cal = buf.readInt();
            byte[] cas = new byte[cal];
            buf.readBytes(cas);

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            byte[] secret1 = RSAUtil.decrypt(capub, cas);

            buf.clear().writeBytes(secret1);

            byte[] rubbish = new byte[1];
            buf.readBytes(rubbish);

            int id1Lca = buf.readInt();
            byte[] id1caByte = new byte[id1Lca];
            buf.readBytes(id1caByte);

            if (new String(id1Byte, StandardCharsets.UTF_8).compareTo(new String(id1caByte, StandardCharsets.UTF_8)) == 0) {
                System.out.println("用户验证成功！");
            }

            int id1pubL = buf.readInt();
            byte[] id1pubByte = new byte[id1pubL];
            buf.readBytes(id1pubByte);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(id1pubByte);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Key id1pub = keyFactory.generatePublic(keySpec);

            distpub = id1pub;

            long tca = buf.readLong();
            if (tca < System.currentTimeMillis()) {
                System.out.println("时间戳验证成功！");
            }

            MsgProtocol msg1 = new MsgProtocol();
            msg1.setStep(22);

            buf.clear();

            buf.writeInt(id1L)
                    .writeBytes(id1Byte)
                    .writeInt(ca.length)
                    .writeBytes(ca)
                    .writeInt(id2L)
                    .writeBytes(id2Byte);

            byte[] content = new byte[buf.readableBytes()];

            msg1.setLength(buf.readableBytes());
            buf.readBytes(content);
            msg1.setContent(content);

            System.out.println("a->b success");
            ctx.channel().writeAndFlush(msg1);
        }
        if (msg.getStep() == 32) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id1L = buf.readInt();
            byte[] id1Byte = new byte[id1L];
            buf.readBytes(id1Byte);

            int cal = buf.readInt();
            byte[] cas = new byte[cal];
            buf.readBytes(cas);

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            byte[] secret1 = RSAUtil.decrypt(capub, cas);

            buf.clear().writeBytes(secret1);

            byte[] rubbish = new byte[1];
            buf.readBytes(rubbish);

            int id2Lca = buf.readInt();
            byte[] id2caByte = new byte[id2Lca];
            buf.readBytes(id2caByte);

            if (new String(id2Byte, StandardCharsets.UTF_8).compareTo(new String(id2caByte, StandardCharsets.UTF_8)) == 0) {
                System.out.println("用户验证成功！");
            }

            int id2pubL = buf.readInt();
            byte[] id2pubByte = new byte[id2pubL];
            buf.readBytes(id2pubByte);

            long tca = buf.readLong();
            if (tca < System.currentTimeMillis()) {
                System.out.println("时间戳验证成功！");
            }

            System.out.println("start send");

            buf.clear();


            FileInputStream stream = new FileInputStream(filename);
            byte[] file = stream.readAllBytes();

//            byte[] md5 = MD5Util.MD5(file);
//            byte[] md5s = RSAUtil.encrypt(keyPair.getPrivate(), md5);
//
//            ByteBuf tb = Unpooled.buffer();
//
//            tb.writeInt(md5s.length)
//                    .writeBytes(md5s)
//                    .writeBytes(file);

//            byte[] trans = new byte[tb.readableBytes()];
//            tb.readBytes(trans);

            System.out.println("origin " + file.length);

            long timestart = System.currentTimeMillis();
            System.out.println("encrpyt start: " + timestart);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(id2pubByte);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Key id2pub = keyFactory.generatePublic(keySpec);
            distpub = id2pub;

            byte[] secret2 = RSAUtil.encrypt(id2pub, file);
//            byte[] secret2 = file;
            System.out.println("secret2 " + secret2.length);
            ByteBuf tb = Unpooled.buffer();

            byte[] md5 = MD5Util.MD5(secret2);
            tb.writeInt(md5.length)
                    .writeBytes(md5)
                    .writeBytes(secret2);

            byte[] trans = new byte[tb.readableBytes()];
            tb.readBytes(trans);

            secret2 = trans;

            int j = 0;
            int size = 32 * 1024;
            int rest = 0;

            while ((rest = (secret2.length - j * size)) > 0) {
                MsgProtocol msg1 = new MsgProtocol();

                int len = size > rest ? rest : size;
                buf.clear();

                buf.writeInt(id2L)
                        .writeBytes(id2Byte)
                        .writeLong(timestart)
                        .writeInt(j)
                        .writeInt(len);
                byte[] temp = new byte[len];
                for (int i = 0; i < len; i++) {
                    temp[i] = secret2[j * size + i];
                }
                buf.writeBytes(temp);

                byte[] content = new byte[buf.readableBytes()];

                msg1.setStep(23);
                msg1.setLength(buf.readableBytes());
                buf.readBytes(content);
                msg1.setContent(content);

                System.out.println("length file: " + msg1.getLength());

                System.out.println(j);
                ctx.channel().writeAndFlush(msg1);
                j++;
            }
        }
        if (msg.getStep() == 33) {
            int size = 32 * 1024;
            ByteBuf buf = Unpooled.buffer();
            buf.writeBytes(msg.getContent());

            int id2L = buf.readInt();
            byte id2Byte[] = new byte[id2L];
            buf.readBytes(id2Byte);

            long timestart = buf.readLong();

            int j = buf.readInt();
            int filelength = buf.readInt();
            byte file[] = new byte[filelength];
            buf.readBytes(file);
            System.out.println("j: " + j);
            System.out.println("this piece length " + filelength);
            for (int i = 0; i < filelength; i++) {
                filebuf[i + size * j] = file[i];
            }

            if (filelength < size) {
                byte[] d = new byte[j * size + filelength];
                for (int i = 0; i < d.length; i++) {
                    d[i] = filebuf[i];
                }

                ByteBuf tb = Unpooled.buffer();
                tb.writeBytes(d);

                int lenM = tb.readInt();
                byte[] md5d = new byte[lenM];
                tb.readBytes(md5d);

                System.out.println("get length " + d.length);

                byte[] content= new byte[tb.readableBytes()];
                tb.writeBytes(content);

                byte[] md5s = MD5Util.MD5(content);
                byte[] secret3 = RSAUtil.decrypt(keyPair.getPrivate(), content);

//                byte[] secret3 = d;
                System.out.println("secret length " + secret3.length);
                long timeend = System.currentTimeMillis();
                System.out.println("start - end: " + (timeend - timestart));


                boolean flag = true;
                System.out.println("MD5 confirming");
                for (int i = 0; i < md5s.length; i++) {
                    if (md5s[i] != md5d[i]) {
                        System.out.print(md5s[i] + " ");
                        flag = false;
                    }
                }
                if (flag) {
                    System.out.println("md5 check!");
                }

                FileOutputStream stream = new FileOutputStream("1.bmp");
                stream.write(secret3);
                stream.close();
            }
        }
    }
}
