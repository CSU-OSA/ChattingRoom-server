package cn.csuosa.chatroom.serivce;

import cn.csuosa.chatroom.CoreResources;
import cn.csuosa.chatroom.model.Message;
import cn.csuosa.chatroom.model.OnlineUser;
import cn.csuosa.chatroom.proto.Response;
import com.google.protobuf.ByteString;

import java.util.ArrayList;

public class InfoPushService
{
    /**
     * 向全体用户推送频道列表（不包含私聊频道）
     * <p>
     * （一般在频道创建或关闭时使用）
     */
    public void pushChannelListToAll()
    {
        Response.ResponsePOJO.Builder ret = Response.ResponsePOJO.newBuilder();
        ret.setType(Response.ResponsePOJO.Type.PUSH_CHA_LIST);

        ArrayList<Response.ChannelInfo.Builder> channelInfoBuilderList = CoreResources.talkChannelService.getChannelInfo();

        CoreResources.onlineUserService.getOnlineUserMap().forEach((s, onlineUser) -> {
            ret.clearChannelInfo(); //清空频道信息记录
            channelInfoBuilderList.forEach(builder -> ret.addChannelInfo(builder
                    .setIsIN(onlineUser.isInChannel(builder.getName()))
                    .build())); //根据用户所在设置频道
            onlineUser.getCtx().writeAndFlush(ret.build()); //构造、推送消息
        });
    }

    /**
     * 向某用户推送频道列表（不包含私聊频道）
     * <p>
     * （一般在用户登录时使用）
     *
     * @param onlineUser 用户的用户实例
     */
    public void pushChannelList(OnlineUser onlineUser)
    {
        Response.ResponsePOJO.Builder ret = Response.ResponsePOJO.newBuilder();
        ret.setType(Response.ResponsePOJO.Type.PUSH_CHA_LIST);

        ArrayList<Response.ChannelInfo.Builder> channelInfoBuilderList = CoreResources.talkChannelService.getChannelInfo();

        channelInfoBuilderList.forEach(builder -> ret.addChannelInfo(builder
                .setIsIN(onlineUser.isInChannel(builder.getName()))
                .build())); //根据用户所在设置频道
        onlineUser.getCtx().writeAndFlush(ret.build()); //构造、推送消息
    }

    /**
     * 向全体用户推送系统消息
     *
     * @param msg 消息内容
     */
    public void sendSysMsgToAll(String msg, String fromNick)
    {
        Response.ResponsePOJO ret = Response.ResponsePOJO.newBuilder()
                .setType(Response.ResponsePOJO.Type.PUSH_MSG)
                .setMessage(Response.Message.newBuilder()
                        .setChannel("System")
                        .setType(Message.MessageType.PLAIN_TEXT.ordinal())
                        .setFromNick(fromNick)
                        .setContent(ByteString.copyFrom(msg.getBytes()))
                        .build())
                .build();

        CoreResources.onlineUserService.getOnlineUserMap().forEach((s, onlineUser) -> {
            onlineUser.getCtx().writeAndFlush(ret); //推送消息
        });
    }
}
