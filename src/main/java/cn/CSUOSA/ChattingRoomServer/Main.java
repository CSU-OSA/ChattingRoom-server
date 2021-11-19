package cn.CSUOSA.ChattingRoomServer;

import cn.CSUOSA.ChattingRoomServer.Channel.ChannelInfo;
import cn.CSUOSA.ChattingRoomServer.ConsoleController.CmdProcessor;
import cn.CSUOSA.ChattingRoomServer.Message.MessageListEntry;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;
import cn.CSUOSA.ChattingRoomServer.User.UserInfo;
import cn.CSUOSA.ChattingRoomServer.User.UserMapTimer;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class Main
{
    public static ConcurrentHashMap<String, UserInfo> UserList;
    public static ConcurrentHashMap<String, ChannelInfo> ChannelList;
    public static ConcurrentHashMap<Long, MessageListEntry> MsgList;
    public static UserMapTimer userMapTimer = new UserMapTimer();
    public static Terminal terminal;
    public static LineReader lineReader;
    public static long msgCount = 0;
    private static ConfigurableApplicationContext CAC;
    @Autowired
    Environment environment;

    public static void main(String[] args)
    {
        UserList = new ConcurrentHashMap<>();   //用户列表
        ChannelList = new ConcurrentHashMap<>();          //频道列表

        new Thread(new CmdProcessor()).start(); //启动命令行处理器

        CAC = SpringApplication.run(Main.class, args);  //启动SpringBoot

        new Thread(userMapTimer).start();       //启动昵称占用计时器

        try
        {
            Thread.sleep(500);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        Out.Info("Channel [PublicChannel] Opened");
        Main.ChannelList.put("PublicChannel", new ChannelInfo("PublicChannel", "", false));
    }
}