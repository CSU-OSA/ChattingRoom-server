package cn.CSUOSA.ChattingRoomServer.Message;

import cn.CSUOSA.ChattingRoomServer.Main;

import java.util.ArrayList;

public class MessageInfo
{
    private final long msgID;
    private final String recTime;
    private final String channelName;
    private final String senderNick;
    private final ArrayList<String> msg;

    public MessageInfo(String recTime, String channelName, String senderNick, ArrayList<String> msg)
    {
        Main.msgCount++;
        this.msgID = Main.msgCount;
        this.recTime = recTime;
        this.channelName = channelName;
        this.senderNick = senderNick;
        this.msg = msg;
    }

    public long getMsgID()
    {
        return msgID;
    }

    public String getRecTime()
    {
        return recTime;
    }

    public String getChannelName()
    {
        return channelName;
    }

    public String getSenderNick()
    {
        return senderNick;
    }

    public ArrayList<String> getMsg()
    {
        return msg;
    }
}
