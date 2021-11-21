package cn.CSUOSA.ChattingRoomServer.User;

import cn.CSUOSA.ChattingRoomServer.Main;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;

public class UserMapCleaner implements Runnable
{
    private boolean keepRunning = true;

    public void stopRunning()
    {
        keepRunning = false;
        synchronized (this) {notify();}
    }

    @Override
    public synchronized void run()
    {
        Out.Warn("UserMapCleaner thread started.");
        try
        {
            while (keepRunning)
            {
                if (!Main.UserList.isEmpty())
                {
                    Main.UserList.forEach((usrNick, usrInfo) -> {

                        usrInfo.leftTime--;
                        if (usrInfo.leftTime == 0)
                        {
                            Main.UserList.remove(usrNick);
                            Out.Info("Nick [" + usrNick + "] Released");
                            Main.mainController.runChannelCleaner();
                        }
                    });
                }
                wait(1000);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        Out.Warn("UserMapCleaner thread terminated.");
    }
}
