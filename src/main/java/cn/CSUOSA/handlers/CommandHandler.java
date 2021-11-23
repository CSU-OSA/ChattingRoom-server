package cn.csuosa.handlers;

import cn.csuosa.model.Message;
import cn.csuosa.model.User;
import cn.csuosa.pojo.Command;
import cn.csuosa.pojo.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

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
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            switch (((IdleStateEvent) evt).state()) {
                case WRITER_IDLE, READER_IDLE, ALL_IDLE -> ctx.channel().close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        AttributeKey<User> key = AttributeKey.valueOf("user");
        User user = new User();
        ctx.channel().attr(key).set(user);
        ctx.writeAndFlush(buildResponse(true, "Login successfully"));
        System.out.println("User " + user.getUuid() + " login successfully");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        AttributeKey<User> key = AttributeKey.valueOf("user");
        User user = ctx.channel().attr(key).get();
        user.logout();
        System.out.println("User " + user.getUuid() + " logout successfully");
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        Command.CommandPOJO command = (Command.CommandPOJO) msg;
        switch (command.getOperation()) {
            case LOGOUT -> ctx.channel().close();
            case JOIN_CHANNEL -> {
                Response.ResponsePOJO response;
                AttributeKey<User> key = AttributeKey.valueOf("user");
                User user = ctx.channel().attr(key).get();
                user.join(command.getChannel().getChannel(), command.getChannel().getNick());
                response = buildResponse(true, "ok");
                System.out.println("User " + user.getUuid() + " join the channel <" + command.getChannel().getChannel() + ">, using nick name <" + command.getChannel().getNick() + ">");
                ctx.writeAndFlush(response);
            }
            case QUIT_CHANNEL -> {
                AttributeKey<User> key = AttributeKey.valueOf("user");
                User user = ctx.channel().attr(key).get();
                String channel = command.getChannel().getChannel();
                if (!user.isInChannel(channel))
                    throw new Exception("You are not in this channel");
                user.quit(channel);
                System.out.println("User " + user.getUuid() + " quit the channel <" + channel + ">");
                ctx.writeAndFlush(buildResponse(true, "ok"));
            }
            case SEND -> {
                AttributeKey<User> key = AttributeKey.valueOf("user");
                User user = ctx.channel().attr(key).get();
                String channel = command.getMessage().getChannel();
                if (!user.isInChannel(channel))
                    throw new Exception("You are not in this channel");
                user.send(new Message(channel, user.getNick(channel), command.getMessage().getContent()));
                ctx.writeAndFlush(buildResponse(true, "ok"));
            }
            case RECEIVE -> {
                AttributeKey<User> key = AttributeKey.valueOf("user");
                User user = ctx.channel().attr(key).get();
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
            }
            case HEARTBEAT -> ctx.writeAndFlush(buildResponse(true, "Heartbeat echo"));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.writeAndFlush(buildResponse(false, cause.getMessage()));
        // TODO: RuntimeError should close the socket
    }
}
