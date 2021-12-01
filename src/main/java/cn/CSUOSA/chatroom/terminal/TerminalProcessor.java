package cn.csuosa.chatroom.terminal;

import cn.csuosa.chatroom.Main;
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

            Main.lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();

            prompt = "ChatRoom> ";

            while (keepRunning)
            {
                String line = Main.lineReader.readLine(prompt);
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
                                                    
                        """);
                default -> Out.ConsoleOut.warn("Unsupported command. Please enter \"help\" or \"HELP\" to get help.");
            }
        }
    }
}
