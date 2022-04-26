package cn.csuosa.chatroom.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor()
public class Configuration
{
    private String serverURL;
    private int serverPort;
    private String dataBaseURL;
    private String dataBaseUser;
    private String dataBasePassword;
}
