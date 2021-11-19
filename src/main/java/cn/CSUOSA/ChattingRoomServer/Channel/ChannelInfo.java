package cn.CSUOSA.ChattingRoomServer.Channel;

import cn.CSUOSA.ChattingRoomServer.Message.MessageInfo;
import cn.CSUOSA.ChattingRoomServer.User.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelInfo
{
    private final Boolean autoClose;
    private int msgCount;
    private final List<UserInfo> members;
    private final ConcurrentHashMap<Integer, MessageInfo> messageList;
    private final String name;
    private final String ticket;

    public ChannelInfo(String name, String ticket, Boolean autoClose)
    {
        this.msgCount = 0;
        this.autoClose = autoClose;
        this.name = name;
        this.ticket = ticket;
        members = new ArrayList<>();
        messageList = new ConcurrentHashMap<>();
    }

    public ChannelInfo(String name, String ticket)
    {
        this.autoClose = true;
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

    public int addMessage(MessageInfo msg)
    {
        this.messageList.put(msgCount, msg);
        msgCount++;
        return (msgCount - 1);
    }

    public MessageInfo getMessage(int msgId)
    {
        if (!this.messageList.containsKey(msgId))
            return null;
        return this.messageList.get(msgId);
    }
  
    public String getName() {return name;}

    public String getTicket() {return ticket;}

    public List<UserInfo> getMembers()
    {
        return members;
    }

    public Boolean getAutoClose()
    {
        return autoClose;
    }
}
