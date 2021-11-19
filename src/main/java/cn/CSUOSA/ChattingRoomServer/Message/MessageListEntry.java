package cn.CSUOSA.ChattingRoomServer.Message;

public class MessageListEntry
{
    public final MessageInfo messageInfo;
    private int quoteCount;

    public MessageListEntry(MessageInfo messageInfo)
    {
        this.messageInfo = messageInfo;
        quoteCount = 0;
    }

    public synchronized void addQuoteCount()
    {
        quoteCount++;
    }

    public synchronized void minusQuoteCount()
    {
        quoteCount--;
    }

    public int getQuoteCount()
    {
        return quoteCount;
    }
}
