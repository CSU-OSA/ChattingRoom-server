package cn.csuosa.chatroom.serivce;

import cn.csuosa.chatroom.Core;
import cn.csuosa.chatroom.model.Channel;
import cn.csuosa.chatroom.model.OnlineUser;
import cn.csuosa.chatroom.proto.Response;
import io.netty.util.AttributeKey;

import java.util.ArrayList;

public class InfoPushService
{
    /**
     * 推送频道列表
     */
    public void pushChannelList()
    {
        Core.getOnlineUserMap().forEach((s, onlineUser) -> {
            String uuid = onlineUser.getCtx().channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get().getUUID();

            Response.ResponsePOJO.Builder ret = Response.ResponsePOJO.newBuilder();
            ret.setType(Response.ResponsePOJO.Type.PUSH_CHA_LIST);

            Core.getChaMap().forEach((channelName, channel) -> ret.addChannelInfo(Response.ChannelInfo.newBuilder()
                    .setName(channelName)
                    .setIsPublic(channel.checkTicket(""))
                    .setMemberNum(channel.getMemberMap().size())
                    .setIsIN(onlineUser.isInChannel(channelName))
                    .build()));
            onlineUser.getCtx().writeAndFlush(ret.build());
        });
    }

    public void pushMemberList(Channel channel)
    {
        Response.ResponsePOJO.Builder ret = Response.ResponsePOJO.newBuilder();

        ret.setType(Response.ResponsePOJO.Type.PUSH_MEMBER_LIST);

        ArrayList<String> memberList = new ArrayList<>();
        channel.getMemberMap().values().forEach(member -> memberList.add(member.getNick(channel.name)));

        ret.setMemberInfo(Response.MemberInfo.newBuilder()
                .setChannelName(channel.name)
                .addAllMemberNick(memberList)
                .build());

        Response.ResponsePOJO responsePOJO = ret.build();

        channel.getMemberMap().values().forEach(member -> member.getCtx().writeAndFlush(responsePOJO));
    }

    public void pushOnlineUser()
    {
        Response.ResponsePOJO.Builder ret = Response.ResponsePOJO.newBuilder();

        ret.setType(Response.ResponsePOJO.Type.PUSH_MEMBER_LIST);

        ArrayList<String> memberList = new ArrayList<>();
        //Core.getUserMap().forEach((s, user) -> user.getNickNames().forEach(()));

        ret.setMemberInfo(Response.MemberInfo.newBuilder()
                .setChannelName("")
                .addAllMemberNick(memberList)
                .build());

        Response.ResponsePOJO responsePOJO = ret.build();

        Core.getOnlineUserMap().forEach((s, onlineUser) -> onlineUser.getCtx().writeAndFlush(responsePOJO));
    }
}
