package cn.CSUOSA.ChattingRoomServer.Channel;

import cn.CSUOSA.ChattingRoomServer.Main;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;
import cn.CSUOSA.ChattingRoomServer.ReturnParams.BoolMsgWithObj;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.CSUOSA.ChattingRoomServer.User.UserActions.verifyUser;

@RestController
@RequestMapping("/channel")
public class ChannelActions
{
    public static boolean verifyChannel(String name, String ticket)
    {
        if (ticket == null)
            ticket = "";

        if (name.length() < 4 || name.length() > 64 || (!ticket.isEmpty() && ticket.length() != 6))
            return false;
        else
        {
            //查找是否含有非法字符
            for (char ch : name.toCharArray())
            {
                if (((ch < 48) || (ch > 57)) && ((ch < 65) || (ch > 90)) && ((ch < 97) || (ch > 122)))
                {
                    return false;
                }
            }
            for (char ch : ticket.toCharArray())
            {
                if (((ch < 48) || (ch > 57)) && ((ch < 65) || (ch > 90)) && ((ch < 97) || (ch > 122)))
                {
                    return false;
                }
            }
            return Main.ChannelList.containsKey(name) && Main.ChannelList.get(name).getTicket().equals(ticket);
        }
    }

    //频道创建相关
    @PostMapping("/create")
    public BoolMsgWithObj channelCreation(@RequestParam(value = "usrNick") String usrNick, @RequestParam(value = "name") String name, String usrTicket, @Nullable String ticket)
    {
        if (!verifyUser(usrNick, usrTicket))
            return new BoolMsgWithObj(false, "Authentication failed.");

        if (name.length() < 4 || name.length() > 64 || (ticket != null && ticket.length() != 6))
            return new BoolMsgWithObj(false, "Invalid name or ticket length.");
        else
        {
            //查找是否含有非法字符
            for (char ch : name.toCharArray())
            {
                if (((ch < 48) || (ch > 57)) && ((ch < 65) || (ch > 90)) && ((ch < 97) || (ch > 122)))
                {
                    return new BoolMsgWithObj(false, "Invalid name.");
                }
            }
            if (ticket != null)
                for (char ch : ticket.toCharArray())
                {
                    if (((ch < 48) || (ch > 57)) && ((ch < 65) || (ch > 90)) && ((ch < 97) || (ch > 122)))
                    {
                        return new BoolMsgWithObj(false, "Invalid ticket.");
                    }
                }
            if (Main.ChannelList.containsKey(name))
                return new BoolMsgWithObj(false, "Channel already opened.");

            Out.Info("Channel [" + name + "] Created");

            Main.ChannelList.put(name, new ChannelInfo(name, (ticket == null) ? "" : ticket));
            Out.Info("User [" + usrNick + "] joined channel [" + name + "]");
            Main.ChannelList.get(name).addMember(Main.UserList.get(usrNick));
            return new BoolMsgWithObj(true, "");
        }
    }

    @PostMapping("/join")
    public BoolMsgWithObj channelJoin(@RequestParam(value = "usrNick") String usrNick, @RequestParam(value = "name") String name, String usrTicket, @Nullable String ticket)
    {
        if (!verifyUser(usrNick, usrTicket))
            return new BoolMsgWithObj(false, "Authentication failed.");

        if (name.length() < 4 || name.length() > 64 || (ticket != null && ticket.length() != 6))
            return new BoolMsgWithObj(false, "Invalid name or ticket length.");
        else
        {
            //查找是否含有非法字符
            for (char ch : name.toCharArray())
            {
                if (((ch < 48) || (ch > 57)) && ((ch < 65) || (ch > 90)) && ((ch < 97) || (ch > 122)))
                {
                    return new BoolMsgWithObj(false, "Invalid name.");
                }
            }
            if (ticket != null)
                for (char ch : ticket.toCharArray())
                {
                    if (((ch < 48) || (ch > 57)) && ((ch < 65) || (ch > 90)) && ((ch < 97) || (ch > 122)))
                    {
                        return new BoolMsgWithObj(false, "Invalid ticket.");
                    }
                }
            if (!Main.ChannelList.containsKey(name))
                return new BoolMsgWithObj(false, "Channel does not exist.");
            if (!Main.ChannelList.get(name).getTicket().isEmpty() && ticket == null)
                return new BoolMsgWithObj(false, "This channel needs a ticket.");
            if (!Main.ChannelList.get(name).getTicket().isEmpty() && ticket != null && !Main.ChannelList.get(name).getTicket().equals(ticket))
                return new BoolMsgWithObj(false, "Wrong ticket.");
            if (Main.ChannelList.get(name).getMembers().contains(Main.UserList.get(usrNick)))
                return new BoolMsgWithObj(false, "You have already in this channel.");

            Out.Info("User [" + usrNick + "] joined channel [" + name + "]");
            Main.ChannelList.get(name).addMember(Main.UserList.get(usrNick));
            return new BoolMsgWithObj(true, "");
        }
    }
}
