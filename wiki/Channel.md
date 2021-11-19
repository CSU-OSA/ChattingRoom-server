`频道(Channel)`也是CSUchatting中的重要元素，使用者必须加入一个`频道`才能在频道中发送或接收信息。

频道有公共和私有之分,私有频道需要验证凭证(ticket)才可加入,而公共频道不需要.

服务端在启动时会自动创建一个公共频道`PublicChannel`,该频道不需要验证凭证即可加入,且不会因为频道中无人在线而被自动关闭. 用户也可以自行 创建/加入 公共/私有 频道. 目前,由用户创建的任何频道都将在频道内最后一个用户退出后自动关闭.

## 创建
### URL
`~/channel/create`

### 请求内容
方法: POST
#### 参数
- `usrNick`: 用户昵称,用户在注册时向服务端提供的昵称
- `name`: 频道名,长度应在4~64个英文字符以内,目前仅支持数字/英文大小写
#### body
- `usrTicket`: 用户凭证,用户在注册时向服务端提供的与昵称配对的凭证
- `ticket`: 频道凭证,长度应为6个英文字符,目前仅支持数字/英文大小写

### 关于频道的创建

用户有时需要创建新频道(整活或者[doge])

想要创建新频道,你需要向服务端发送一个POST请求. 请求参数应当包含你的nick,与之配对的ticket,想要创建的频道的name和与之配对的ticket(可选). 服务端接收到请求,处理后会返回一个`BoolWithMsg Json`,包含指示操作是否成功的标签`success`和信息标签`msg`. 当success为`true`时,操作成功,msg为空; 当success为`false`时,操作失败,msg会包含相关信息.

### 示例
```
//URL
http://localhost:8003/chat/channel/create?usrNick=OctAutumn&name=HotBedOfCSE

//data (header-type: 'application/form-data')(可直接拷贝到Postman的body中)
[
    {
        "key": "usrTicket",
        "value": "40096a",
        "description": "",
        "type": "text",
        "enabled": true
    },
    {
        "key": "ticket",
        "value": "40096b",
        "description": "",
        "type": "text",
        "enabled": true
    }
]
```
这条请求将在本地服务端创建一个name为`HotBedOfCSE`的频道,与之配对的ticket为`40096b`,且用户OctAutumn将自动加入到该频道中.

## 加入
### URL
`~/channel/join`

### 请求内容
方法: POST
#### 参数
- `usrNick`: 用户昵称,用户在注册时向服务端提供的昵称
- `name`: 频道名
#### body
- `usrTicket`: 用户凭证,用户在注册时向服务端提供的与昵称配对的凭证
- `ticket`: (可选)频道凭证,与该频道配对的频道凭证

### 关于频道的加入

用户需要加入`频道`才可以在频道中发送与接收消息

想要加入频道,你需要向服务端发送一个POST请求. 请求参数应当包含你的nick,与之配对的ticket,想要加入的频道的name和与之配对的ticket(可选). 服务端接收到请求,处理后会返回一个`BoolWithMsg Json`,包含指示操作是否成功的标签`success`和信息标签`msg`. 当success为`true`时,操作成功,msg为空; 当success为`false`时,操作失败,msg会包含相关信息.

### 示例
```
//URL
http://localhost:8003/chat/user/renew?usrNick=OctAutumn&name=HotBedOfCSE

//data (header-type: 'application/form-data')(可直接拷贝到Postman的body中)
[
    {
        "key": "usrTicket",
        "value": "40096a",
        "description": "",
        "type": "text",
        "enabled": true
    },
    {
        "key": "ticket",
        "value": "40096b",
        "description": "",
        "type": "text",
        "enabled": true
    }
]
```
这条请求使用户OctAutumn加入到频道HotBedOfCSE中(HotBedOfCSE的配套ticket是40096b)
