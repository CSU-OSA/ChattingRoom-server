package cn.CSUOSA.ChattingRoomServer.User;

import java.util.ArrayList;

public class UserInfo
{
    private static final int TimeInternal = 30; //second
    private final ArrayList<Long> msgList;
    private final String nick;
    private final String ticket;
    public int leftTime;

    public UserInfo(String nick, String ticket)
    {
        msgList = new ArrayList<>();
        this.nick = nick;
        this.ticket = ticket;
        this.leftTime = TimeInternal;
    }

    public void addMsg(long mid)
    {
        msgList.add(mid);
    }

    public ArrayList<Long> getMsgList()
    {
        ArrayList<Long> cpy = new ArrayList<>(msgList);
        msgList.clear();
        return cpy;
    }

    public void ResetLeftTime() {leftTime = TimeInternal;}

    public String getNick() {return nick;}

    public String getTicket() {return ticket;}
}
