package top.xeonwang;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static byte[] MD5(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(content);
            byte[] res = digest.digest();
            return res;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
