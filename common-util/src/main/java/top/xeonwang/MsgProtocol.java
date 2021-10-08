package top.xeonwang;

import lombok.Data;

@Data
public class MsgProtocol {
    int length;
    byte[] content;
}
