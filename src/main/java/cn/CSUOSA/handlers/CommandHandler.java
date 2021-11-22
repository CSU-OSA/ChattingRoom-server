package cn.csuosa.handlers;

import cn.csuosa.Core;
import cn.csuosa.model.Message;
import cn.csuosa.model.User;
import cn.csuosa.pojo.Command;
import cn.csuosa.pojo.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class CommandHandler extends ChannelInboundHandlerAdapter {

    private Response.ResponsePOJO buildResponse(boolean isOk, String reason) {
        Response.Result result = Response.Result.newBuilder()
                .setResult(isOk)
                .setReason(reason)
                .build();
        return Response.ResponsePOJO.newBuilder()
                .setType(Response.ResponsePOJO.Type.RESULT)
                .setResult(result)
                .build();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        Command.CommandPOJO command = (Command.CommandPOJO) msg;
        switch (command.getOperation()) {
            case LOGIN: {
                User user = new User();
                boolean status = user.login();
                ctx.writeAndFlush(buildResponse(status, (status) ? user.getUuid() : "error"));
                break;
            }
            case LOGOUT: {
                User user = Core.getUser(command.getUser().getUuid());
                boolean status = user.logout();
                ctx.writeAndFlush(buildResponse(status, (status) ? "ok" : "error"));
                break;
            }
            case JOIN_CHANNEL: {
                Response.ResponsePOJO response;
                User user = Core.getUser(command.getUser().getUuid());
                user.join(command.getChannel().getChannel(), command.getChannel().getNick());
                response = buildResponse(true, "ok");
                ctx.writeAndFlush(response);
                break;
            }
            case QUIT_CHANNEL: {
                User user = Core.getUser(command.getUser().getUuid());
                String channel = command.getChannel().getChannel();
                if (!user.isInChannel(channel))
                    throw new Exception("You are not in this channel");
                user.quit(channel);
                ctx.writeAndFlush(buildResponse(true, "ok"));
                break;
            }
            case SEND: {
                User user = Core.getUser(command.getUser().getUuid());
                String channel = command.getMessage().getChannel();
                if (!user.isInChannel(channel))
                    throw new Exception("You are not in this channel");
                user.send(new Message(channel, user.getNick(channel), command.getMessage().getContent()));
                ctx.writeAndFlush(buildResponse(true, "ok"));
                break;
            }
            case RECEIVE: {
                User user = Core.getUser(command.getUser().getUuid());
                Message[] messages = user.getMessageList();
                Response.ResponsePOJO.Builder ret = Response.ResponsePOJO.newBuilder();
                for (Message m : messages) {
                    ret.addMessage(Response.Message.newBuilder()
                            .setChannel(m.getChannel())
                            .setFromNick(m.getFromNick())
                            .setContent(m.getContent())
                            .build());
                }
                ret.setType(Response.ResponsePOJO.Type.MESSAGE);
                ctx.writeAndFlush(ret.build());
                break;
            }
            case HEARTBEAT: {
                // TODO: 心跳包
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.writeAndFlush(buildResponse(false, cause.getMessage()));
        // TODO: RuntimeError should close the socket
    }
}
