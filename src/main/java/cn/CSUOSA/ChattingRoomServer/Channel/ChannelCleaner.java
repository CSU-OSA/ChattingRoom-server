package cn.CSUOSA.ChattingRoomServer.Channel;

import cn.CSUOSA.ChattingRoomServer.Main;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;
import cn.CSUOSA.ChattingRoomServer.User.UserInfo;

public class ChannelCleaner implements Runnable
{
    @Override
    public void run()
    {
        if (!Main.ChannelList.isEmpty())
        {
            Main.ChannelList.forEach((chaName, chaInfo) -> {
                for (UserInfo nowMember : chaInfo.getMembers())
                    if (!Main.UserList.containsKey(nowMember.getNick()))
                    {
                        Out.Info("User [" + nowMember.getNick() + "] left channel [" + chaName + "]");
                        chaInfo.removeMember(nowMember);
                        if (chaInfo.getMembers().isEmpty())
                            break;
                    }
                if (chaInfo.getMembers().isEmpty() && chaInfo.getAutoClose())
                {
                    Main.ChannelList.remove(chaName);
                    Out.Info("Channel [" + chaName + "] Closed");
                }
            });
        }
    }
}
