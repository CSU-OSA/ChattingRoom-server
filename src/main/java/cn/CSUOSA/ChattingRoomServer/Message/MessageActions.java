package cn.CSUOSA.ChattingRoomServer.Message;

import cn.CSUOSA.ChattingRoomServer.ReturnParams.BoolMsgWithObj;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import static cn.CSUOSA.ChattingRoomServer.Channel.ChannelActions.verifyChannel;
import static cn.CSUOSA.ChattingRoomServer.User.UserActions.verifyUser;

@RestController
@RequestMapping("/msg")
public class MessageActions
{
    @PostMapping("/send")
    public BoolMsgWithObj sendMsg(@RequestParam(value = "usrNick") String usrNick, @RequestParam(value = "name") String name, String usrTicket, @Nullable String ticket, ArrayList<String> msg)
    {
        if (!verifyUser(usrNick, usrTicket))
            return new BoolMsgWithObj(false, "Authentication failed.");

        if (!verifyChannel(name, ticket))
            return new BoolMsgWithObj(false, "Un Known Channel.");

        return new BoolMsgWithObj(true, "").setReturnObj(msg);
    }

    @PostMapping("/get")
    public BoolMsgWithObj getMsg(@RequestParam(value = "nick") String nick, String ticket)
    {
        if (!verifyUser(nick, ticket))
            return new BoolMsgWithObj(false, "Authentication failed.");

        return new BoolMsgWithObj(true, "").setReturnObj(new MessageInfo(0, "1970-1-1 00:00:00", "Test", "Test", "TestMSG"));
    }
}
