package cn.csuosa.chatroom.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA
{
    public static String getSHA_256(String str)
    {
        try
        {
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            return byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public static String getSHA_512(String str)
    {
        try
        {
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            return byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public static String byte2Hex(byte[] bytes)
    {
        StringBuilder stringBuilder = new StringBuilder();
        String temp = null;
        for (byte aByte : bytes)
        {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1)
            {
                //1得到一位的进行补0操作
                stringBuilder.append("0");
            }
            stringBuilder.append(temp);
        }
        return stringBuilder.toString();
    }
}
