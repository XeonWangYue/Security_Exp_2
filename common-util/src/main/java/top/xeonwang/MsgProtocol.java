package top.xeonwang;

import lombok.Data;

/**
 * @author Chen Q.
 */
@Data
public class MsgProtocol {
    private int step;
    private int length;
    private byte[] content;

    MsgProtocol() {

    }

    MsgProtocol(int step, byte[] content) {
        this.step = step;
        this.length = content.length;
        this.content = content;
    }
}
