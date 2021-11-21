package cn.CSUOSA.ChattingRoomServer.Message;

import cn.CSUOSA.ChattingRoomServer.Main;
import cn.CSUOSA.ChattingRoomServer.ReturnParams.BoolMsgWithObj;
import cn.CSUOSA.ChattingRoomServer.User.UserInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static cn.CSUOSA.ChattingRoomServer.Channel.ChannelActions.verifyChannel;
import static cn.CSUOSA.ChattingRoomServer.User.UserActions.verifyUser;

@RestController
@RequestMapping("/msg")
public class MessageActions
{
    @PostMapping("/send")
    public BoolMsgWithObj sendMsg(String usrNick, String usrTicket, String name, String msg)
    {
        if (!verifyUser(usrNick, usrTicket))
            return new BoolMsgWithObj(false, "Authentication failed.");

        if (!verifyChannel(name))
            return new BoolMsgWithObj(false, "Un Known Channel.");

        if (!Main.ChannelList.get(name).getMembers().contains(Main.UserList.get(usrNick)))
            return new BoolMsgWithObj(false, "You are not a member of that channel.");

        MessageInfo newMsg = new MessageInfo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), name, usrNick, new ArrayList<>(Collections.singletonList(msg)));
        long mid = newMsg.getMsgID();
        MessageListEntry msgListEntry = new MessageListEntry(newMsg);

        Main.MsgList.put(mid, msgListEntry);

        for (UserInfo it : Main.ChannelList.get(name).getMembers())
        {
            it.addMsg(mid);
            msgListEntry.addQuoteCount();
        }

        return new BoolMsgWithObj(true, "").setReturnObj(msg);
    }

    @PostMapping("/get")
    public BoolMsgWithObj getMsg(String nick, String ticket)
    {
        if (!verifyUser(nick, ticket))
            return new BoolMsgWithObj(false, "Authentication failed.");

        ArrayList<MessageInfo> msgList = new ArrayList<>();

        for (long mid : Main.UserList.get(nick).getMsgList())
        {
            msgList.add(Main.MsgList.get(mid).messageInfo);
            Main.MsgList.get(mid).minusQuoteCount();
        }

        return new BoolMsgWithObj(true, "").setReturnObj(msgList);
    }
}
