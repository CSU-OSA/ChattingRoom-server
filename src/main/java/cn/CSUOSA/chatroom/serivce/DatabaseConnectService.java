package cn.csuosa.chatroom.serivce;

import cn.csuosa.chatroom.CoreResources;
import cn.csuosa.chatroom.Out;
import cn.csuosa.chatroom.config.ConfigOperation;
import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectService
{
    @Getter
    private static Connection connection = null;

    public static boolean connectDatabase() throws ClassNotFoundException
    {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try
        {
            connection = DriverManager.getConnection(ConfigOperation.getConfiguration().getDataBaseURL(),
                    ConfigOperation.getConfiguration().getDataBaseUser(),
                    ConfigOperation.getConfiguration().getDataBasePassword()
            );
        } catch (SQLException e)
        {
            e.printStackTrace();
            Out.ConsoleOut.err("Failed to connect to the database. Program exit.");
            System.exit(-1);
        }
        Out.ConsoleOut.info("Connected to the database.");
        new Thread(() -> {
            while (true)
            {
                try
                {
                    Thread.sleep(60000);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                try
                {
                    if (connection.isClosed())
                    {
                        Out.ConsoleOut.warn("Connection to MySQL is closed. Reconnecting...");
                        connection = DriverManager.getConnection(ConfigOperation.getConfiguration().getDataBaseURL(),
                                ConfigOperation.getConfiguration().getDataBaseUser(),
                                ConfigOperation.getConfiguration().getDataBasePassword()
                        );
                    }
                } catch (SQLException e)
                {
                    e.printStackTrace();
                    Out.ConsoleOut.err("Failed to connect to the database. Program exit.");
                    CoreResources.infoPushService.sendSysMsgToAll("Server is closing because of something wrong happened.", "Server");
                    System.exit(-1);
                }
            }
        }).start();
        return true;
    }
}
