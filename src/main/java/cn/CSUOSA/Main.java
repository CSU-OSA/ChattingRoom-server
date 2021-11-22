package cn.csuosa;

import cn.csuosa.handlers.CommandHandler;
import cn.csuosa.pojo.Command;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

public class Main {

    private static final String host = "127.0.0.1";
    private static final int port = 8080;

    public static void main(String[] args) throws Exception{
        startServer();
    }

    private static void startServer() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast("encoder", new ProtobufEncoder());
                        socketChannel.pipeline().addLast("decoder", new ProtobufDecoder(Command.CommandPOJO.getDefaultInstance()));
                        socketChannel.pipeline().addLast(new CommandHandler());
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);
        Channel channel = bootstrap.bind(host, port).sync().channel();
        channel.closeFuture().addListener((ChannelFutureListener) channelFuture -> {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        });
    }

}