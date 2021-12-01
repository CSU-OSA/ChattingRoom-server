package cn.csuosa.chatroom;

import cn.csuosa.chatroom.model.Channel;
import cn.csuosa.chatroom.model.Message;
import cn.csuosa.chatroom.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 核心类
 * <p>
 * 包含两个核心实例：
 * <p>
 * userMap - uuid->用户实例 映射表
 * <p>
 * chaMap  - 频道名->频道实例 映射表
 */
public class Core
{
    private static final Map<String, User> userMap = new ConcurrentHashMap<>();
    private static final Map<String, Channel> chaMap = new ConcurrentHashMap<>();

    /**
     * 获取 uuid->用户实例 映射表
     *
     * @return 返回userMap
     */
    public static Map<String, User> getUserMap()
    {
        return userMap;
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
     * 新建用户 - 新建用户实例, 并将之添加到 uuid->用户实例 映射表中
     *
     * @return 返回新建的用户实例
     */
    public static User addNewUser()
    {
        User newUser = new User();
        userMap.put(newUser.getUUID(), newUser);
        Out.ConsoleOut.info("User " + newUser.getUUID() + " login successfully");

        return newUser;
    }

    /**
     * 移除用户 - 从 uuid->用户实例 映射表中移除指定uuid的用户实例 (不会从频道实例的 nick->用户实例 映射表中移除)
     *
     * @param uuid 要删除的用户实例的uuid
     */
    public static void removeUser(String uuid)
    {
        userMap.remove(uuid);
        Out.ConsoleOut.info("User " + uuid + " logout successfully");
    }

    /**
     * 创建频道 - 创建指定名称和ticket(可选)的频道实例, 并将之添加到 频道名->频道实例 映射表中
     *
     * @param name   频道名称
     * @param ticket 准入ticket, 如果为公共频道, 则置为空串("")
     * @return 返回新建的频道实例
     */
    public static int addCha(String name, String ticket)
    {
        if (!chaMap.containsKey(name))
        {
            chaMap.put(name, new Channel(name, ticket));
            Out.ConsoleOut.info("Channel [" + name + "] created.");
            return 0;
        }
        return -1;
    }

    /**
     * 移除频道 - 从 频道名->频道实例 映射表中移除指定频道名陈的频道实例
     *
     * @param name 要移除的频道实例的频道名称
     * @return 结果代码
     */
    public static int removeCha(String name)
    {
        if (chaMap.containsKey(name))
        {
            chaMap.remove(name);
            Out.ConsoleOut.info("Channel [" + name + "] closed.");
            return 0;
        }
        return -1;
    }

    /**
     * 检查准入ticket是否正确
     *
     * @param name   频道名称
     * @param ticket 用户输入的准入ticket
     * @return 结果代码
     */
    public static int chkCha(String name, String ticket)
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
     * 向频道中添加消息 -
     *
     * @param message 消息实例
     */
    public static void addMessage(Message message)
    {
        chaMap.get(message.getChannel()).getMemberMap().forEach((nick, user) -> {
            if (!nick.equals(message.getFromNick()))
                user.receive(message);
        });
    }
}
