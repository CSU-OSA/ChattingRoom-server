package cn.csuosa.chatroom;

import cn.csuosa.chatroom.model.Channel;
import cn.csuosa.chatroom.model.Message;
import cn.csuosa.chatroom.model.OnlineUser;
import cn.csuosa.chatroom.serivce.InfoPushService;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 核心类
 * <p>
 * 包含三个核心实例：
 * <p>
 * onlineUserMap - uuid->在线用户实例 映射表
 * <p>
 * chaMap  - 频道名->频道实例 映射表
 */
public class Core
{
    private static final Map<String, OnlineUser> onlineUserMap = new ConcurrentHashMap<>();
    public static InfoPushService infoPushService = new InfoPushService();
    private static final Map<String, Channel> chaMap = new ConcurrentHashMap<>();

    /**
     * 获取 uuid->在线用户实例 映射表
     *
     * @return 返回onlineUserMap
     */
    public static Map<String, OnlineUser> getOnlineUserMap()
    {
        return onlineUserMap;
    }

    /**
     * 获取 频道名->频道实例 映射表
     *
     * @return 返回chaMap
     */
    public static Map<String, Channel> getChaMap()
    {
        return chaMap;
    }

    /**
     * 新建在线用户 - 新建在线用户实例, 并将之添加到 uuid->在线用户实例 映射表中
     *
     * @return 返回新建的在线用户实例
     */
    public static OnlineUser addNewOnlineUser(ChannelHandlerContext ctx)
    {
        OnlineUser newOnlineUser = new OnlineUser(ctx);
        onlineUserMap.put(newOnlineUser.getUUID(), newOnlineUser);
        Out.ConsoleOut.info("Socket {" + newOnlineUser.getUUID() + "} connected successfully");
        return newOnlineUser;
    }

    /**
     * 移除用户 - 从 uuid->用户实例 映射表中移除指定uuid的用户实例
     * <p>
     * (注意：不会从频道实例的 nick->用户实例 映射表中移除)
     *
     * @param uuid 要删除的用户实例的uuid
     */
    public static void removeOnlineUser(String uuid)
    {
        onlineUserMap.remove(uuid);
        Out.ConsoleOut.info("Socket {" + uuid + "} disconnected successfully");
    }

    /**
     * 创建频道 - 创建指定名称和ticket(可选)的频道实例, 并将之添加到 频道名->频道实例 映射表中
     *
     * @param name      频道名称
     * @param ticket    准入ticket, 如果为公共频道, 则置为空串("")
     * @param closeable 是否可自动关闭, 如果是, 则当频道内人数为零时频道会自动关闭
     * @return 执行结果
     */
    public static int addCha(String name, String ticket, boolean closeable)
    {
        if (!chaMap.containsKey(name))
        {
            chaMap.put(name, new Channel(name, ticket, closeable));
            Out.ConsoleOut.info("Channel [" + name + "] created. Closeable:" + closeable);
            return 0;
        }
        return -1;
    }

    /**
     * 移除频道 - 从 频道名->频道实例 映射表中移除指定频道名陈的频道实例
     *
     * @param chaName 要移除的频道实例的频道名称
     * @return 结果代码
     */
    public static boolean removeCha(String chaName)
    {
        if (chaMap.containsKey(chaName))
        {
            chaMap.remove(chaName);
            Out.ConsoleOut.info("Channel [" + chaName + "] closed.");
            return true;
        }
        return false;
    }

    /**
     * 检查准入ticket是否正确
     *
     * @param name   频道名称
     * @param ticket 用户输入的准入ticket
     * @return 结果代码
     */
    public static int checkChannelTicket(String name, String ticket)
    {
        if (chaMap.containsKey(name))
        {
            if (!chaMap.get(name).checkTicket(ticket))
                return -1;
            return 0;
        }
        return -1;
    }

    /**
     * 向频道中发送消息
     *
     * @param message 消息实例
     */
    public static void addMessage(Message message)
    {
        chaMap.get(message.getChannel()).getMemberMap()
                .forEach((nick, onlineUser) -> {
                    if (!nick.equals(message.getFromNick()))
                        onlineUser.receive(message);
                });
    }
}
