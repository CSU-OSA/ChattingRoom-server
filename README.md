# ChattingRoom-server

CSU-OSA Chatting Room (server)

CSU-OSA 聊天室(服务端)

**该版本为重构后的早期架构, 含大量需修补的内容(代码规范 边界条件)**

## 框架说明

该版本使用Socket连接, 传输二进制数据(Google Protobuf编码). 目前仍使用类Http的无状态通信, 新消息的获取需要轮询服务端.

注意: 非稳定版本, 服务端可能仍然存在隐形bug. 如果遇到任何问题, 欢迎在issue里提出.

## 接口

每一个接口都对应一条指令, 同时也对应一个RequestPOJO数据传输对象. RequestPOJO数据传输对象需指定指令类别, 并附加需要的信息内容, 具体定义见`/src/proto/Request.proto`.

- `LOGOUT`: 退出用户的登录并清除所有信息
- `JOIN_CHA`: 携带频道名称&频道ticket&昵称, 加入该频道
- `QUIT_CHA`: 携带频道名称, 退出该频道
- `CREATE_CHA`: 携带频道名称&频道ticket&昵称, 创建该频道并加入
- `SENDMSG`: 携带频道名称&发送内容, 向该频道发送信息
- `GETMSG`: 拉取用户在所有频道内的新消息
- `HEARTBEAT`: 发送心跳包(检测间隔10s)
- `VERSION`: 获取服务端版本和API版本信息
- `GET_CHANNEL_LIST`: 获取频道列表

## 返回值

返回值的数据传输对象定义位于`/src/proto/Response.proto`, 当前仅包含简单的操作状态返回值&消息列表返回值&频道列表返回值.

## 测试客户端

"官方"客户端: [ChattingRoom-client](https://github.com/CSU-OSA/ChattingRoom-client), 由OctAutumn基于Qt(Cpp)框架编写

`/src/proto/`下的批处理文件用于编译生成`Google Protobuf`类(服务端及测试客户端).

运行前请确保你安装了`protobuf`库

```shell
pip install protobuf
```
