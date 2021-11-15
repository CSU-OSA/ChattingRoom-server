package cn.CSUOSA.ChattingRoomServer.Channel;

import cn.CSUOSA.ChattingRoomServer.Main;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;
import cn.CSUOSA.ChattingRoomServer.User.UserInfo;

public class ChannelChecker implements Runnable
{
    @Override
    public void run()
    {
        if (!Main.ChannelList.isEmpty())
        {
            ChannelInfo channelInfo;
            for (String nowKey : Main.ChannelList.keySet())
            {
                channelInfo = Main.ChannelList.get(nowKey);
                for (UserInfo nowMember : channelInfo.getMembers())
                    if (nowMember != null && !Main.UserList.containsKey(nowMember.getNick()))
                    {
                        Out.Info("User [" + nowMember.getNick() + "] left channel [" + nowKey + "]");
                        channelInfo.removeMember(nowMember);
                        if (channelInfo.getMembers().isEmpty())
                            break;
                    }
                if (channelInfo.getMembers().isEmpty())
                {
                    Main.ChannelList.remove(nowKey);
                    Out.Info("Channel [" + nowKey + "] Closed");
                }
            }
        }
    }
}
