package cn.csuosa;

import cn.csuosa.model.Message;
import cn.csuosa.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Core {

    private static final Map<String, ConcurrentHashMap<String, User>> sources = new ConcurrentHashMap<>();
    private static final Map<String, User> online = new ConcurrentHashMap<>();

    public static boolean login(User user) {
        if (online.containsKey(user.getUuid()))
            return false;
        online.put(user.getUuid(), user);
        return true;
    }

    public static boolean logout(String uuid) {
        if (!online.containsKey(uuid))
            return false;
        online.remove(uuid);
        // TODO: 需要在消息维护结构中移除所有的user对象
        return true;
    }

    public static void send(Message message) {
        ConcurrentHashMap<String, User> channel = sources.get(message.getChannel());
        for (Map.Entry<String, User> x : channel.entrySet()) {
            if (x.getKey().equals(message.getFromNick()))
                continue;
            x.getValue().receive(message);
        }
    }

    private static void createChannel(String channel) {
        if (sources.containsKey(channel))
            return;
        sources.put(channel, new ConcurrentHashMap<>());
    }

    public static void joinChannel(String channel, String nick, User user) throws Exception {
        if (!sources.containsKey(channel))
            createChannel(channel);
        Map<String, User> set = sources.get(channel);
        if (user.isInChannel(channel))
            throw new Exception("You have already in this channel");
        if (set.containsKey(nick))
            throw new Exception("Nick is used");
        sources.get(channel).put(nick, user);
    }

    public static void quitChannel(String channel, String nick) {
        if (!sources.containsKey(channel) || !sources.get(channel).containsKey(nick))
            return;
        sources.get(channel).remove(nick);
    }

    public static User getUser(String uuid) throws Exception {
        if (!online.containsKey(uuid))
            throw new Exception("User is not online");
        return online.get(uuid);
    }
}
