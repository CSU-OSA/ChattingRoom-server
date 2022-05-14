package cn.csuosa.chatroom.serivce;

import cn.csuosa.chatroom.CoreResources;
import cn.csuosa.chatroom.Out;
import cn.csuosa.chatroom.model.OnlineUser;
import cn.csuosa.chatroom.model.pojo.RegUser;
import cn.csuosa.chatroom.proto.Response;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线用户服务（单例）
 * <p>
 * 维护 uuid -> OnlineUser 映射表
 */
public class OnlineUserService
{
    @Getter
    private final Map<String, OnlineUser> onlineUserMap = new ConcurrentHashMap<>(); //连接至服务器的用户表
    @Getter
    private final Map<Long, OnlineUser> onlineRegUserMap = new ConcurrentHashMap<>();    //登陆在线的注册用户

    /**
     * 新建在线用户 - 新建在线用户实例, 并将之添加到 uuid->在线用户实例 映射表中
     *
     * @return 返回新建的在线用户实例
     */
    public OnlineUser addNewOnlineUser(ChannelHandlerContext ctx)
    {
        OnlineUser newOnlineUser = new OnlineUser(ctx);
        onlineUserMap.put(newOnlineUser.getUUID(), newOnlineUser);
        Out.ConsoleOut.info("Socket {" + newOnlineUser.getUUID() + "} connected");
        return newOnlineUser;
    }

    /**
     * 移除在线用户 - 从 uuid->用户实例 映射表中移除指定uuid的用户实例
     * <p>
     * (注意：不会从频道实例的 nick->用户实例 映射表中移除)
     *
     * @param uuid 要删除的用户实例的uuid
     */
    public void removeOnlineUser(String uuid)
    {
        onlineUserMap.get(uuid).getNickNames().forEach((chaName, nick) ->
                CoreResources.talkChannelService.getTalkChannel(chaName).removeMember(nick));
        if (onlineUserMap.get(uuid).isLoginUser())  //如果是登录的注册用户，还要退出登录
            regUserLogout(uuid);
        onlineUserMap.remove(uuid);
        Out.ConsoleOut.info("Socket {" + uuid + "} disconnected");
    }

    /**
     * 注册用户登录 - 将在线用户与注册用户绑定
     *
     * @param regUser 注册用户实例
     * @param uuid    在线用户uuid
     */
    public void regUserLogin(RegUser regUser, String uuid)
    {
        OnlineUser onlineUser = onlineUserMap.get(uuid);
        onlineUser.login(regUser);
        onlineRegUserMap.put(regUser.getUid(), onlineUser);
        Out.ConsoleOut.info("Socket {" + uuid + "} login as {" + regUser.getUid() + "}");
        pushOnlineRegUser();
    }

    /**
     * 注册用户登出 - 将在线用户与注册用户解绑（并删除注册用户的映射信息）
     *
     * @param uuid 在线用户uuid
     */
    public void regUserLogout(String uuid)
    {
        OnlineUser onlineUser = onlineUserMap.get(uuid);
        long uid = onlineUser.getRegUser().getUid();
        onlineUser.logout();
        onlineRegUserMap.remove(uid);
        Out.ConsoleOut.info("RegUser {" + uid + "} logout, socket {" + uuid + "}");
        pushOnlineRegUser();
    }

    /**
     * 通过uuid获取在线用户的实例
     *
     * @param uuid 在线用户的uuid
     * @return 在线用户实例
     */
    public OnlineUser getOnlineUserByUUID(String uuid)
    {
        return onlineUserMap.get(uuid);
    }

    /**
     * 通过UID获取在线注册用户的实例
     *
     * @param uid 注册用户的uid
     * @return 在线用户实例
     */
    public OnlineUser getOnlineUserByUID(Long uid)
    {
        return onlineRegUserMap.get(uid);
    }

    /**
     * 推送登录用户列表（不包含匿名登陆用户）
     */
    public void pushOnlineRegUser()
    {
        Response.ResponsePOJO.Builder ret = Response.ResponsePOJO.newBuilder();

        ret.setType(Response.ResponsePOJO.Type.PUSH_LOGIN_USER_LIST);

        onlineRegUserMap.forEach((s, onlineUser) -> ret.addUserInfo(Response.UserInfo.newBuilder()
                .setUid(onlineUser.getRegUser().getUid())
                .setMemberNick(onlineUser.getRegUser().getDefault_nick())
                .build()));

        Response.ResponsePOJO responsePOJO = ret.build();

        onlineRegUserMap.forEach((s, onlineUser) -> onlineUser.getCtx().writeAndFlush(responsePOJO));
    }
}
