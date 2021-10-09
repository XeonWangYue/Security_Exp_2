package top.xeonwang;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;


public class ClientCommand {
    public static ChannelFuture excute(String command, Channel channel) {
        /**
         * send B -d/-k -m"[message]"
         * send B -d/-k -f"[file path]"
         */
        String[] keys = command.split(" ");
        String operation = keys[0];
        String dist = keys[1];
        for (int i = 2; i < keys.length; i++) {
            String p = keys[i];
            if (p.charAt(0) == '-') {
                switch (p.charAt(1)) {
                    case 'd': {
                        break;
                    }
                    case 'k': {
                        break;
                    }
                    case 'm': {
                        break;
                    }
                    case 'f': {
                        int start = p.indexOf("\"") + 1;
                        int end = p.lastIndexOf("\"");
                        try {
                            System.out.println(p.substring(start, end));
                            FileInputStream in = new FileInputStream(p.substring(start,end));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        }
        return null;
    }
}
