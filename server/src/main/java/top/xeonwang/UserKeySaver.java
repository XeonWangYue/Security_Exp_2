package top.xeonwang;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Chen Q.
 */
public class UserKeySaver {
    private static volatile UserKeySaver userKeySaver;
    private ConcurrentHashMap<String, byte[]> saver;
    private UserKeySaver(){
        this.saver= new ConcurrentHashMap<>();
    }
    public static UserKeySaver getInstance(){
        if(userKeySaver == null){
            synchronized (UserKeySaver.class){
                if(userKeySaver==null){
                    userKeySaver = new UserKeySaver();
                }
            }
        }
        return userKeySaver;
    }

    public void addUserKey(String id, byte[] key) {
        saver.put(id, key);
    }

    public byte[] getUserKey(String id) {
        return saver.get(id);
    }
}
