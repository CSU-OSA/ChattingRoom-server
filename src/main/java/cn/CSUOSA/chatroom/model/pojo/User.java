package cn.csuosa.chatroom.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class User
{
    /**
     * 用户ID
     */
    long uid;

    /**
     * 用户注册的邮箱
     */
    @NonNull
    String e_mail;

    /**
     * 加密后的用户密码
     */
    @NonNull
    String password;

    @NonNull
    String salt;

    /**
     * 用户默认昵称
     */
    String default_nick;
}
