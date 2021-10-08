package top.xeonwang;

import java.security.Key;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Chen Q.
 */
public class KeySaver{
    private ConcurrentHashMap<String,Key> Userkeys;

    public KeySaver(){
        this.Userkeys = new ConcurrentHashMap<>();
    }

    public void addUserKey(String user, Key key){
        Userkeys.put(user,key);
    }

    public Key getUserKey(String user){
        return Userkeys.get(user);
    }
}
