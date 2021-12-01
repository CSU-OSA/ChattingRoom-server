package cn.csuosa.chatroom;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Out
{
    public static class ConsoleOut
    {
        private static void print(String str)
        {
            if (Main.lineReader != null)
                Main.lineReader.printAbove(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + str);
        }

        public static void info(String info)
        {
            print("\033[36m[I] " + info + "\033[m");
        }

        public static void warn(String warn)
        {
            print("\033[33m[W] " + warn + "\033[m");
        }

        public static void err(String err)
        {
            print("\033[31m[E] " + err + "\033[m");
        }

        public static void dbg(String err)
        {
            print("\033[35m[D] " + err + "\033[m");
        }
    }
}
