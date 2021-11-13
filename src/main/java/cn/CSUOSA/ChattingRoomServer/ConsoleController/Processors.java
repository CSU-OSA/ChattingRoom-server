package cn.CSUOSA.ChattingRoomServer.ConsoleController;

import cn.CSUOSA.ChattingRoomServer.Channel.ChannelInfo;
import cn.CSUOSA.ChattingRoomServer.Main;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;
import cn.CSUOSA.ChattingRoomServer.User.UserInfo;

public class Processors
{
    public static void listObject(String[] args)
    {
        switch (args[1])
        {
            case "-u", "--user" -> {
                if (Main.UserList.isEmpty())
                    Out.Warn("No User Online.");
                else
                {
                    UserInfo userInfo;
                    StringBuilder strBuilder = new StringBuilder().append("\n");
                    strBuilder.append("User Count: ").append(Main.UserList.size()).append("\n");
                    for (String nowKey : Main.UserList.keySet())
                    {
                        userInfo = Main.UserList.get(nowKey);
                        strBuilder.append(userInfo.getNick())
                                .append("  ")
                                .append(userInfo.leftTime)
                                .append("\n");
                    }
                    strBuilder.append("\n");

                    Out.Info(strBuilder.toString());
                }
            }
            case "-g", "--group" -> {
                if (Main.ChannelList.isEmpty())
                    Out.Warn("No Channel Opened.");
                else
                {
                    ChannelInfo channelInfo;
                    StringBuilder strBuilder = new StringBuilder().append("\n");
                    strBuilder.append("Channel Count: ").append(Main.ChannelList.size()).append("\n");
                    for (String nowKey : Main.ChannelList.keySet())
                    {
                        channelInfo = Main.ChannelList.get(nowKey);
                        strBuilder.append(channelInfo.getName())
                                .append("  ")
                                .append(channelInfo.getTicket().isEmpty() ? "-null" : channelInfo.getTicket())
                                .append("\n");
                    }
                    strBuilder.append("\n");

                    Out.Info(strBuilder.toString());
                }
            }
        }

    }
}
