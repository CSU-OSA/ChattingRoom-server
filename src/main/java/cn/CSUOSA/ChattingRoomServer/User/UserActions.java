package cn.CSUOSA.ChattingRoomServer.User;

import cn.CSUOSA.ChattingRoomServer.Main;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;
import cn.CSUOSA.ChattingRoomServer.ReturnParams.BoolMsgWithObj;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserActions
{
    //验证用户
    public static boolean verifyUser(String nick, String ticket)
    {
        if (nick.length() < 4 || nick.length() > 32 || ticket.length() != 6)
            return false;
        else
        {
            //查找是否含有非法字符
            for (char ch : nick.toCharArray())
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
            if (!Main.UserList.containsKey(nick) || !Main.UserList.get(nick).getTicket().equals(ticket))
                return false;
            Main.UserList.get(nick).ResetLeftTime();
            return true;
        }
    }

    //用户登录相关
    @PostMapping("/login")
    public BoolMsgWithObj userLogin(String nick, String ticket)
    {
        if (nick.length() < 4 || nick.length() > 32 || ticket.length() != 6)
            return new BoolMsgWithObj(false, "Invalid nick or ticket length.");
        else
        {
            //查找是否含有非法字符
            for (char ch : nick.toCharArray())
            {
                if (((ch < 48) || (ch > 57)) && ((ch < 65) || (ch > 90)) && ((ch < 97) || (ch > 122)))
                {
                    return new BoolMsgWithObj(false, "Invalid nick.");
                }
            }
            for (char ch : ticket.toCharArray())
            {
                if (((ch < 48) || (ch > 57)) && ((ch < 65) || (ch > 90)) && ((ch < 97) || (ch > 122)))
                {
                    return new BoolMsgWithObj(false, "Invalid ticket.");
                }
            }
            if (Main.UserList.containsKey(nick))
                return new BoolMsgWithObj(false, "Nickname already taken.");

            Out.Info("Nick [" + nick + "] Occupied");
            Main.UserList.put(nick, new UserInfo(nick, ticket));    //添加昵称占用与计时器
            return new BoolMsgWithObj(true, "");
        }
    }

    @PostMapping("/renew")
    public BoolMsgWithObj userRenew(String nick, String ticket)
    {
        if (!verifyUser(nick, ticket))
            return new BoolMsgWithObj(false, "Authentication failed.");

        Main.UserList.get(nick).ResetLeftTime();
        return new BoolMsgWithObj(true, "");
    }

    @PostMapping("/logout")
    public BoolMsgWithObj userLogout(String nick, String ticket)
    {
        if (!verifyUser(nick, ticket))
            return new BoolMsgWithObj(false, "Authentication failed.");

        Main.UserList.remove(nick);
        Out.Info("Nick [" + nick + "] Released");
        Main.mainController.runChannelCleaner();

        return new BoolMsgWithObj(true, "");
    }

    @GetMapping("/list")
    public List<String> listUsers()
    {
        List<String> userList = new ArrayList<>();
        if (!Main.UserList.isEmpty())
        {
            for (Iterator<String> it = Main.UserList.keys().asIterator(); it.hasNext(); )
                userList.add(it.next());
        }

        return userList;
    }
}
