package cn.csuosa.chatroom.terminal;

import cn.csuosa.chatroom.MainClass;
import cn.csuosa.chatroom.Out;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class TerminalProcessor implements Runnable
{
    private boolean keepRunning = true;
    private String prompt = "";

    public synchronized void setPrompt(String prompt)
    {
        this.prompt = prompt;
    }

    @Override
    public void run()
    {
        try
        {
            Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();

            MainClass.lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();

            prompt = "ChatRoom> ";

            while (keepRunning)
            {
                String line = MainClass.lineReader.readLine(prompt);
                String[] args = line.split(" ");

                cmdHandler(args);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void cmdHandler(String[] args)
    {
        if (args.length >= 1)
        {
            switch (args[0])
            {
                case "stop", "STOP" -> {
                    Out.ConsoleOut.warn("Program exit.");
                    System.exit(0);
                }
                case "help", "HELP" -> Out.ConsoleOut.info("""
                                                    
                        ------------HELP-------------
                        stop        关闭服务端
                        help        命令帮助
                        kick        将某用户从频道中踢出(或直接中断与服务器的连接)
                                                    
                        """);
                case "kick", "KICK" ->
                {
                    if ((args.length == 2) && (args[1].equals("-h") || args[1].equals("--help")))
                    {
                        Out.ConsoleOut.info("""
                                kick指令用于将某用户从频道中踢出, 或直接中断其与服务器的连接.
                                用法：kick [opt] [talkChannel] [nick]
                                    opt:
                                        -h,--help   命令帮助
                                        -k,--kill   从频道中踢出
                                        -K,--KILL   断开与服务器的连接
                                    talkChannel:
                                        频道名
                                    nick:
                                        昵称
                                        
                                """);
                        break;
                    }
                    if (args.length == 4)
                    {
                        switch (args[1])
                        {
                            case "-k", "--kill" -> kickUser(args[2], args[3]);
                            case "-K", "--KILL" -> disconnectUser(args[2], args[3]);
                        }
                        break;
                    }
                    Out.ConsoleOut.warn("Incomplete command. Please check your input, or type \"kick -h\" for usage");
                }
                default -> Out.ConsoleOut.warn("Unsupported command. Please enter \"help\" or \"HELP\" to get help.");
            }
        }
    }

    private void kickUser(String ch, String nick)
    {

    }

    private void disconnectUser(String ch, String nick)
    {

    }
}
