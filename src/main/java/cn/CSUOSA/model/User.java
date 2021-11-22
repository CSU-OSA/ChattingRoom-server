package cn.csuosa.model;

import cn.csuosa.Core;
import com.fasterxml.uuid.Generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private final String uuid;

    private final Map<String, String> nickNames = new HashMap<>();
    private final List<Message> messageList = new ArrayList<>();

    public User() {
        uuid = Generators.timeBasedGenerator().generate().toString();
    }

    public String getUuid() {
        return uuid;
    }

    public void join(String channel, String nick) throws Exception {
        Core.joinChannel(channel, nick, this);
        nickNames.put(channel, nick);
    }

    public void quit(String channel) {
        Core.quitChannel(channel, nickNames.get(channel));
        nickNames.remove(channel);
    }

    public boolean login() {
        return Core.login(this);
    }

    public boolean logout() {
        return Core.logout(uuid);
    }

    public void send(Message message) {
        Core.send(message);
    }

    public boolean isInChannel(String channel) {
        return nickNames.containsKey(channel);
    }

    public void receive(Message msg) {
        messageList.add(msg);
    }

    public Message[] getMessageList() {
        Message[] ret = messageList.toArray(new Message[0]);
        messageList.clear();
        return ret;
    }

    public String getNick(String channel) {
        if (!nickNames.containsKey(channel))
            return "";
        return nickNames.get(channel);
    }
}
