package cn.csuosa.chatroom.handlers;

import cn.csuosa.chatroom.CoreResources;
import cn.csuosa.chatroom.Out;
import cn.csuosa.chatroom.common.SHA;
import cn.csuosa.chatroom.model.Message;
import cn.csuosa.chatroom.model.OnlineUser;
import cn.csuosa.chatroom.model.pojo.RegUser;
import cn.csuosa.chatroom.proto.Request;
import cn.csuosa.chatroom.proto.Response;
import cn.csuosa.chatroom.serivce.InfoPushService;
import cn.csuosa.chatroom.serivce.OnlineUserService;
import cn.csuosa.chatroom.serivce.RegUserService;
import cn.csuosa.chatroom.serivce.TalkChannelService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

/**
 * RequestHandler - 请求处理类
 * <p>
 * 接收来自客户端的请求，进行信息校验后交给业务逻辑
 */
public class RequestHandler extends ChannelInboundHandlerAdapter
{
    public final OnlineUserService onlineUserService = CoreResources.onlineUserService;
    public final RegUserService regUserService = CoreResources.regUserService;
    public final TalkChannelService talkChannelService = CoreResources.talkChannelService;
    private final InfoPushService infoPushService = CoreResources.infoPushService;

    /**
     * 构造Result型回复
     *
     * @param stCode 状态码
     * @param msg    返回信息
     * @return ResponsePOJO
     */
    private Response.ResponsePOJO buildResponse(int stCode, String msg)
    {
        Response.Result result = Response.Result.newBuilder()
                .setStCode(stCode)
                .setMsg(msg)
                .build();
        return Response.ResponsePOJO.newBuilder()
                .setType(Response.ResponsePOJO.Type.RESULT)
                .setResult(result)
                .build();
    }

    /**
     * 心跳包超时处理
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        if (evt instanceof IdleStateEvent)
        {//超时断开连接
            switch (((IdleStateEvent) evt).state())
            {
                case READER_IDLE, ALL_IDLE ->
                {
                    AttributeKey<OnlineUser> key = AttributeKey.valueOf("user");
                    OnlineUser onlineUser = ctx.channel().attr(key).get();
                    Out.ConsoleOut.info("Socket " + onlineUser.getUUID() + " is InActive");
                    ctx.channel().close();
                }
                case WRITER_IDLE ->
                {
                }
            }
        } else
        {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx)
    {
        AttributeKey<OnlineUser> key = AttributeKey.valueOf("user");
        OnlineUser onlineUser = onlineUserService.addNewOnlineUser(ctx);
        ctx.channel().attr(key).set(onlineUser);
        CoreResources.worker.execute(() -> {
            try
            {
                Thread.sleep(400);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            ctx.writeAndFlush(buildResponse(0, "CONNECT|Server-version:1.0.0-beta;API-edition:1.0.0-beta"));
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        AttributeKey<OnlineUser> key = AttributeKey.valueOf("user");
        OnlineUser onlineUser = ctx.channel().attr(key).get();
        onlineUserService.removeOnlineUser(onlineUser.getUUID());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception
    {
        Request.RequestPOJO request = (Request.RequestPOJO) msg;
        switch (request.getOperation())
        {
            case RETAIN ->
            {
            }
            case REGISTER ->
            {
                //检查参数完整性
                if (!request.hasUser() || !request.getUser().hasEmail())
                {
                    ctx.channel().writeAndFlush(buildResponse(200, "REGISTER|Missing user information."));
                    return;
                }

                //检查参数合法性
                if (userInfoLegalityCheck("REGISTER", request.getUser(), ctx)) return;

                //检查Email是否被占用
                if (regUserService.findUserByEmail(request.getUser().getEmail()) != null)
                {
                    ctx.channel().writeAndFlush(buildResponse(411, "REGISTER|Email has been taken."));
                    return;
                }

                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();

                //检查用户是否已登录（若已登录，想要切换用户则必须重新连接服务器）
                if (onlineUser.isLoginUser())
                {
                    ctx.channel().writeAndFlush(buildResponse(2, "REGISTER|You are already logged in."));
                    return;
                }

                regUserService.newRegisterUser(
                        request.getUser().getEmail(),
                        request.getUser().getPwd().toByteArray(),
                        request.getUser().getDefaultNick());

                long uid = regUserService.findUserByEmail(request.getUser().getEmail()).getUid();

                ctx.channel().writeAndFlush(buildResponse(0, "REGISTER|" + uid));
            }
            case LOGIN ->
            {
                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();

                if (!request.hasUser())
                {
                    ctx.channel().writeAndFlush(buildResponse(0, "LOGIN|Welcome.*.Nobody"));
                    infoPushService.pushChannelList(onlineUser);
                    return;
                }

                //检查参数完整性
                if (!request.getUser().hasEmail() && !request.getUser().hasUid())
                {
                    ctx.channel().writeAndFlush(buildResponse(200, "LOGIN|Missing user information."));
                    return;
                }

                //检查参数合法性
                if (userInfoLegalityCheck("LOGIN", request.getUser(), ctx)) return;

                //检查用户是否已登录（若已登录，想要切换用户则必须重新连接服务器）
                if (onlineUser.isLoginUser())
                {
                    ctx.channel().writeAndFlush(buildResponse(200, "LOGIN|You are already logged in."));
                    return;
                }

                RegUser regUserInst = null;
                if (request.getUser().hasEmail())
                    regUserInst = regUserService.findUserByEmail(request.getUser().getEmail());
                if (request.getUser().hasUid())
                    regUserInst = regUserService.findUserByUid(request.getUser().getUid());

                if (regUserInst == null
                        || !SHA.getSHA_256(SHA.byte2Hex(request.getUser().getPwd().toByteArray()) + regUserInst.getSalt())
                        .equals(regUserInst.getAuthentication_field()))
                {
                    ctx.channel().writeAndFlush(buildResponse(421, "LOGIN|Wrong UID/Email/Password."));
                    return;
                }

                //检查注册用户是否已登录（若已登录，则将把已登录的账号踢下线）
                if (onlineUserService.getOnlineUserByUID(regUserInst.getUid()) != null)
                {
                    onlineUserService.regUserLogout(onlineUserService.getOnlineUserByUID(regUserInst.getUid()).getUUID());
                    onlineUserService.getOnlineUserByUID(regUserInst.getUid()).getCtx()
                            .writeAndFlush(buildResponse(421, "SYSTEM|Your account is logged in elsewhere."));
                }

                onlineUserService.regUserLogin(regUserInst, onlineUser.getUUID());

                ctx.channel().writeAndFlush(buildResponse(0, "LOGIN|Welcome." + regUserInst.getUid() + "-\"" + regUserInst.getE_mail() + "\"-" + regUserInst.getDefault_nick()));

                infoPushService.pushChannelList(onlineUser);
            }
            case LOGOUT ->
            {//用户主动断开与服务器的连接
                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();
                Out.ConsoleOut.info("Socket {" + onlineUser.getUUID() + "} request to disconnect");
                ctx.channel().close();
            }
            case UPDATE_INFO ->
            {//用户更新信息
                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();

                if (!onlineUser.isLoginUser())
                {
                    ctx.channel().writeAndFlush(buildResponse(2, "UPDATE_INFO|You are not a registered user."));
                    return;
                }

                //检查参数完整性
                if (!request.hasUser())
                {
                    ctx.channel().writeAndFlush(buildResponse(101, "UPDATE_INFO|Missing user information."));
                    return;
                }

                //检查参数合法性
                if (userInfoLegalityCheck("UPDATE_INFO", request.getUser(), ctx)) return;

                if (request.getUser().hasPwdOld()
                        && !SHA.getSHA_256(SHA.byte2Hex(request.getUser().getPwd().toByteArray()) + onlineUser.getRegUser().getSalt())
                        .equals(onlineUser.getRegUser().getAuthentication_field()))
                {
                    ctx.channel().writeAndFlush(buildResponse(201, "UPDATE_INFO|Wrong Password."));
                    return;
                }

                ctx.channel().writeAndFlush(buildResponse(0, "UPDATE_INFO|Success."));
            }
            case JOIN_CHA ->
            {//用户加入频道
                //检查参数完整性
                if (!request.hasChannel() || !request.getChannel().hasNick())
                {
                    ctx.channel().writeAndFlush(buildResponse(101, "JOIN_CHA|Missing user information."));
                    return;
                }

                //检查参数合法性
                if (channelInfoLegalityCheck("SEND_MSG", request.getChannel(), ctx)) return;

                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();

                String chaName = request.getChannel().getName();
                String nick = request.getChannel().getNick();

                //检查频道是否存在, ticket是否正确.
                if (!talkChannelService.checkChannelTicket(chaName, request.getChannel().hasTicket() ? request.getChannel().getTicket() : ""))
                {
                    ctx.channel().writeAndFlush(buildResponse(301, "JOIN_CHA|Channel check failed."));
                    return;
                }
                //检查用户是否在频道中
                if (onlineUser.isInChannel(chaName))
                {
                    ctx.channel().writeAndFlush(buildResponse(2, "JOIN_CHA|You are already in this channel."));
                    return;
                }

                //检查nick是否被占用
                if (talkChannelService.getTalkChannel(chaName).getMemberMap().containsKey(nick))
                {
                    if (talkChannelService.getTalkChannel(chaName).getMemberMap().get(nick).isLoginUser())
                    {
                        ctx.channel().writeAndFlush(buildResponse(202, "JOIN_CHA|Unavailable nick."));
                        return;
                    } else
                    {
                        talkChannelService.getTalkChannel(chaName).getMemberMap().get(nick).getCtx().channel()
                                .writeAndFlush(buildResponse(303, "SYSTEM|You are kicked out."));
                        talkChannelService.getTalkChannel(chaName).getMemberMap().get(nick).quit(chaName);
                        talkChannelService.getTalkChannel(chaName).getMemberMap().remove(nick);
                    }
                }

                onlineUser.join(chaName, nick);
                talkChannelService.getTalkChannel(chaName).addMember(nick, onlineUser);

                ctx.writeAndFlush(buildResponse(0, "JOIN_CHA|Success."));
            }
            case QUIT_CHA ->
            {//用户退出频道
                AttributeKey<OnlineUser> key = AttributeKey.valueOf("user");
                OnlineUser onlineUser = ctx.channel().attr(key).get();

                String chaName = request.getChannel().getName();
                //检查用户是否在频道中
                if (!onlineUser.isInChannel(chaName))
                {
                    ctx.channel().writeAndFlush(buildResponse(200, "QUIT_CHA|You are not in this channel."));
                    return;
                }

                CoreResources.userQuitCha(onlineUser, chaName);

                ctx.writeAndFlush(buildResponse(0, "QUIT_CHA|Success."));
            }
            case CREATE_CHA ->
            {//用户创建频道
                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();

                //不是登录用户，不能创建频道
                if (!onlineUser.isLoginUser())
                {
                    ctx.channel().writeAndFlush(buildResponse(2, "CREATE_CHA|You are not a registered user."));
                    return;
                }

                String chaName = request.getChannel().getName();
                String nick = request.getChannel().getNick();

                //检查两个保留字 检查频道是否已存在
                if (chaName.equals("System")
                        || chaName.equals("Default"))
                {
                    ctx.writeAndFlush(buildResponse(202, "CREATE_CHA|Unavailable channel name."));
                    return;
                }

                if (talkChannelService.getTalkChannel(chaName) != null)
                {
                    ctx.writeAndFlush(buildResponse(202, "CREATE_CHA|Unavailable channel name."));
                    return;
                }

                if (chaName.matches("^<Pvt>\\d{6,10}\\.\\d{6,10}$"))
                {
                    String[] uid_str = chaName.substring(4).split("\\.");
                    OnlineUser onlineUser2 = onlineUserService.getOnlineUserByUID(Long.parseLong(uid_str[1]));
                    if (onlineUser.getRegUser().getUid() != Integer.parseInt(uid_str[0]))
                    {
                        ctx.writeAndFlush(buildResponse(202, "CREATE_CHA|You are not allowed to create this pvt channel."));
                        return;
                    }
                    if (onlineUser2 == null)
                    {
                        ctx.writeAndFlush(buildResponse(202, "CREATE_CHA|User does not exist."));
                        return;
                    }

                    talkChannelService.addCha(chaName,
                            request.getChannel().hasTicket() ? request.getChannel().getTicket() : "*",
                            true);

                    String nick2 = onlineUser2.getRegUser().getDefault_nick() == null ?
                            String.valueOf(onlineUser2.getRegUser().getUid()) : onlineUser2.getRegUser().getDefault_nick();

                    CoreResources.userJoinCha(onlineUser, nick, chaName);
                    CoreResources.userJoinCha(onlineUser2, nick2, chaName);

                    ctx.writeAndFlush(buildResponse(0, "CREATE_CHA|Success."));
                    return;
                }

                talkChannelService.addCha(chaName,
                        request.getChannel().hasTicket() ? request.getChannel().getTicket() : "",
                        true);

                CoreResources.userJoinCha(onlineUser, nick, chaName);

                ctx.writeAndFlush(buildResponse(0, "CREATE_CHA|Success."));
            }
            case SEND_MSG ->
            {
                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();

                //不是登录用户，不能发送消息
                if (!onlineUser.isLoginUser())
                {
                    ctx.channel().writeAndFlush(buildResponse(200, "SEND_MSG|You are not a registered user."));
                    return;
                }

                //检查参数合法性
                if (channelInfoLegalityCheck("SEND_MSG", request.getChannel(), ctx)) return;

                String channel = request.getChannel().getName();
                if (!onlineUser.isInChannel(channel))
                {
                    ctx.channel().writeAndFlush(buildResponse(200, "SEND_MSG|You are not in this channel."));
                    return;
                }

                CoreResources.addMessage(new Message(
                        request.getMessage().getTimestamp(),
                        request.getChannel().getName(),
                        request.getChannel().getNick(),
                        Message.MessageType.values()[request.getMessage().getType()],
                        request.getMessage().getContent()
                ));
                ctx.writeAndFlush(buildResponse(0, "SEND_MSG|copy."));
            }
            case HEARTBEAT ->
            {
            }
            case UNRECOGNIZED ->
            {
                ctx.writeAndFlush(buildResponse(100, "Unsupported method."));
            }
            default -> ctx.writeAndFlush(buildResponse(100, "Unsupported method."));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        if (cause instanceof RequestError)
        {
            ctx.writeAndFlush(buildResponse(((RequestError) cause).StCode, cause.getMessage()));
        } else
            ctx.writeAndFlush(buildResponse(1, cause.getMessage()));

        Out.ConsoleOut.err(cause.toString());
        // TODO: RuntimeError should close the socket
    }

    /**
     * 检查请求中的用户信息合法性
     *
     * @param requestType      请求类型（将被附在异常中返回给客户端）
     * @param userInfo_request 请求内容实例
     * @param ctx              用户Socket上下文
     * @return true表示存在问题
     */
    private boolean userInfoLegalityCheck(String requestType, Request.User userInfo_request, ChannelHandlerContext ctx)
    {
        final long MIN_UID = 100000;    //最小UID
        final long MAX_UID = 999999;    //最大UID
        //检查uid合法性
        if (userInfo_request.hasUid()
                && (userInfo_request.getUid() < MIN_UID || userInfo_request.getUid() > MAX_UID))
        {
            ctx.channel().writeAndFlush(buildResponse(201, requestType + "|Illegal UID."));
            return true;
        }
        //检查Email合法性
        if (userInfo_request.hasEmail()
                && !userInfo_request.getEmail().matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$"))
        {
            ctx.channel().writeAndFlush(buildResponse(201, requestType + "|Illegal Email address."));
            return true;
        }
        //检查密码长度
        if (userInfo_request.getPwd().size() < 6)
        {
            ctx.channel().writeAndFlush(buildResponse(201, requestType + "|Insufficient password length."));
            return true;
        }
        //检查默认昵称的合法性
        if (userInfo_request.hasDefaultNick() && !userInfo_request.getDefaultNick().matches("^[A-Za-z]\\w{3,63}$"))
        {
            ctx.channel().writeAndFlush(buildResponse(200, requestType + "|Illegal defaultNick."));
            return true;
        }


        return false;
    }

    /**
     * 检查请求中的频道信息合法性
     *
     * @param requestType     请求类型（将被附在异常中返回给客户端）
     * @param channel_request 请求内容实例
     * @param ctx             用户Socket上下文
     * @return true表示存在问题
     */
    private boolean channelInfoLegalityCheck(String requestType, Request.Channel channel_request, ChannelHandlerContext ctx)
    {
        //检查nick合法性
        if (channel_request.hasNick()
                && !channel_request.getNick().matches("^[A-Za-z]\\w{3,63}$"))
        {
            ctx.channel().writeAndFlush(buildResponse(200, requestType + "|Illegal nick."));
            return true;
        }
        //检查ticket合法性
        if (channel_request.hasTicket()
                && !channel_request.getTicket().matches("^[A-Za-z\\d]{4,63}$"))
        {
            ctx.channel().writeAndFlush(buildResponse(201, requestType + "|Illegal ticket."));
            return true;
        }
        //检查频道名
        if (!channel_request.getName().matches("^[\\u4E00-\\u9FA5A-Za-z\\d_ ]{1,32}$")
                && !channel_request.getName().matches("^<Pvt>\\d{6,10}\\.\\d{6,10}$"))
        {
            ctx.channel().writeAndFlush(buildResponse(201, requestType + "|Illegal channel name."));
            return true;
        }

        return false;
    }

    private static final class RequestError extends Exception
    {
        int StCode;

        RequestError(int stCode, String msg)
        {
            super(msg);
            this.StCode = stCode;
        }
    }
}
