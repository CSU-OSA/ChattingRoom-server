package cn.CSUOSA.ChattingRoomServer.ConsoleController;

import cn.CSUOSA.ChattingRoomServer.Main;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

import static cn.CSUOSA.ChattingRoomServer.Main.mainController;

public class CmdProcessor implements Runnable
{
    private boolean keepRunning = true;

    public void stopRunning()
    {
        keepRunning = false;
        synchronized (this) {notify();}
    }

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
            while (keepRunning)
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
            case "stop", "Stop", "STOP" -> mainController.closeServer();
            case "list", "List", "LIST" -> Processors.listObject(args);
        }
    }
}
