package cn.CSUOSA.ChattingRoomServer.Message;

import cn.CSUOSA.ChattingRoomServer.Main;

public class MessageListMaintain implements Runnable
{
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                synchronized (this)
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
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
