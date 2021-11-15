package cn.CSUOSA.ChattingRoomServer.ReturnParams;

public class BoolMsgWithObj
{
    boolean success;
    String msg;
    Object returnObj;

    public BoolMsgWithObj(boolean b, String msg)
    {
        success = b;
        this.msg = msg;
    }

    public Object getReturnObj()
    {
        return returnObj;
    }

    public boolean getSuccess()
    {
        return success;
    }

    public String getMsg()
    {
        return msg;
    }

    public BoolMsgWithObj setReturnObj(Object returnObj)
    {
        this.returnObj = returnObj;
        return this;
    }
}
