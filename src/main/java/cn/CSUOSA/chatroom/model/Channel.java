package cn.csuosa.chatroom.model;

import cn.csuosa.chatroom.Core;
import cn.csuosa.chatroom.Out;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 频道实例
 */
public class Channel
{
    public final Map<String, OnlineUser> memberMap = new ConcurrentHashMap<>();
    public final String name;
    public final String ticket;
    public boolean closeable;

    public Channel(String name, String ticket, boolean closeable)
    {
        this.closeable = closeable;
        this.name = name;
        this.ticket = ticket;
    }

    public boolean checkTicket(String t)
    {
        return ticket.equals(t);
    }

    public void addMember(String nick, OnlineUser onlineUser)
    {
        memberMap.put(nick, onlineUser);
        Out.ConsoleOut.info("User " + onlineUser.getUUID() + " join the channel <" + name + ">, using nick name <" + nick + ">");
        Core.infoPushService.pushChannelList();
    }

    public void removeMember(String nick)
    {
        Out.ConsoleOut.info("User " + memberMap.get(nick).getUUID() + " quit the channel <" + name + ">");
        memberMap.remove(nick);

        if (memberMap.isEmpty() && closeable)
            Core.removeCha(name);

        Core.infoPushService.pushChannelList();
    }

    public Map<String, OnlineUser> getMemberMap()
    {
        return memberMap;
    }
}
