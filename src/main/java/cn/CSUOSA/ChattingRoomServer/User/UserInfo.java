package cn.CSUOSA.ChattingRoomServer.User;

public class UserInfo
{
    private static final int TimeInternal = 30; //second
    private final String nick;
    private final String ticket;
    public int leftTime;

    public UserInfo(String nick, String ticket)
    {
        this.nick = nick;
        this.ticket = ticket;
        this.leftTime = TimeInternal;
    }

    public void ResetLeftTime() {leftTime = TimeInternal;}

    public String getNick() {return nick;}

    public String getTicket() {return ticket;}
}
