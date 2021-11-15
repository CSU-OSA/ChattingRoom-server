package cn.CSUOSA.ChattingRoomServer.Channel;

import cn.CSUOSA.ChattingRoomServer.Message.MessageInfo;
import cn.CSUOSA.ChattingRoomServer.User.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelInfo
{
    private final List<UserInfo> members;
    private final ConcurrentHashMap<Integer, MessageInfo> messageList;
    private final String name;
    private final String ticket;

    public ChannelInfo(String name, String ticket)
    {
        this.name = name;
        this.ticket = ticket;
        members = new ArrayList<>();
        messageList = new ConcurrentHashMap<>();
    }

    public boolean addMember(UserInfo member)
    {
        if (!members.contains(member))
        {
            members.add(member);
            return true;
        }
        return false;
    }

    public boolean removeMember(UserInfo member)
    {
        if (members.contains(member))
        {
            members.remove(member);
            return true;
        }
        return false;
    }
  
    public String getName() {return name;}

    public String getTicket() {return ticket;}

    public List<UserInfo> getMembers()
    {
        return members;
    }
}
