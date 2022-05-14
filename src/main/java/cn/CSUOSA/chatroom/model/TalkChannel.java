package cn.csuosa.chatroom.model;

import cn.csuosa.chatroom.proto.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 频道实例
 */
public class TalkChannel
{
    private final Map<String, OnlineUser> memberMap = new ConcurrentHashMap<>();
    @Getter
    private final String name;
    private final String ticket;
    @Getter
    @Setter
    private boolean autoClose;

    public TalkChannel(String name, String ticket, boolean autoClose)
    {
        this.autoClose = autoClose;
        this.name = name;
        this.ticket = ticket;
    }

    /**
     * 检查准入ticket是否正确
     *
     * @param t 用户提供的准入ticket
     * @return 布尔值-ticket是否正确
     */
    public boolean checkTicket(String t)
    {
        return ticket.equals(t);
    }

    /**
     * @param nick
     * @param onlineUser
     */
    public void addMember(String nick, OnlineUser onlineUser)
    {
        memberMap.put(nick, onlineUser);
        pushMemberList();
    }

    public void removeMember(String nick)
    {
        memberMap.remove(nick);
        pushMemberList();
    }

    public Map<String, OnlineUser> getMemberMap()
    {
        return memberMap;
    }

    /**
     * 推送频道成员列表
     */
    public void pushMemberList()
    {
        Response.ResponsePOJO.Builder ret = Response.ResponsePOJO.newBuilder();

        ret.setType(Response.ResponsePOJO.Type.PUSH_CHA_MEMBER_LIST);

        ret.addChannelInfo(Response.ChannelInfo.newBuilder()
                .setName(this.name)
                .build());

        this.getMemberMap().values().forEach(member -> ret.addUserInfo(Response.UserInfo.newBuilder()
                .setUid(member.isLoginUser() ? member.getRegUser().getUid() : -1)
                .setMemberNick(member.getNick(this.name))
                .build()));

        Response.ResponsePOJO responsePOJO = ret.build();

        this.getMemberMap().values().forEach(member -> member.getCtx().writeAndFlush(responsePOJO));
    }
}
