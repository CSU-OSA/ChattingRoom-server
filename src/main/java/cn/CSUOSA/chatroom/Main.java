package cn.csuosa.chatroom;

import cn.csuosa.chatroom.handlers.RequestHandler;
import cn.csuosa.chatroom.proto.Request;
import cn.csuosa.chatroom.terminal.TerminalProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.jline.reader.LineReader;

import java.util.concurrent.TimeUnit;

public class Main
{
    public static final TerminalProcessor terminalProcessor = new TerminalProcessor();
    public static LineReader lineReader;

    private static final String host = "127.0.0.1";
    private static int port = 8080;

    public static void main(String[] args) throws Exception
    {
        if (args.length != 0)
        {
            if (args.length >= 2)
            {
                switch (args[0])
                {
                    case "-p", "--port":
                        if ((Integer.parseInt(args[1]) > 0) && (Integer.parseInt(args[1]) < 65536))
                        {
                            port = Integer.parseInt(args[1]);
                        }
                        break;
                    default:
                        System.out.println("Error Arg: Illegal port number. Server will try to start at port 8080");
                }
            }
        }

        System.out.println("""
                \033[34m
                      _____  _____ _    _        _           _   _   _
                     / ____|/ ____| |  | |      | |         | | | | (_)
                    | |    | (___ | |  | |   ___| |__   __ _| |_| |_ _ _ __   __ _
                    | |     \\___ \\| |  | |  / __| '_ \\ / _` | __| __| | '_ \\ / _` |
                    | |____ ____) | |__| | | (__| | | | (_| | |_| |_| | | | | (_| |
                     \\_____|_____/ \\____/   \\___|_| |_|\\__,_|\\__|\\__|_|_| |_|\\__, |
                                                                              __/ |
                                                                             |___/
                    ------
                    [CSU chatting] developed by CSU-OSA(c)
                \033[m
                """);

        new Thread(terminalProcessor).start();

        Core.addCha("PublicChannel", "");
        Thread.sleep(500);

        startServer();
    }

    private static void startServer() throws InterruptedException
    {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel socketChannel)
                    {
                        socketChannel.pipeline().addLast("decoder", new ProtobufDecoder(Request.RequestPOJO.getDefaultInstance()));
                        socketChannel.pipeline().addLast("encoder", new ProtobufEncoder());
                        socketChannel.pipeline().addLast("idle", new IdleStateHandler(10, 10, 10, TimeUnit.SECONDS));
                        socketChannel.pipeline().addLast(new RequestHandler());
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