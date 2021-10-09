package top.xeonwang;

public class KDCClient {
    public static void excute(){
        byte[] s1 = getKs();
        byte[] s2 =decrypt1(s1);
        byte[] s3 = sub(s2);
        byte[] s4 = decrypt2(s3);
        boolean s5 =send(s4);
    }

    private static byte[] getKs() {
        return null;
    }
    private static byte[] decrypt1(byte[] s1){
        return null;
    }

    private static byte[] sub(byte[] s2){
        return null;
    }
    private static byte[] send(byte[] s3){
        return null;
    }
    private static boolean decrypt2(byte[] s4){
        return false;
    }

}
