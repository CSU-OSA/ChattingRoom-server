package cn.csuosa.chatroom.serivce;

import cn.csuosa.chatroom.Out;
import cn.csuosa.chatroom.common.RandGenerator;
import cn.csuosa.chatroom.common.SHA;
import cn.csuosa.chatroom.mapper.RegUserMapper;
import cn.csuosa.chatroom.mapper.impl.RegUserMapperImpl;
import cn.csuosa.chatroom.model.pojo.RegUser;

import java.sql.SQLException;
import java.util.Date;

public class RegUserService
{
    private final RegUserMapper regUserMapper = new RegUserMapperImpl();              //注册用户Mapper

    /**
     * 通过uid查找用户
     *
     * @param uid 用户id
     * @return 用户实例
     */
    public RegUser findUserByUid(long uid) throws SQLException
    {
        return regUserMapper.findUserByUid(uid);
    }

    /**
     * 通过E-mail地址查找用户
     *
     * @param email 用户E-mail地址
     * @return 用户实例
     */
    public RegUser findUserByEmail(String email) throws SQLException
    {
        return regUserMapper.findUserByEmail(email);
    }

    /**
     * 新增注册用户
     *
     * @param email          新注册用户的Email
     * @param pwdAsByteArray 新注册用户的pwd（字节型）
     * @param defaultNick    默认昵称（可以留null）
     * @throws SQLException SQL异常
     */
    public void newRegisterUser(String email, byte[] pwdAsByteArray, String defaultNick) throws SQLException
    {
        String salt = RandGenerator.randSalt(); //生成随机Salt

        RegUser newRegUserInst = RegUser.builder()
                .e_mail(email)
                .authentication_field(SHA.getSHA_256(SHA.byte2Hex(pwdAsByteArray) + salt))
                .salt(salt)
                .default_nick(defaultNick)
                .create_time(new Date())
                .creator("Itself")
                .last_edit(new Date())
                .last_editor("Itself")
                .build();

        regUserMapper.newUser(newRegUserInst);  //向数据库中写入内容

        Out.ConsoleOut.info("A new regUser was created with uid {" + newRegUserInst.getUid() + "}");
    }

    /**
     * 更新注册用户信息
     *
     * @param uid               注册用户的uid
     * @param newPwdAsByteArray
     * @param newDefaultNick
     * @throws SQLException
     */
    public void updateRegUserInfo(Long uid, byte[] newPwdAsByteArray, String newDefaultNick) throws SQLException
    {
        if (newPwdAsByteArray != null)
        {
            String salt = RandGenerator.randSalt(); //生成随机Salt
            regUserMapper.updateUserPwd(uid, SHA.getSHA_256(SHA.byte2Hex(newPwdAsByteArray) + salt), salt, "Itself");
        }

        if (newDefaultNick != null)
        {
            regUserMapper.updateUserDefaultNick(uid, newDefaultNick, "Itself");
        }
    }
}
