package cn.CSUOSA.ChattingRoomServer.Message;

import cn.CSUOSA.ChattingRoomServer.Main;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;

public class MsgListCleaner implements Runnable
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
        Out.Warn("MsgListCleaner thread started.");
        try
        {
            while (keepRunning)
            {
                if (!Main.MsgList.isEmpty())
                {
                    Main.MsgList.forEach((key, value) -> {
                        MessageListEntry msgEntry = Main.MsgList.get(key);

                        if (msgEntry.getQuoteCount() == 0)
                        {
                            Main.MsgList.remove(key);
                        }
                    });
                }
                wait(60 * 1000);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        Out.Warn("MsgListCleaner thread terminated.");
    }
}
