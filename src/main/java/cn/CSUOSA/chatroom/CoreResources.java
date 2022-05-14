package cn.csuosa.chatroom;

import cn.csuosa.chatroom.model.Message;
import cn.csuosa.chatroom.model.OnlineUser;
import cn.csuosa.chatroom.serivce.InfoPushService;
import cn.csuosa.chatroom.serivce.OnlineUserService;
import cn.csuosa.chatroom.serivce.RegUserService;
import cn.csuosa.chatroom.serivce.TalkChannelService;
import io.netty.channel.nio.NioEventLoopGroup;


/**
 * 核心资源类
 * <p>
 * 包含三个核心实例：
 * <p>
 * onlineUserMap - uuid->在线用户实例 映射表
 * <p>
 * loginUserMap - uid->在线用户实例 映射表
 * <p>
 * chaMap  - 频道名->频道实例 映射表
 */
public class CoreResources
{
    public static final OnlineUserService onlineUserService = new OnlineUserService();
    public static final RegUserService regUserService = new RegUserService();
    public static final TalkChannelService talkChannelService = new TalkChannelService();
    public static final InfoPushService infoPushService = new InfoPushService();            //信息推送服务

    public static final NioEventLoopGroup worker = new NioEventLoopGroup();

    /**
     * 在线用户加入频道 - 将在线用户加入到频道中
     *
     * @param onlineUser 在线用户实例
     * @param nick       该用户加入频道时使用的Nick
     * @param chaName    频道名
     */
    public static void userJoinCha(OnlineUser onlineUser, String nick, String chaName)
    {
        talkChannelService.getTalkChannel(chaName).addMember(nick, onlineUser);
        onlineUser.join(chaName, nick);
        Out.ConsoleOut.info("User " + onlineUser.getUUID() + " join the channel [" + chaName + "], using nick name <" + nick + ">");
        infoPushService.pushChannelListToAll();
    }

    /**
     * 在线用户加入频道 - 将在线用户加入到频道中
     *
     * @param onlineUser 在线用户实例
     * @param chaName    频道名
     */
    public static void userQuitCha(OnlineUser onlineUser, String chaName)
    {
        talkChannelService.getTalkChannel(chaName).removeMember(onlineUser.getNick(chaName));
        //若频道的成员列表为空且可以自动关闭，则关闭之
        if (talkChannelService.getTalkChannel(chaName).getMemberMap().isEmpty()
                && talkChannelService.getTalkChannel(chaName).isAutoClose())
        {
            talkChannelService.removeCha(chaName);
            return;
        }
        onlineUser.quit(chaName);
        Out.ConsoleOut.info("User " + onlineUser.getUUID() + " join the channel [" + chaName + "]");
        infoPushService.pushChannelListToAll();
    }

    /**
     * 向频道中发送消息
     *
     * @param message 消息实例
     */
    public static void addMessage(Message message)
    {
        talkChannelService.getTalkChannel(message.getChannel()).getMemberMap()
                .forEach((nick, onlineUser) -> {
                    if (!nick.equals(message.getFromNick()))
                        onlineUser.receive(message);
                });
    }
}
