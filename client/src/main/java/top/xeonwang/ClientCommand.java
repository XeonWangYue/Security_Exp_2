package top.xeonwang;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Scanner;


public class ClientCommand {
    private static String id;
//    private static Long uid;

    public static void register(Channel channel) {
//        Random random = new Random();
//        uid = random.nextLong();

        ByteBuf buf = Unpooled.buffer();
//        buf.writeLong(uid);
        buf.writeInt(id.getBytes(StandardCharsets.UTF_8).length);
        buf.writeBytes(id.getBytes(StandardCharsets.UTF_8));

        MsgProtocol msg = new MsgProtocol();
        msg.setStep(0);
        msg.setLength(buf.readableBytes());
        byte[] content = new byte[buf.readableBytes()];
        buf.readBytes(content);
        msg.setContent(content);
        try {
            channel.writeAndFlush(msg).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String ip = null;
        int port;
        if (args.length > 0) {
            ip = args[0];
        } else {
            ip = "127.0.0.1";
        }
        if (args.length > 1) {
            port = Integer.valueOf(args[1]);
        } else {
            port = 8099;
        }

        ChannelFuture cf = new Exp2Client(ip, port).run();
        if (cf == null) {
            System.exit(1);
        }
        Channel server = cf.channel();
        Scanner scanner = new Scanner(System.in);
        id = scanner.nextLine();
        register(server);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] list = line.split(" ");
//            for(int i = 0;i<list.length;i++){
//                System.out.println(list[i]);
//            }
            String opt = list[0];
            String dist = list[1];
            String t = null;
            int serverType = -1;
            int mType = -1;
            String file = null;
            String message = null;
            for (int i = 2; i < list.length; i++) {
                t = list[i];
                int start, end;
                switch (t.charAt(1)) {
                    case 'k':
                        serverType = 1;
                        break;
                    case 'd':
                        serverType = 0;
                        break;
                    case 's':
                        serverType = 3;
                        break;
                    case 'n':
                        serverType = 2;
                        break;
                    case 'f':
                        start = t.indexOf("\"") + 1;
                        end = t.lastIndexOf("\"");
                        file = t.substring(start, end);
                        mType = 1;
                        break;
                    case 'm':
                        start = t.indexOf("\"") + 1;
                        end = t.lastIndexOf("\"");
                        message = t.substring(start, end);
                        mType = 2;
                        break;
                    case '4':
                        serverType = 4;
                        break;
                    case '5':
                        serverType = 5;
                        break;
                    default:
                        break;
                }
            }
            System.out.println("操作： " + opt + " 目标： " + dist + " 模式： " + serverType + " 文件名: " + file);
            if (mType == 1 && serverType == 1) {
                KDCClientHandler.filename = file;
                Random trandom = new Random();
                int n1 = trandom.nextInt();
                ByteBuf buf = Unpooled.buffer();
                buf.writeInt(id.getBytes(StandardCharsets.UTF_8).length);
                buf.writeBytes(id.getBytes(StandardCharsets.UTF_8));

                buf.writeInt(dist.getBytes(StandardCharsets.UTF_8).length);
                buf.writeBytes(dist.getBytes(StandardCharsets.UTF_8));
                buf.writeInt(n1);

                MsgProtocol msg = new MsgProtocol();
                msg.setStep(1);
                msg.setLength(buf.readableBytes());
                byte[] content = new byte[buf.readableBytes()];
                buf.readBytes(content);
                msg.setContent(content);
                server.writeAndFlush(msg);
            }
            if (mType == 1 && serverType == 0) {
                KDCClientHandler.filename = file;
                Random trandom = new Random();
                int n1 = trandom.nextInt();
                ByteBuf buf = Unpooled.buffer();

                buf.writeInt(dist.getBytes(StandardCharsets.UTF_8).length);
                buf.writeBytes(dist.getBytes(StandardCharsets.UTF_8));
                buf.writeInt(n1);
                buf.writeInt(id.getBytes(StandardCharsets.UTF_8).length);
                buf.writeBytes(id.getBytes(StandardCharsets.UTF_8));

                byte[] content = new byte[buf.readableBytes()];
                MsgProtocol msg = new MsgProtocol();
                msg.setStep(11);
                msg.setLength(buf.readableBytes());
                buf.readBytes(content);
                msg.setContent(content);

                server.writeAndFlush(msg);
            }
            if (mType == 1 && serverType == 2) {
                CAClientHandler.filename = file;
                MsgProtocol msg = new MsgProtocol();
                msg.setStep(2);
                ByteBuf buf = Unpooled.buffer();
                buf.writeInt(dist.getBytes(StandardCharsets.UTF_8).length)
                        .writeBytes(dist.getBytes(StandardCharsets.UTF_8))
                        .writeInt(CAClientHandler.ca.length)
                        .writeBytes(CAClientHandler.ca)
                        .writeInt(id.getBytes(StandardCharsets.UTF_8).length)
                        .writeBytes(id.getBytes(StandardCharsets.UTF_8));
                byte[] content = new byte[buf.readableBytes()];

                msg.setLength(buf.readableBytes());
                buf.readBytes(content);
                msg.setContent(content);
                server.writeAndFlush(msg);
            }
            if (mType == 1 && serverType == 3) {
                CAClientHandler.filename = file;
                MsgProtocol msg = new MsgProtocol();
                msg.setStep(10);
                ByteBuf buf = Unpooled.buffer();
                buf.writeInt(dist.getBytes(StandardCharsets.UTF_8).length)
                        .writeBytes(dist.getBytes(StandardCharsets.UTF_8))
                        .writeInt(CAClientHandler.ca.length)
                        .writeBytes(CAClientHandler.ca)
                        .writeInt(id.getBytes(StandardCharsets.UTF_8).length)
                        .writeBytes(id.getBytes(StandardCharsets.UTF_8));
                byte[] content = new byte[buf.readableBytes()];

                msg.setLength(buf.readableBytes());
                buf.readBytes(content);
                msg.setContent(content);
                server.writeAndFlush(msg);
            }
            if (mType == 1 && serverType == 4) {
                CAClientHandler.filename = file;
                MsgProtocol msg = new MsgProtocol();
                msg.setStep(31);
                ByteBuf buf = Unpooled.buffer();
                buf.writeInt(dist.getBytes(StandardCharsets.UTF_8).length)
                        .writeBytes(dist.getBytes(StandardCharsets.UTF_8))
                        .writeInt(CAClientHandler.ca.length)
                        .writeBytes(CAClientHandler.ca)
                        .writeInt(id.getBytes(StandardCharsets.UTF_8).length)
                        .writeBytes(id.getBytes(StandardCharsets.UTF_8));
                byte[] content = new byte[buf.readableBytes()];

                msg.setLength(buf.readableBytes());
                buf.readBytes(content);
                msg.setContent(content);
                server.writeAndFlush(msg);
            }
            if (mType == 1 && serverType == 5) {
                CAClientHandler.filename = file;
                MsgProtocol msg = new MsgProtocol();
                msg.setStep(21);
                ByteBuf buf = Unpooled.buffer();
                buf.writeInt(dist.getBytes(StandardCharsets.UTF_8).length)
                        .writeBytes(dist.getBytes(StandardCharsets.UTF_8))
                        .writeInt(CAClientHandler.ca.length)
                        .writeBytes(CAClientHandler.ca)
                        .writeInt(id.getBytes(StandardCharsets.UTF_8).length)
                        .writeBytes(id.getBytes(StandardCharsets.UTF_8));
                byte[] content = new byte[buf.readableBytes()];

                msg.setLength(buf.readableBytes());
                buf.readBytes(content);
                msg.setContent(content);
                server.writeAndFlush(msg);
            }
        }
    }
}
