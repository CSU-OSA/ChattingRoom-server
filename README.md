# ChattingRoom-server

CSU-OSA Chatting Room (server)

CSU-OSA 聊天室（服务端）

**该版本为重构后的早期架构，含大量需修补的内容（代码规范、边界条件）**

## 框架说明

该版本直接使用Socket连接，传输二进制数据（Google Protobuf编码）。为了一定程度上兼容原有未写完的客户端，仍使用类Http的无状态通信，新消息的获取需要轮询服务端。

## 接口

每一个接口都对应一条指令，同时也对应一个Command数据传输对象。Command数据传输对象需指定指令类别，并附加需要的信息内容，具体定义见`/src/proto/Command.proto`。

- `LOGOUT`: 退出该UUID用户的登录并清除所有信息
- `JOIN_CHANNEL`: 携带频道名称，加入该频道，频道不存在则直接创建
- `QUIT_CHANNEL`: 携带频道名称，退出该频道
- `SEND`: 携带频道名称、发送内容，向该频道发送信息
- `RECEIVE`: 拉取用户在所有频道内的新消息
- `HEARTBEAT`: 发送心跳包（检测间隔3s）

## 返回值

返回值的数据传输对象定义位于`/src/proto/Response.proto`，当前仅包含简单的操作状态返回值、消息列表返回值。

## 测试客户端

你可以运行位于`/src/proto/test/test_client.py`的基本功能测试客户端来进行简单的测试。`/src/proto/`下的批处理文件用于编译生成`Google Protobuf`类（服务端及测试客户端）。

运行前请确保你安装了`protobuf`库

```shell
pip install protobuf
```