package cn.csuosa.chatroom.model;

import cn.csuosa.chatroom.model.pojo.RegUser;
import cn.csuosa.chatroom.proto.Response;
import com.fasterxml.uuid.Generators;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class OnlineUser
{
    /**
     * 该在线用户（链接）的唯一标识符
     */
    private final String uuid;
    /**
     * Socket链接实例
     */
    private final ChannelHandlerContext ctx;
    /**
     * 用户的频道与昵称映射
     */
    private final Map<String, String> nickNames = new HashMap<>();
    /**
     * 用户实例
     * （作为注册用户登录后生成）
     */
    private RegUser regUser = null;

    /**
     * 创建在线用户实例
     *
     * @param ctx
     */
    public OnlineUser(ChannelHandlerContext ctx)
    {
        uuid = Generators.timeBasedGenerator().generate().toString();
        this.ctx = ctx;
    }

    /**
     * 获取uuid
     *
     * @return uuid
     */
    public String getUUID()
    {
        return uuid;
    }

    /**
     * 加入频道
     *
     * @param channel 频道名称
     * @param nick    加入频道时使用的nick
     */
    public void join(String channel, String nick)
    {
        nickNames.put(channel, nick);
    }

    /**
     * 退出频道
     *
     * @param chaName 要退出的频道的频道名
     */
    public void quit(String chaName)
    {
        nickNames.remove(chaName);
    }

    /**
     * 获取 频道名->昵称 映射表
     *
     * @return 频道名->昵称 映射表
     */
    public Map<String, String> getNickNames()
    {
        return nickNames;
    }

    /**
     * 检查是否在频道内
     *
     * @param chaName 要检查的频道的频道名
     */
    public boolean isInChannel(String chaName)
    {
        return nickNames.containsKey(chaName);
    }

    /**
     * 获取与频道对应的nick
     *
     * @param chaName 要查询的频道的频道名
     * @return 对应的nick
     */
    public String getNick(String chaName)
    {
        if (!nickNames.containsKey(chaName))
            return "";
        return nickNames.get(chaName);
    }

    /**
     * 收取消息，将其推送至客户端
     *
     * @param msg 消息实例
     */
    public void receive(Message msg)
    {
        getCtx().writeAndFlush(Response.ResponsePOJO.newBuilder()
                .setType(Response.ResponsePOJO.Type.PUSH_MSG)
                .setMessage(Response.Message.newBuilder()
                        .setType(msg.getType().ordinal())
                        .setChannel(msg.getChannel())
                        .setFromNick(msg.getFromNick())
                        .setContent(msg.getContent())
                        .setTimestamp(msg.getTimestamp())));
    }

    /**
     * 注册用户登录，将 用户实例 绑定到 在线用户实例
     *
     * @param regUser 用户实例
     */
    public void login(RegUser regUser)
    {
        this.regUser = regUser;
    }

    /**
     * 注册用户登出，将 用户实例 与 在线用户实例 解绑
     */
    public void logout()
    {
        this.regUser = null;
    }

    /**
     * 是否为登录的注册用户
     */
    public boolean isLoginUser()
    {
        return (this.regUser != null);
    }

    @Override
    public String toString()
    {
        return super.toString().split("@")[1];
    }
}
