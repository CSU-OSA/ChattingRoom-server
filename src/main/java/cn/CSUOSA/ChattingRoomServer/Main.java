package cn.CSUOSA.ChattingRoomServer;

import cn.CSUOSA.ChattingRoomServer.Channel.ChannelInfo;
import cn.CSUOSA.ChattingRoomServer.Message.MessageListEntry;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;
import cn.CSUOSA.ChattingRoomServer.User.UserInfo;
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

    public final static ConcurrentHashMap<String, UserInfo> UserList = new ConcurrentHashMap<>();   //用户列表
    public final static ConcurrentHashMap<String, ChannelInfo> ChannelList = new ConcurrentHashMap<>();   //频道列表
    public final static ConcurrentHashMap<Long, MessageListEntry> MsgList = new ConcurrentHashMap<>();    //消息队列
    public final static MainController mainController = new MainController();

    public static Terminal terminal;
    public static LineReader lineReader;
    public static long msgCount = 0;

    public static ConfigurableApplicationContext CAC;

    @Autowired
    Environment environment;

    public static void main(String[] args)
    {
        System.out.println("""
                \033[34m
                   _____  _____ _   _        _           _   _   _
                  /  __ \\/  ___| | | |      | |         | | | | (_)
                  | /  \\/\\ `--.| | | |   ___| |__   __ _| |_| |_ _ _ __   __ _
                  | |     `--. \\ | | |  / __| '_ \\ / _` | __| __| | '_ \\ / _` |
                  | \\__/\\/\\__/ / |_| | | (__| | | | (_| | |_| |_| | | | | (_| |
                   \\____/\\____/ \\___/   \\___|_| |_|\\__,_|\\__|\\__|_|_| |_|\\__, |
                                                                          __/ |
                                                                         |___/

                CSU-OSA Chatting Room Server(v0.4.0-beta)
                ------
                CSU-OSA all rights reserved.
                \033[m
                """);

        CAC = SpringApplication.run(Main.class, args);  //启动SpringBoot

        new Thread(mainController).start(); //启动中控线程

        try
        {
            //等待200ms
            Thread.sleep(500);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        Out.Info("Channel [PublicChannel] Opened");

        Main.ChannelList.put("PublicChannel", new ChannelInfo("PublicChannel", "", false));
    }
}