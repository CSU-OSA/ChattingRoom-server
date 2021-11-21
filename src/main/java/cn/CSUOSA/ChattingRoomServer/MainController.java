package cn.CSUOSA.ChattingRoomServer;

import cn.CSUOSA.ChattingRoomServer.Channel.ChannelCleaner;
import cn.CSUOSA.ChattingRoomServer.ConsoleController.CmdProcessor;
import cn.CSUOSA.ChattingRoomServer.Message.MsgListCleaner;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;
import cn.CSUOSA.ChattingRoomServer.User.UserMapCleaner;

public class MainController implements Runnable
{
    public final static CmdProcessor cmdProcessor = new CmdProcessor();
    public final static UserMapCleaner userMapCleaner = new UserMapCleaner();
    public final static MsgListCleaner msgListCleaner = new MsgListCleaner();
    public final static ChannelCleaner channelCleaner = new ChannelCleaner();
    boolean keepRunning;
    private Thread cmdProcessor_T;      //命令行控制线程
    private Thread userMapCleaner_T;    //用户列表维护线程
    private Thread msgListCleaner_T;    //主消息队列维护线程
    private Thread channelChecker_T;    //频道列表维护线程

    public MainController()
    {
        keepRunning = true;
    }

    public void closeServer()
    {
        keepRunning = false;
        synchronized (this) {notify();}

        userMapCleaner.stopRunning();
        msgListCleaner.stopRunning();
        Main.CAC.close();

        Out.Warn("Server status - stopped.");


        System.exit(0);
    }

    //启动用户列表维护线程
    public Thread runUserMapCleaner()
    {
        if (userMapCleaner_T != null && userMapCleaner_T.isAlive())
            return userMapCleaner_T;
        userMapCleaner_T = new Thread(userMapCleaner);
        userMapCleaner_T.start();
        return userMapCleaner_T;
    }

    //启动主消息队列维护线程
    public Thread runMsgListCleaner()
    {
        if (msgListCleaner_T != null && msgListCleaner_T.isAlive())
            return msgListCleaner_T;
        msgListCleaner_T = new Thread(msgListCleaner);
        msgListCleaner_T.start();
        return msgListCleaner_T;
    }

    //启动频道维护线程
    public Thread runChannelCleaner()
    {
        if (channelChecker_T != null && channelChecker_T.isAlive())
            return channelChecker_T;
        channelChecker_T = new Thread(channelCleaner);
        channelChecker_T.start();
        return channelChecker_T;
    }

    @Override
    public synchronized void run()
    {
        cmdProcessor_T = new Thread(cmdProcessor);    //启动命令行处理器
        cmdProcessor_T.start();

        try
        {
            //等待100ms, 防止线程未启动导致lineReader为null
            Thread.sleep(100);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        runUserMapCleaner();    //启动用户列表维护线程
        runMsgListCleaner();    //启动主消息队列维护线程

        while (keepRunning)
        {
            if (cmdProcessor_T == null || !cmdProcessor_T.isAlive())
            {
                System.out.println("\033[31m" + "[W] CmdProcessor thread is not running. ProtectThread is trying to fix this problem." + "\033[m");
                cmdProcessor_T = new Thread(cmdProcessor);    //重新启动启动命令行处理器
                cmdProcessor_T.start();

                if (cmdProcessor_T == null || !cmdProcessor_T.isAlive())
                {
                    System.out.println("\033[31m" + "[E] Failed to restart the CmdProcessor thread. Server stopped." + "\033[m");
                    closeServer();
                }
                System.out.println("\033[31m" + "[W] CmdProcessor thread has been restarted" + "\033[m");
            }

            if (userMapCleaner_T == null || !userMapCleaner_T.isAlive())
            {
                Out.Warn("UserMapCleaner thread is not running. ProtectThread is trying to fix this problem.");
                userMapCleaner_T = runUserMapCleaner();
                if (userMapCleaner_T == null || !userMapCleaner_T.isAlive())
                {
                    Out.Err("Failed to restart the UserMapCleaner thread. Server stopped.");
                    closeServer();
                }
                Out.Warn("UserMapCleaner thread has been restarted");
            }

            if (msgListCleaner_T == null || !msgListCleaner_T.isAlive())
            {
                Out.Warn("MsgListCleaner thread is not running. ProtectThread is trying to fix this problem.");
                msgListCleaner_T = runMsgListCleaner();
                if (msgListCleaner_T == null || !msgListCleaner_T.isAlive())
                {
                    Out.Err("Failed to restart the MsgListCleaner thread. Server stopped.");
                    closeServer();
                }
                Out.Warn("MsgListCleaner thread has been restarted");
            }

            try
            {
                wait(1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
