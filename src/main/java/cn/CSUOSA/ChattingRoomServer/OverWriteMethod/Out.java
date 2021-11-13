package cn.CSUOSA.ChattingRoomServer.OverWriteMethod;

import cn.CSUOSA.ChattingRoomServer.Main;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Out
{
    private static void println(String s)
    {
        Main.lineReader.printAbove(getDateTime() + " " + s);
    }

    public static void Info(String info)
    {
        println("\033[36m[Info]\t" + info + "\033[m");
    }

    public static void Warn(String info)
    {
        println("\033[33m[Warn]\t" + info + "\033[m");
    }

    public static void Err(String info)
    {
        println("\033[31m[Err]\t" + info + "\033[m");
    }

    private static String getDateTime()
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
