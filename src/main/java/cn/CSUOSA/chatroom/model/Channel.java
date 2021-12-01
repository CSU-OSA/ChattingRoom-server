package cn.csuosa.chatroom.model;

import cn.csuosa.chatroom.Out;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 频道实例
 */
public class Channel
{
    public final String name;
    public final String ticket;
    public final Map<String, User> memberMap = new ConcurrentHashMap<>();

    public Channel(String name, String ticket)
    {
        this.name = name;
        this.ticket = ticket;
    }

    public boolean checkTicket(String t)
    {
        return ticket.equals(t);
    }

    public void addMember(String nick, User user)
    {
        memberMap.put(nick, user);
        Out.ConsoleOut.info("User " + user.getUUID() + " join the channel <" + name + ">, using nick name <" + nick + ">");
    }

    public void removeMember(String nick)
    {
        Out.ConsoleOut.info("User " + memberMap.get(nick).getUUID() + " quit the channel <" + name + ">");
        memberMap.remove(nick);
    }

    public Map<String, User> getMemberMap()
    {
        return memberMap;
    }
}
