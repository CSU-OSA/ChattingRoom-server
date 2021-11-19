`用户(User)`是CSUchatting中最重要的元素，使用者必须注册成为`用户`才能正常使用CSUchatting的各项功能。

## 注册
### URL
`~/user/login`

### 请求内容
方法: POST
#### 参数
- `nick`: 昵称,长度应在4~32个英文字符以内,目前仅支持数字/英文大小写
#### body
- `ticket`: 凭证,长度应为6个英文字符,目前仅支持数字/英文大小写

### 关于用户的注册

用户所有的操作都需要基于nick和与之配对的ticket. 因此,在使用CSUchatting时,最重要的就是注册并持有一个nick.

想要注册并持有一个nick,你需要向服务端发送一个POST请求. 请求参数应当包含你想要注册的nick和与之配对的ticket(类似于密码,但在一次登录期间不可更改). 服务端接收到请求,处理后会返回一个`BoolWithMsg Json`,包含指示操作是否成功的标签`success`和信息标签`msg`. 当success为`true`时,操作成功,msg为空; 当success为`false`时,操作失败,msg会包含相关信息.

### 示例
```
//URL
http://localhost:8003/chat/user/login?nick=OctAutumn

//data (header-type: 'application/form-data')(可直接拷贝到Postman的body中)
[
    {
        "key": "ticket",
        "value": "40096a",
        "description": "",
        "type": "text",
        "enabled": true
    }
]
```
这条请求将在本地服务端注册一个nick为`OctAutumn`的用户,与之配对的ticket为`40096a`

## 刷新
### URL
`~/user/renew`

### 请求内容
方法: POST
#### 参数
- `nick`: 昵称,长度应在4~32个英文字符以内,目前仅支持数字/英文大小写
#### body
- `ticket`: 凭证,长度应为6个英文字符,目前仅支持数字/英文大小写

### 关于用户昵称的刷新

目前,平台尚不提供永久注册功能. 用户的nick均为暂时持有,需要通过`刷新`来更新nick的占用状态.

服务端中,所有nick都有一个倒计时器,用户通过`刷新`来重置计时器,使之不会归零. 若用户没有在一定时间内`刷新`计时器,计时器归零后用户将自动注销,其占用的nick将被释放.

想要更新nick的占用状态,你需要向服务端发送一个POST请求. 请求参数应当包含你的nick和与之配对的ticket(在注册时提供). 服务端接收到请求,处理后会返回一个`BoolWithMsg Json`,包含指示操作是否成功的标签`success`和信息标签`msg`. 当success为`true`时,操作成功,msg为空; 当success为`false`时,操作失败,msg会包含相关信息.

### 示例
```
//URL
http://localhost:8003/chat/user/renew?nick=OctAutumn

//data (header-type: 'application/form-data')(可直接拷贝到Postman的body中)
[
    {
        "key": "ticket",
        "value": "40096a",
        "description": "",
        "type": "text",
        "enabled": true
    }
]
```
这条请求将重置nick为`OctAutumn`的用户的nick计时器,使之不会自动释放
