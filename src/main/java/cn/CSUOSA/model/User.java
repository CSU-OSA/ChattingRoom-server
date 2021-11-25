package cn.csuosa.model;

import cn.csuosa.Core;
import com.fasterxml.uuid.Generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private final String uuid;

    private final Map<String, String> nickNames = new HashMap<>();
    private final List<Message> messageList = new ArrayList<>();

    public User() {
        uuid = Generators.timeBasedGenerator().generate().toString();
    }
    public void join(String channel, String nick) throws Exception {
        Map<String, Map<String, User>> sources = Core.getSources();
        if (!sources.containsKey(channel))
            sources.put(channel, new ConcurrentHashMap<>());
        Map<String, User> set = sources.get(channel);
        if (isInChannel(channel))
            throw new Exception("You have already in this channel");
        if (set.containsKey(nick))
            throw new Exception("Nick is used");
        sources.get(channel).put(nick, this);
        nickNames.put(channel, nick);
    }

    public void quit(String channel) {
        Core.removeChannel(channel, nickNames.get(channel));
        nickNames.remove(channel);
    }

    public void logout() {
        Map<String, Map<String, User>> sources = Core.getSources();
        for (Map.Entry<String, Map<String, User>> x : sources.entrySet()) {
            if (isInChannel(x.getKey()))
                x.getValue().remove(nickNames.get(x.getKey()));
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void send(Message message) {
        Core.addMessage(message);
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
