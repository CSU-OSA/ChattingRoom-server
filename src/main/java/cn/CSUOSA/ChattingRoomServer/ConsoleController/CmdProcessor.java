package cn.CSUOSA.ChattingRoomServer.ConsoleController;

import cn.CSUOSA.ChattingRoomServer.Main;
import cn.CSUOSA.ChattingRoomServer.OverWriteMethod.Out;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class CmdProcessor implements Runnable
{
    @Override
    public void run()
    {
        try
        {
            Main.terminal = TerminalBuilder.builder()
                    .system(true)
                    .jansi(true)
                    .build();

            Main.lineReader = LineReaderBuilder.builder()
                    .terminal(Main.terminal)
                    .build();

            String prompt = "ChatServer> ";
            while (true)
            {
                String line = Main.lineReader.readLine(prompt);
                String[] args = line.split(" ");
                CommandProcessor(args);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void CommandProcessor(String[] args)
    {
        switch (args[0])
        {
            case "exit", "Exit", "EXIT" -> {
                Out.Warn("Server Exit.");
                System.exit(0);
            }
            case "list", "List", "LIST" -> Processors.listObject(args);
        }
    }
}
