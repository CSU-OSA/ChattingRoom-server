package cn.csuosa.chatroom.handlers;

import cn.csuosa.chatroom.Core;
import cn.csuosa.chatroom.Out;
import cn.csuosa.chatroom.model.Message;
import cn.csuosa.chatroom.model.User;
import cn.csuosa.chatroom.proto.Request;
import cn.csuosa.chatroom.proto.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RequestHandler extends ChannelInboundHandlerAdapter
{

    private Response.ResponsePOJO buildResponse(boolean isOk, String msg)
    {
        Response.Result result = Response.Result.newBuilder()
                .setResult(isOk)
                .setMsg(msg)
                .build();
        return Response.ResponsePOJO.newBuilder()
                .setType(Response.ResponsePOJO.Type.RESULT)
                .setResult(result)
                .build();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        if (evt instanceof IdleStateEvent)
        {
            switch (((IdleStateEvent) evt).state())
            {
                case WRITER_IDLE, READER_IDLE, ALL_IDLE -> ctx.channel().close();
            }
        }
        else
        {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx)
    {
        AttributeKey<User> key = AttributeKey.valueOf("user");
        User user = Core.addNewUser();
        ctx.channel().attr(key).set(user);
        ctx.writeAndFlush(buildResponse(true, "VERSION Server-version:1.0.0-beta;API-edition:1.0.0-beta"));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        AttributeKey<User> key = AttributeKey.valueOf("user");
        User user = ctx.channel().attr(key).get();
        user.getNickNames().forEach((chaName, nick) -> Core.getChaMap().get(chaName).removeMember(nick));
        Core.removeUser(user.getUUID());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception
    {
        Request.RequestPOJO request = (Request.RequestPOJO) msg;
        switch (request.getOperation())
        {
            case LOGOUT -> ctx.channel().close();
            case JOIN_CHA -> {
                AttributeKey<User> key = AttributeKey.valueOf("user");
                User user = ctx.channel().attr(key).get();
                String chaName = request.getChannel().getChannel();
                String ticket = request.getChannel().hasTicket() ? request.getChannel().getTicket() : "";
                if (Core.chkCha(chaName, ticket) != 0)
                    throw new Exception("JOIN_CHA Failed to join Channel.");

                user.join(request.getChannel().getChannel(), request.getChannel().getNick());
                Core.getChaMap().get(request.getChannel().getChannel())
                        .addMember(request.getChannel().getNick(), user);

                ctx.writeAndFlush(buildResponse(true, "JOIN_CHA ok"));
            }
            case QUIT_CHA -> {
                AttributeKey<User> key = AttributeKey.valueOf("user");
                User user = ctx.channel().attr(key).get();

                String channel = request.getChannel().getChannel();
                if (!user.isInChannel(channel))
                    throw new Exception("QUIT_CHA You are not in this channel");

                user.quit(channel);
                Core.getChaMap().get(request.getChannel().getChannel())
                        .removeMember(request.getChannel().getNick());

                ctx.writeAndFlush(buildResponse(true, "QUIT_CHA ok"));
            }
            case CREATE_CHA -> {
                String channel = request.getChannel().getChannel();

                if (Core.getChaMap().containsKey(channel))
                    throw new Exception("CREATE_CHA Channel already exists.");

                String ticket = "";
                if (request.getChannel().hasTicket())
                    ticket = request.getChannel().getTicket();
                Core.addCha(channel, ticket);

                ctx.writeAndFlush(buildResponse(true, "CREATE_CHA ok"));
            }
            case SENDMSG -> {
                AttributeKey<User> key = AttributeKey.valueOf("user");

                User user = ctx.channel().attr(key).get();
                String channel = request.getMessage().getChannel();
                if (!user.isInChannel(channel))
                    throw new Exception("SENDMSG You are not in this channel");

                Core.addMessage(new Message(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                        channel, user.getNick(channel), request.getMessage().getContent()));
                ctx.writeAndFlush(buildResponse(true, "SENDMSG ok"));
            }
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
            case HEARTBEAT -> ctx.writeAndFlush(buildResponse(true, "HEARTBEAT Heartbeat echo"));
            case GET_CHANNEL_LIST -> {
                Response.ResponsePOJO.Builder ret = Response.ResponsePOJO.newBuilder();
                ret.setType(Response.ResponsePOJO.Type.ChannelList);
                Core.getChaMap().forEach((channelName, channel) -> {
                    ret.addChannelInfo(Response.ChannelInfo.newBuilder()
                            .setName(channelName)
                            .setIsPublic(channel.checkTicket(""))
                            .setMemberNum(channel.getMemberMap().size())
                            .build());
                });
                ctx.writeAndFlush(ret.build());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        ctx.writeAndFlush(buildResponse(false, cause.getMessage()));

        Out.ConsoleOut.err(cause.toString());
        // TODO: RuntimeError should close the socket
    }
}
