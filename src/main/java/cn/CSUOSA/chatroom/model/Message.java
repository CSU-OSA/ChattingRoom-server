package cn.csuosa.chatroom.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message
{
    private String recTime;
    private String channel;
    private String fromNick;
    private String content;

    public Message(String recTime, String channel, String fromNick, String content)
    {
        this.recTime = recTime;
        this.channel = channel;
        this.fromNick = fromNick;
        this.content = content;
    }
}
