package top.xeonwang;

import io.netty.buffer.ByteBuf;

public class BufUtil {
    public static byte[] getByte(ByteBuf buf) {
        int len = buf.readInt();
        byte[] content = new byte[len];
        buf.readBytes(content);
        return content;
    }

    public static void addByte(ByteBuf buf, byte[] content) {
        buf.writeInt(content.length)
                .writeBytes(content);
    }
}
