package cn.CSUOSA.ChattingRoomServer.Message;

public class MessageInfo
{
    private final long id;
    private final String recTime;
    private final String channelName;
    private final String senderNick;
    private final String msg;

    public MessageInfo(long id, String recTime, String channelName, String senderNick, String msg)
    {
        this.id = id;
        this.recTime = recTime;
        this.channelName = channelName;
        this.senderNick = senderNick;
        this.msg = msg;
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

    public String getMsg()
    {
        return msg;
    }
}
