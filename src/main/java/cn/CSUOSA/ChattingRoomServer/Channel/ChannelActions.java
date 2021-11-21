package cn.CSUOSA.ChattingRoomServer.Channel;

import cn.CSUOSA.ChattingRoomServer.Main;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;
import cn.CSUOSA.ChattingRoomServer.ReturnParams.BoolMsgWithObj;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static cn.CSUOSA.ChattingRoomServer.User.UserActions.verifyUser;

@RestController
@RequestMapping("/channel")
public class ChannelActions
{
    //验证频道存在与ticket是否正确
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

    //仅验证频道存在
    public static boolean verifyChannel(String name)
    {
        if (name.length() < 4 || name.length() > 64)
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
            return Main.ChannelList.containsKey(name);
        }
    }

    //创建频道
    @PostMapping("/create")
    public BoolMsgWithObj channelCreation(String usrNick, String usrTicket, String name, @Nullable String ticket)
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

    //加入频道
    @PostMapping("/join")
    public BoolMsgWithObj channelJoin(String usrNick, String usrTicket, String name, @Nullable String ticket)
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

    //退出频道
    @PostMapping("/quit")
    public BoolMsgWithObj channelQuit(String usrNick, String usrTicket, String name)
    {
        if (!verifyUser(usrNick, usrTicket))
            return new BoolMsgWithObj(false, "Authentication failed.");

        if (!verifyChannel(name))
            return new BoolMsgWithObj(false, "Un Known Channel.");

        if (!Main.ChannelList.get(name).getMembers().contains(Main.UserList.get(usrNick)))
            return new BoolMsgWithObj(false, "You are not a member of that channel.");

        Out.Info("User [" + usrNick + "] lefts channel [" + name + "]");
        Main.ChannelList.get(name).removeMember(Main.UserList.get(usrNick));


        return new BoolMsgWithObj(true, "");
    }

    @GetMapping("/list")
    public List<String> listUsers()
    {
        List<String> userList = new ArrayList<>();
        if (!Main.ChannelList.isEmpty())
        {
            Main.ChannelList.forEach((chaName, chaInfo) -> userList.add(chaName + " " + (chaInfo.getTicket().equals("") ? "[public]" : "[private]")));
        }

        return userList;
    }
}

