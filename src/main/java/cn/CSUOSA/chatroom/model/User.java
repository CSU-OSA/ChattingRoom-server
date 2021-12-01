package cn.csuosa.chatroom.model;

import com.fasterxml.uuid.Generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User
{
    private final String uuid;  //用户的唯一身份标识符

    private final Map<String, String> nickNames = new HashMap<>();  //用户的频道与昵称映射
    private final List<Message> messageList = new ArrayList<>();    //用户的消息队列

    //创建用户
    public User()
    {
        uuid = Generators.timeBasedGenerator().generate().toString();
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
    {//退出<channel>频道
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
     * 收取消息
     *
     * @param msg 消息实例
     */
    public void receive(Message msg)
    {
        messageList.add(msg);
    }

    /**
     * 获取消息列表
     *
     * @return 消息列表
     */
    public List<Message> getMessageList()
    {
        List<Message> ret = new ArrayList<>(messageList);
        messageList.clear();
        return ret;
    }

    @Override
    public String toString()
    {
        return super.toString().split("@")[1];
    }
}
