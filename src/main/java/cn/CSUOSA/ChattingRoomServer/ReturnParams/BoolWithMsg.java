package cn.CSUOSA.ChattingRoomServer.ReturnParams;

public class BoolWithMsg
{
    boolean success;
    String msg;

    public BoolWithMsg(boolean b, String msg)
    {
        success = b;
        this.msg = msg;
    }

    public boolean getSuccess()
    {
        return success;
    }

    public String getMsg()
    {
        return msg;
    }
}
