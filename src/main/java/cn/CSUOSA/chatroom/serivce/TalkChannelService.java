package cn.csuosa.chatroom.serivce;

import cn.csuosa.chatroom.Out;
import cn.csuosa.chatroom.model.TalkChannel;
import cn.csuosa.chatroom.proto.Response;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TalkChannelService
{
    @Getter
    private final Map<String, TalkChannel> channelMap = new ConcurrentHashMap<>();           //频道表

    /**
     * 创建频道 - 创建指定名称和ticket(可选)的频道实例, 并将之添加到 频道名->频道实例 映射表中
     * <p>
     * （创建前请检查频道名是否已存在，否则频道有被覆盖的风险）
     *
     * @param name      频道名称
     * @param ticket    准入ticket, 如果为公共频道, 则置为空串("")
     * @param closeable 是否可自动关闭, 如果是, 则当频道内人数为零时频道会自动关闭
     */
    public void addCha(String name, String ticket, boolean closeable)
    {
        channelMap.put(name, new TalkChannel(name, ticket, closeable));
        Out.ConsoleOut.info("Channel [" + name + "] created. Closeable:" + closeable);
    }

    /**
     * 移除频道 - 从 频道名->频道实例 映射表中移除指定频道名陈的频道实例
     * <p>
     * （删除前请检查频道名是否存在）
     *
     * @param chaName 要移除的频道实例的频道名称
     */
    public void removeCha(String chaName)
    {
        channelMap.remove(chaName);
        Out.ConsoleOut.info("Channel [" + chaName + "] closed.");
    }

    /**
     * 检查准入ticket是否正确
     *
     * @param name   频道名称
     * @param ticket 用户输入的准入ticket
     * @return 结果代码
     */
    public boolean checkChannelTicket(String name, String ticket)
    {
        if (channelMap.containsKey(name))
        {
            return channelMap.get(name).checkTicket(ticket);
        }
        return false;
    }

    /**
     * 通过频道名获取频道实例
     *
     * @param chaName 频道名
     * @return 频道名
     */
    public TalkChannel getTalkChannel(String chaName)
    {
        return channelMap.get(chaName);
    }

    public ArrayList<Response.ChannelInfo.Builder> getChannelInfo()
    {
        ArrayList<Response.ChannelInfo.Builder> channelInfoBuilderList = new ArrayList<>();

        channelMap.forEach((channelName, talkChannel) ->
        {
            if (!channelName.matches("^<Pvt>\\d{6,10}\\.\\d{6,10}$"))
                channelInfoBuilderList.add(Response.ChannelInfo.newBuilder()
                        .setName(channelName)
                        .setIsPublic(talkChannel.checkTicket(""))
                        .setMemberNum(talkChannel.getMemberMap().size()));
        });

        return channelInfoBuilderList;
    }
}
