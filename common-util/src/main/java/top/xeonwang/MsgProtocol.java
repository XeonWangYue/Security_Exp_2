package top.xeonwang;

import lombok.Data;

/**
 * @author Chen Q.
 */
@Data
public class MsgProtocol {
    int step;
    int length;
    byte[] content;
}
