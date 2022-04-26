package cn.csuosa.chatroom.common;

import java.util.Random;

public class RandGenerator
{
    private static char[] charSet = "0123456789AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz".toCharArray();

    public static String randSalt()
    {
        Random random = new Random();

        char[] salt = new char[32];
        for (int i = 0; i < 32; i++)
            salt[i] = charSet[random.nextInt(62)];

        return String.valueOf(salt);
    }
}
