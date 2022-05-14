package cn.csuosa.chatroom.mapper;

import cn.csuosa.chatroom.model.pojo.RegUser;

import java.sql.SQLException;

public interface RegUserMapper
{
    /**
     * 通过uid查找用户
     *
     * @param uid 用户id
     * @return 用户实例
     */
    RegUser findUserByUid(long uid) throws SQLException;

    /**
     * 通过E-mail地址查找用户
     *
     * @param email 用户E-mail地址
     * @return 用户实例
     */
    RegUser findUserByEmail(String email) throws SQLException;

    /**
     * 新建用户
     *
     * @param regUser 用户实例
     * @return 影响行数，正常应为1，否则为失败
     */
    int newUser(RegUser regUser) throws SQLException;

    /**
     * 更新用户密码（需要同步更新Salt）
     *
     * @param uid     用户id
     * @param newPwd  新密码
     * @param newSalt 新Salt
     * @param editor  编辑者（一般为“Itself”）
     * @return 影响行数，正常应为1，否则为失败
     */
    int updateUserPwd(long uid, String newPwd, String newSalt, String editor) throws SQLException;

    /**
     * 更新用户默认昵称
     *
     * @param uid            用户id
     * @param newDefaultNick 新默认昵称
     * @param editor         编辑者（一般为“Itself”）
     * @return 影响行数，正常应为1，否则为失败
     */
    int updateUserDefaultNick(long uid, String newDefaultNick, String editor) throws SQLException;
}
