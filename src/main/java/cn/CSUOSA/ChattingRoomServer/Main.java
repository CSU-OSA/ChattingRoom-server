package cn.CSUOSA.ChattingRoomServer;

import cn.CSUOSA.ChattingRoomServer.Channel.ChannelInfo;
import cn.CSUOSA.ChattingRoomServer.ConsoleController.CmdProcessor;
import cn.CSUOSA.ChattingRoomServer.User.UserInfo;
import cn.CSUOSA.ChattingRoomServer.User.UserMapTimer;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class Main
{
    public static ConcurrentHashMap<String, UserInfo> UserList;
    public static HashMap<String, ChannelInfo> ChannelList;
    public static UserMapTimer userMapTimer = new UserMapTimer();
    public static Terminal terminal;
    public static LineReader lineReader;
    private static ConfigurableApplicationContext CAC;
    @Autowired
    Environment environment;

    public static void main(String[] args)
    {
        UserList = new ConcurrentHashMap<>();
        ChannelList = new HashMap<>();

        CAC = SpringApplication.run(Main.class, args);
        new Thread(userMapTimer).start();

        new Thread(new CmdProcessor()).start();
    }
}