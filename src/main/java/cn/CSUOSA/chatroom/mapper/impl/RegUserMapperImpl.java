package cn.csuosa.chatroom.mapper.impl;

import cn.csuosa.chatroom.mapper.RegUserMapper;
import cn.csuosa.chatroom.model.pojo.RegUser;
import cn.csuosa.chatroom.serivce.DatabaseConnectService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class RegUserMapperImpl implements RegUserMapper
{
    /**
     * 通过uid查找用户
     *
     * @param uid 用户id
     * @return 用户实例
     */
    public RegUser findUserByUid(long uid) throws SQLException
    {
        PreparedStatement statement = DatabaseConnectService.getConnection()
                .prepareStatement("SELECT e_mail, authentication_field, salt, default_nick, create_time, creator, last_edit, last_editor FROM user WHERE uid=?");

        statement.setLong(1, uid);

        ResultSet rawResult = statement.executeQuery();

        if (!rawResult.next())
        {
            statement.close();
            return null;
        }

        RegUser res = RegUser.builder()
                .uid(uid)
                .e_mail(rawResult.getString("e_mail"))
                .authentication_field(rawResult.getString("authentication_field"))
                .salt(rawResult.getString("salt"))
                .default_nick(rawResult.getString("default_nick"))
                .create_time(rawResult.getTimestamp("create_time"))
                .creator(rawResult.getString("creator"))
                .last_edit(rawResult.getTimestamp("last_edit"))
                .last_editor(rawResult.getString("last_editor"))
                .build();

        statement.close();

        return res;
    }

    /**
     * 通过E-mail地址查找用户
     *
     * @param email 用户E-mail地址
     * @return 用户实例
     */
    public RegUser findUserByEmail(String email) throws SQLException
    {
        //验证email地址，若不符合则返回null
        if (!email.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$"))
            return null;

        PreparedStatement statement = DatabaseConnectService.getConnection()
                .prepareStatement("SELECT uid, authentication_field, salt, default_nick, create_time, creator, last_edit, last_editor FROM user WHERE e_mail=?");

        statement.setString(1, email);

        ResultSet rawResult = statement.executeQuery();

        if (!rawResult.next())
        {
            statement.close();
            return null;
        }

        RegUser res = RegUser.builder()
                .uid(rawResult.getInt("uid"))
                .e_mail(email)
                .authentication_field(rawResult.getString("authentication_field"))
                .salt(rawResult.getString("salt"))
                .default_nick(rawResult.getString("default_nick"))
                .create_time(rawResult.getTimestamp("create_time"))
                .creator(rawResult.getString("creator"))
                .last_edit(rawResult.getTimestamp("last_edit"))
                .last_editor(rawResult.getString("last_editor"))
                .build();

        statement.close();

        return res;
    }

    /**
     * 新建用户
     *
     * @param regUser 用户实例
     * @return 影响行数，正常应为1，否则为失败
     */
    public int newUser(RegUser regUser) throws SQLException
    {
        PreparedStatement statement = DatabaseConnectService.getConnection()
                .prepareStatement("INSERT INTO user(e_mail, authentication_field, salt, default_nick, create_time, creator, last_edit, last_editor) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");

        statement.setString(1, regUser.getE_mail());
        statement.setString(2, regUser.getAuthentication_field());
        statement.setString(3, regUser.getSalt());
        statement.setString(4, regUser.getDefault_nick());
        statement.setTimestamp(5, new Timestamp(regUser.getCreate_time().getTime()));
        statement.setString(6, regUser.getCreator());
        statement.setTimestamp(7, new Timestamp(regUser.getLast_edit().getTime()));
        statement.setString(8, regUser.getLast_editor());

        int result = statement.executeUpdate();
        if (result == 1)
        {
            ResultSet keySet = statement.getGeneratedKeys();
            if (keySet.next())
                regUser.setUid(keySet.getInt(1));
        }
        statement.close();
        return 0;
    }

    /**
     * 更新用户密码（需要同步更新Salt）
     *
     * @param uid     用户id
     * @param newPwd  新密码
     * @param newSalt 新Salt
     * @param editor  编辑者（一般为“Itself”）
     * @return 影响行数，正常应为1，否则为失败
     */
    public int updateUserPwd(long uid, String newPwd, String newSalt, String editor) throws SQLException
    {
        PreparedStatement statement = DatabaseConnectService.getConnection()
                .prepareStatement("UPDATE user SET authentication_field=?, salt=?, last_edit=?,last_editor=? WHERE uid=?");

        statement.setString(1, newPwd);
        statement.setString(2, newSalt);
        statement.setTimestamp(3, new Timestamp(new Date().getTime()));
        statement.setString(4, editor);
        statement.setLong(5, uid);

        int ret = statement.executeUpdate();
        statement.close();

        return ret;
    }

    /**
     * 更新用户默认昵称
     *
     * @param uid            用户id
     * @param newDefaultNick 新默认昵称
     * @param editor         编辑者（一般为“Itself”）
     * @return 影响行数，正常应为1，否则为失败
     */
    public int updateUserDefaultNick(long uid, String newDefaultNick, String editor) throws SQLException
    {
        PreparedStatement statement = DatabaseConnectService.getConnection()
                .prepareStatement("UPDATE user SET default_nick=?, last_edit=?, last_editor=? WHERE uid=?");

        statement.setString(1, newDefaultNick);
        statement.setTimestamp(2, new Timestamp(new Date().getTime()));
        statement.setString(3, editor);
        statement.setLong(4, uid);

        int ret = statement.executeUpdate();
        statement.close();

        return ret;
    }
}
