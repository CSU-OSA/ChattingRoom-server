package cn.csuosa.chatroom.model;

import com.google.protobuf.ByteString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Message
{
    private long timestamp;
    private String channel;
    private String fromNick;
    private MessageType type;
    private ByteString content;

    public enum MessageType
    {
        PLAIN_TEXT,
        PICTURE,
        RICH_TEXT,
        HTML,
        FILE
    }
}
