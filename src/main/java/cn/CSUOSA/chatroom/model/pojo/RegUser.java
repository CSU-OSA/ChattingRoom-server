package cn.csuosa.chatroom.model.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

@Data
@Builder
public class RegUser
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
    String authentication_field;

    @NonNull
    String salt;

    /**
     * 用户默认昵称
     */
    String default_nick;

    /**
     * 创建时间
     */
    @NonNull
    Date create_time;

    /**
     * 创建者
     */
    @NonNull
    String creator;

    /**
     * 上次编辑时间
     */
    @NonNull
    Date last_edit;

    /**
     * 上次编辑者
     */
    @NonNull
    String last_editor;
}
