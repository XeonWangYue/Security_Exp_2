package top.xeonwang;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * @author Chen Q.
 */
public class DESUtil {
    private static volatile byte[] key = null;

    public static byte[] getkey() {
        if (key == null) {
            synchronized (DESUtil.class) {
                if (key == null) {
                    key = new byte[8];
                    key[0] = -9;
                    key[1] = -5;
                    key[2] = -75;
                    key[3] = -2;
                    key[4] = -88;
                    key[5] = -23;
                    key[6] = 127;
                    key[7] = 115;
                    return key;
                }
            }
        }
        return key;
    }

    public static byte[] generate() {
        try {
            SecureRandom random = new SecureRandom();
            KeyGenerator generator = KeyGenerator.getInstance("DES", new BouncyCastleProvider());
            generator.init(56, random);

            return generator.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(byte[] keyByte, byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance("DES", new BouncyCastleProvider());
            Key key = new SecretKeySpec(keyByte, "DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] keyByte, byte[] content) {
        try {
            Cipher cipher = Cipher.getInstance("DES", new BouncyCastleProvider());
            Key key = new SecretKeySpec(keyByte, "DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
