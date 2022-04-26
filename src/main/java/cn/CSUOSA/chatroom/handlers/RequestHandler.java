package cn.csuosa.chatroom.handlers;

import cn.csuosa.chatroom.Core;
import cn.csuosa.chatroom.Out;
import cn.csuosa.chatroom.common.RandGenerator;
import cn.csuosa.chatroom.common.SHA;
import cn.csuosa.chatroom.mapper.UserMapper;
import cn.csuosa.chatroom.model.Message;
import cn.csuosa.chatroom.model.OnlineUser;
import cn.csuosa.chatroom.model.pojo.User;
import cn.csuosa.chatroom.proto.Request;
import cn.csuosa.chatroom.proto.Response;
import cn.csuosa.chatroom.serivce.InfoPushService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

public class RequestHandler extends ChannelInboundHandlerAdapter
{
    private final UserMapper userMapper = new UserMapper();
    private final InfoPushService infoPushService = Core.infoPushService;
    private final NioEventLoopGroup worker = new NioEventLoopGroup();

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
                    Out.ConsoleOut.info("User " + onlineUser.getUUID() + " is InActive");
                    ctx.channel().close();
                }
                case WRITER_IDLE ->
                {
                }//infoPushService.pushChannelList(ctx);
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
        OnlineUser onlineUser = Core.addNewOnlineUser(ctx);
        ctx.channel().attr(key).set(onlineUser);
        worker.execute(() -> {
            try
            {
                Thread.sleep(400);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            ctx.writeAndFlush(buildResponse(0, "VERSION|Server-version:1.0.0-beta;API-edition:1.0.0-beta"));
            infoPushService.pushChannelList();
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        AttributeKey<OnlineUser> key = AttributeKey.valueOf("user");
        OnlineUser onlineUser = ctx.channel().attr(key).get();
        onlineUser.getNickNames().forEach((chaName, nick) -> Core.getChaMap().get(chaName).removeMember(nick));
        Core.removeOnlineUser(onlineUser.getUUID());
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
                    ctx.channel().writeAndFlush(buildResponse(101, "REGISTER|Missing user information."));
                    return;
                }

                //检查参数合法性
                if (userInfoLegalityCheck("REGISTER", request.getUser(), ctx)) return;

                //检查Email是否被占用
                if (userMapper.findUserByEmail(request.getUser().getEmail()) != null)
                {
                    ctx.channel().writeAndFlush(buildResponse(101, "REGISTER|Email has been taken."));
                    return;
                }

                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();

                //检查用户是否已登录（若已登录，想要切换用户则必须重新连接服务器）
                if (onlineUser.isLoginUser())
                {
                    ctx.channel().writeAndFlush(buildResponse(2, "REGISTER|You are already logged in."));
                    return;
                }

                String salt = RandGenerator.randSalt(); //生成随机Salt

                User newUserInst = User.builder()
                        .e_mail(request.getUser().getEmail())
                        .password(SHA.getSHA_256(SHA.byte2Hex(request.getUser().getPwd().toByteArray()) + salt))
                        .salt(salt)
                        .default_nick(request.getUser().hasDefaultNick() ? request.getUser().getDefaultNick() : null)
                        .build();

                userMapper.newUser(newUserInst);
                onlineUser.setUser(newUserInst);

                ctx.channel().writeAndFlush(buildResponse(0, "REGISTER|" + newUserInst.getUid()));
            }
            case LOGIN ->
            {
                //检查参数完整性
                if (!request.hasUser() || (!request.getUser().hasEmail() && !request.getUser().hasUid()))
                {
                    ctx.channel().writeAndFlush(buildResponse(101, "LOGIN|Missing user information."));
                    return;
                }

                //检查参数合法性
                if (userInfoLegalityCheck("LOGIN", request.getUser(), ctx)) return;

                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();

                //检查用户是否已登录（若已登录，想要切换用户则必须重新连接服务器）
                if (onlineUser.isLoginUser())
                {
                    ctx.channel().writeAndFlush(buildResponse(2, "LOGIN|You are already logged in."));
                    return;
                }

                User userInst = null;
                if (request.getUser().hasEmail())
                    userInst = userMapper.findUserByEmail(request.getUser().getEmail());
                if (request.getUser().hasUid())
                    userInst = userMapper.findUserByUid(request.getUser().getUid());

                if (userInst == null
                        || !SHA.getSHA_256(SHA.byte2Hex(request.getUser().getPwd().toByteArray()) + userInst.getSalt())
                        .equals(userInst.getPassword()))
                {
                    ctx.channel().writeAndFlush(buildResponse(201, "LOGIN|Wrong UID/Email/Password."));
                    return;
                }


                ctx.channel().writeAndFlush(buildResponse(0, "LOGIN|Success."));
            }
            case LOGOUT ->
            {//用户主动断开与服务器的连接
                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();
                Out.ConsoleOut.info("User " + onlineUser.getUUID() + " request to logout");
                ctx.channel().close();
            }
            case UPDATE_INFO ->
            {//用户更新信息
                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();

                if (!onlineUser.isLoginUser())
                {
                    ctx.channel().writeAndFlush(buildResponse(2, "UPDATE_INFO|You haven't logged in."));
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

                if (request.getUser().hasDefaultNick())
                {
                    onlineUser.getUser().setDefault_nick(request.getUser().getDefaultNick());
                    userMapper.updateUserDefaultNick(onlineUser.getUser().getUid(), request.getUser().getDefaultNick());
                }

                if (request.getUser().hasPwdOld())
                {
                    if (!SHA.getSHA_256(SHA.byte2Hex(request.getUser().getPwd().toByteArray()) + onlineUser.getUser().getSalt())
                            .equals(onlineUser.getUser().getPassword()))
                    {
                        ctx.channel().writeAndFlush(buildResponse(201, "UPDATE_INFO|Wrong Password."));
                        return;
                    } else
                    {
                        String salt = RandGenerator.randSalt(); //生成随机Salt
                        //TODO 更新密码
                        SHA.getSHA_256(SHA.byte2Hex(request.getUser().getPwd().toByteArray()) + salt);
                    }
                }
            }
            case JOIN_CHA ->
            {//用户加入频道
                //检查参数完整性
                if (!request.hasChannel() || request.getChannel().getName().length() > 128 || request.getChannel().getName().length() < 4)
                {
                    ctx.channel().writeAndFlush(buildResponse(101, "JOIN_CHA|Missing user information."));
                    return;
                }

                OnlineUser onlineUser = ctx.channel().attr(AttributeKey.<OnlineUser>valueOf("user")).get();

                String chaName = request.getChannel().getName();

                String nick = onlineUser.isLoginUser() ? onlineUser.getUser().getDefault_nick() : null;
                if (request.getChannel().hasNick())
                {
                    if (request.getChannel().getNick().matches("^[A-Za-z]\\w{3,63}$"))
                        nick = request.getChannel().getNick();
                    else
                    {
                        ctx.channel().writeAndFlush(buildResponse(101, "JOIN_CHA|Illegal nick."));
                        return;
                    }
                } else if (nick == null)
                {
                    ctx.channel().writeAndFlush(buildResponse(101, "JOIN_CHA|Missing nick."));
                    return;
                }

                //检查频道是否存在, ticket是否正确.
                if (Core.checkChannelTicket(chaName, request.getChannel().hasTicket() ? request.getChannel().getTicket() : "") != 0)
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
                if (Core.getChaMap().get(chaName).getMemberMap().containsKey(nick))
                {
                    if (Core.getChaMap().get(chaName).getMemberMap().get(nick).isLoginUser())
                    {
                        ctx.channel().writeAndFlush(buildResponse(202, "JOIN_CHA|Unavailable nick."));
                        return;
                    } else
                    {
                        Core.getChaMap().get(chaName).getMemberMap().get(nick).getCtx().channel()
                                .writeAndFlush(buildResponse(303, "SYSTEM|You are kicked out."));
                    }
                }

                onlineUser.join(chaName, nick);
                Core.getChaMap().get(chaName).addMember(nick, onlineUser);

                ctx.writeAndFlush(buildResponse(0, "JOIN_CHA|" + chaName + " " + nick));
                infoPushService.pushChannelList();
            }
            case QUIT_CHA ->
            {//用户退出频道
                AttributeKey<OnlineUser> key = AttributeKey.valueOf("user");
                OnlineUser onlineUser = ctx.channel().attr(key).get();

                String chaName = request.getChannel().getName();
                //检查用户是否在频道中
                if (!onlineUser.isInChannel(chaName))
                {
                    ctx.channel().writeAndFlush(buildResponse(2, "QUIT_CHA|You are not in this channel."));
                    return;
                }

                String nick = onlineUser.getNick(chaName);
                Core.getChaMap().get(chaName).removeMember(nick);
                onlineUser.quit(chaName);

                ctx.writeAndFlush(buildResponse(0, "QUIT_CHA|" + chaName + " " + nick));
            }
            case CREATE_CHA ->
            {//用户创建频道
                AttributeKey<OnlineUser> key = AttributeKey.valueOf("user");
                OnlineUser onlineUser = ctx.channel().attr(key).get();

                String chaName = request.getChannel().getName();
                String nick = request.getChannel().getNick();

                //检查两个保留字与换行符
                if (chaName.equals("System") || chaName.equals("Default") || chaName.matches("^.{4,128}$"))
                {
                    ctx.writeAndFlush(buildResponse(202, "CREATE_CHA|Unavailable channel name."));
                    return;
                }

                if (Core.getChaMap().containsKey(chaName))
                {
                    if (chaName.equals(nick))
                    {
                        ctx.writeAndFlush(buildResponse(202, "CREATE_CHA|Unavailable nick."));
                        ctx.close();
                    }
                    ctx.writeAndFlush(buildResponse(202, "CREATE_CHA|Unavailable nick."));
                }

                Core.addCha(chaName,
                        request.getChannel().hasTicket() ? request.getChannel().getTicket() : "",
                        true);

                onlineUser.join(chaName, nick);
                Core.getChaMap().get(chaName).addMember(nick, onlineUser);

                ctx.writeAndFlush(buildResponse(0, "CREATE_CHA|" + chaName + " " + nick));
            }
            case SEND_MSG ->
            {
                AttributeKey<OnlineUser> key = AttributeKey.valueOf("user");
                OnlineUser onlineUser = ctx.channel().attr(key).get();

                String channel = request.getChannel().getName();
                if (!onlineUser.isInChannel(channel))
                {
                    ctx.channel().writeAndFlush(buildResponse(2, "QUIT_CHA|You are not in this channel."));
                    return;
                }

                Core.addMessage(new Message(
                        request.getMessage().getTimestamp(),
                        request.getChannel().getName(),
                        request.getChannel().getNick(),
                        Message.MessageType.values()[request.getMessage().getType()],
                        request.getMessage().getContent()
                ));
                ctx.writeAndFlush(buildResponse(0, "SEND_MSG|copy"));
            }
            /*
            case GETMSG -> {
                AttributeKey<User> key = AttributeKey.valueOf("user");
                User user = ctx.channel().attr(key).get();
                List<Message> messages = user.getMessageList();
                Response.ResponsePOJO.Builder ret = Response.ResponsePOJO.newBuilder();
                ret.setType(Response.ResponsePOJO.Type.MESSAGE);
                messages.forEach((m) -> ret.addMessage(Response.Message.newBuilder()
                        .setRecTime(m.getRecTime())
                        .setChannel(m.getChannel())
                        .setFromNick(m.getFromNick())
                        .setContent(m.getContent())
                        .build()));
                ctx.writeAndFlush(ret.build());
            }
            */
            case HEARTBEAT ->
            {
            }//ctx.writeAndFlush(buildResponse(0, "HEARTBEAT|Heartbeat echo"));
            /*
            case GET_MEMBER_LIST -> {
                Response.ResponsePOJO.Builder ret = Response.ResponsePOJO.newBuilder();
                ret.setType(Response.ResponsePOJO.Type.ChannelMemberList);
                Core.getChaMap().get(request.getChannel().getChannel()).memberMap.forEach((nick, userInst) -> {
                    ret.addMemberNick(nick);
                });
                ctx.writeAndFlush(ret.build());
            }*/
            case UNRECOGNIZED ->
            {
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
        if (userInfo_request.getPwd().size() < 64)
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
