package cn.csuosa.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private String channel;
    private String fromNick;
    private String content;

    public Message(String channel, String fromNick, String content) {
        this.channel = channel;
        this.fromNick = fromNick;
        this.content = content;
    }
}
