package top.xeonwang;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Chen Q.
 */
public class UserIdSaver {
    private volatile static UserIdSaver userIpSaver;
    private ConcurrentHashMap<Long, String> saver;;

    private UserIdSaver(){
        this.saver = new ConcurrentHashMap<>();
    }

    public static UserIdSaver getInstance(){
        if(userIpSaver==null){
            synchronized (UserIdSaver.class){
                if(userIpSaver==null){
                    userIpSaver = new UserIdSaver();
                }
            }
        }
        return userIpSaver;
    }

    public void addUserId(Long user, String id) {
        saver.put(user, id);
    }

    public String getUserId(Long user) {
        return saver.get(user);
    }

    public void clear() {
        saver.clear();
    }

    public void readin(byte[] map) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(map);
        int len = buf.readInt();
        for (int i = 0; i < len; i++) {
            Long uid = buf.readLong();
            int length = buf.readInt();
            byte[] id = new byte[length];
            buf.readBytes(id);
            saver.put(uid, new String(id,StandardCharsets.UTF_8));
            System.out.println(saver.get(uid));
        }
    }

    public byte[] output() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(saver.size());
        saver.forEach((key, value) -> {
            buf.writeLong(key);
            buf.writeInt(value.getBytes(StandardCharsets.UTF_8).length);
            buf.writeBytes(value.getBytes(StandardCharsets.UTF_8));
        });
        byte[] res = new byte[buf.readableBytes()];
        buf.readBytes(res);
        return res;
    }

//    public static void main(String[] args) {
//        UserIpSaver saver = new UserIpSaver();
//
//        InetAddress address1 = null;
//        InetAddress address2 = null;
//        InetAddress address3 = null;
//        try {
//            String ip1 = "123.123.123.113";
//            address1 =Inet4Address.getByName(ip1);
//            saver.addUserKey(996L, address1.getAddress());
//            String ip2 = "2.8.42.10";
//            address2 =Inet4Address.getByName(ip2);
//            saver.addUserKey(997L, address2.getAddress());
//            String ip3 = "89.40.10.113";
//            address3 =Inet4Address.getByName(ip3);
//            saver.addUserKey(998L, address3.getAddress());
//            byte[] stream = saver.output();
//
//            saver.clear();
//
//            saver.readin(stream);
//            InetAddress ipo = InetAddress.getByAddress(saver.getUserKey(997L));
//            System.out.println(ipo.getHostAddress());
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//
//    }
}
