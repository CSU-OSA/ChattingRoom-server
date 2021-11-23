package cn.csuosa;

import cn.csuosa.model.Message;
import cn.csuosa.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Core {

    private static final Map<String, Map<String, User>> sources = new ConcurrentHashMap<>();

    public static Map<String, Map<String, User>> getSources() {
        return sources;
    }

    public static void addMessage(Message message) {
        Map<String, User> channel = sources.get(message.getChannel());
        for (Map.Entry<String, User> x : channel.entrySet()) {
            if (x.getKey().equals(message.getFromNick()))
                continue;
            x.getValue().receive(message);
        }
    }

    public static void removeChannel(String channel, String nick) {
        if (!sources.containsKey(channel) || !sources.get(channel).containsKey(nick))
            return;
        sources.get(channel).remove(nick);
    }
}
