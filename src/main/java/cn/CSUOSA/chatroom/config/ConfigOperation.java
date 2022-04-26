package cn.csuosa.chatroom.config;

import cn.csuosa.chatroom.Out;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

public class ConfigOperation
{
    @Getter
    private static Configuration configuration = null;

    public static void readConfig()
    {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        File configFile = new File("config.json");

        if (!configFile.exists())
        {//如果不存在配置文件，则认为是第一次运行，自动生成初始配置
            try
            {
                if (configFile.createNewFile())
                {
                    configuration = new Configuration("0.0.0.0", 8080,
                            "jdbc:mysql://localhost/chat_room", "root", "");
                    jsonObjectMapper.writeValue(configFile, configuration);
                    Out.ConsoleOut.warn("This may be the first time you run this program.\n" +
                            "Please try to run again after perfecting the configuration in \"config.json\".");
                }
            } catch (IOException e)
            {
                e.printStackTrace();
                Out.ConsoleOut.err("Failed to create new configuration file. Program exit.");
            }
            System.exit(0);
        }

        try
        {
            configuration = jsonObjectMapper.readValue(configFile, Configuration.class);
        } catch (IOException e)
        {
            e.printStackTrace();
            Out.ConsoleOut.err("Failed to read configuration file. Program exit.");
        }
    }
}
