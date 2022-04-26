package cn.csuosa.chatroom.mapper;

import cn.csuosa.chatroom.common.RandGenerator;
import cn.csuosa.chatroom.model.pojo.User;
import cn.csuosa.chatroom.serivce.DatabaseConnectService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper
{
    /**
     * 通过uid查找用户
     *
     * @param uid 用户id
     * @return 用户实例
     */
    public User findUserByUid(long uid) throws SQLException
    {
        PreparedStatement statement = DatabaseConnectService.getConnection()
                .prepareStatement("SELECT e_mail, password, salt, default_nick FROM user WHERE uid=?");

        statement.setLong(1, uid);

        ResultSet rawResult = statement.executeQuery();

        if (!rawResult.next())
        {
            statement.close();
            return null;
        }

        User res = User.builder()
                .uid(uid)
                .e_mail(rawResult.getString("e_mail"))
                .password(rawResult.getString("password"))
                .salt(rawResult.getString("salt"))
                .default_nick(rawResult.getString("default_nick"))
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
    public User findUserByEmail(String email) throws SQLException
    {
        //验证email地址，若不符合则返回null
        if (!email.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$"))
            return null;

        PreparedStatement statement = DatabaseConnectService.getConnection()
                .prepareStatement("SELECT uid, password, salt, default_nick FROM user WHERE e_mail=?");

        statement.setString(1, email);

        ResultSet rawResult = statement.executeQuery();

        if (!rawResult.next())
        {
            statement.close();
            return null;
        }

        User res = User.builder()
                .uid(rawResult.getInt("uid"))
                .e_mail(email)
                .password(rawResult.getString("password"))
                .salt(rawResult.getString("salt"))
                .default_nick(rawResult.getString("default_nick"))
                .build();

        statement.close();

        return res;
    }

    /**
     * 新建用户
     *
     * @param user 用户实例
     * @return 影响行数，正常应为1，否则为失败
     */
    public int newUser(User user) throws SQLException
    {
        PreparedStatement statement = DatabaseConnectService.getConnection()
                .prepareStatement("INSERT INTO user(e_mail, password, salt, default_nick) VALUES(?, ?, ?, ?)");

        statement.setString(1, user.getE_mail());
        statement.setString(2, user.getPassword());
        statement.setString(3, user.getSalt());
        statement.setString(4, user.getDefault_nick());

        int result = statement.executeUpdate();
        if (result == 1)
        {
            ResultSet keySet = statement.getGeneratedKeys();
            if (keySet.next())
                user.setUid(keySet.getInt(1));
        }
        statement.close();
        return 0;
    }

    /**
     * 更新用户密码（会同步更新Salt）
     *
     * @param uid    用户id
     * @param newPwd 新密码
     * @return 影响行数，正常应为1，否则为失败
     */
    public int updateUserPwd(int uid, String newPwd) throws SQLException
    {
        PreparedStatement statement = DatabaseConnectService.getConnection()
                .prepareStatement("UPDATE user SET password=?,salt=? WHERE uid=?");

        statement.setString(1, newPwd);
        statement.setString(2, RandGenerator.randSalt());
        statement.setInt(3, uid);

        statement.close();

        return statement.executeUpdate();
    }

    /**
     * 更新用户默认昵称
     *
     * @param uid            用户id
     * @param newDefaultNick 新默认昵称
     * @return 影响行数，正常应为1，否则为失败
     */
    public int updateUserDefaultNick(long uid, String newDefaultNick) throws SQLException
    {
        PreparedStatement statement = DatabaseConnectService.getConnection()
                .prepareStatement("UPDATE user SET default_nick=? WHERE uid=?");

        statement.setString(1, newDefaultNick);
        statement.setLong(2, uid);

        statement.close();

        return statement.executeUpdate();
    }
}
