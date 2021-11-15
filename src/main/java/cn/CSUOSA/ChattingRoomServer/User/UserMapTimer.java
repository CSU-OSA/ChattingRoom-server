package cn.CSUOSA.ChattingRoomServer.User;

import cn.CSUOSA.ChattingRoomServer.Channel.ChannelChecker;
import cn.CSUOSA.ChattingRoomServer.Main;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;

public class UserMapTimer implements Runnable
{
    private Boolean Enabled = true;

    public void setEnabled(Boolean enabled)
    {
        Enabled = enabled;
    }

    @Override
    public synchronized void run()
    {
        try
        {
            while (Enabled)
            {
                if (!Main.UserList.isEmpty())
                {
                    Main.UserList.forEach((key, value) -> {
                        UserInfo nickTime = Main.UserList.get(key);

                        nickTime.leftTime--;
                        if (nickTime.leftTime == 0)
                        {
                            Main.UserList.remove(key);
                            Out.Info("Nick [" + key + "] Released");
                            new Thread(new ChannelChecker()).start();
                        }
                    });
                }
                this.wait(1000);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
