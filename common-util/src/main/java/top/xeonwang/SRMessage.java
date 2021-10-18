package top.xeonwang;

import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class SRMessage {
    private byte[] recv;
    private byte[] send;
    private byte[] content;

    SRMessage(ByteBuf buf) {
        int id1L = buf.readInt();
        recv = new byte[id1L];
        buf.readBytes(recv);

        int len = buf.readInt();
        content = new byte[len];
        buf.readBytes(content);

        int id2L = buf.readInt();
        send = new byte[id2L];
        buf.readBytes(send);
    }

    SRMessage(byte[] recv, byte[] send, byte[] content) {
        this.recv = recv;
        this.send = send;
        this.content = content;
    }

    public void reply(ByteBuf buf) {
        byte[] t = recv;
        recv = send;
        send = t;
        buf.clear();
        buf.writeInt(recv.length)
                .writeBytes(recv)
                .writeInt(content.length)
                .writeBytes(content)
                .writeInt(send.length)
                .writeBytes(send);
    }
}
