import Command_pb2
import Response_pb2
import google.protobuf
import google.protobuf.any_pb2
import socket

def Login_bytes():
    login = Command_pb2.CommandPOJO()
    login.operation = Command_pb2.CommandPOJO.Operation.LOGIN
    login.user.uuid = ''
    return login.SerializeToString()

def Logout_bytes(uuid):
    logout = Command_pb2.CommandPOJO()
    logout.operation = Command_pb2.CommandPOJO.Operation.LOGOUT
    logout.user.uuid = uuid
    return logout.SerializeToString()

def Join_Channel_bytes(uuid, ch, nick):
    join = Command_pb2.CommandPOJO()
    join.operation = Command_pb2.CommandPOJO.Operation.JOIN_CHANNEL
    join.user.uuid = uuid
    join.channel.channel = ch
    join.channel.nick = nick
    return join.SerializeToString()

def Quit_Channel_bytes(uuid, ch):
    q = Command_pb2.CommandPOJO()
    q.operation = Command_pb2.CommandPOJO.Operation.QUIT_CHANNEL
    q.user.uuid = uuid
    q.channel.channel = ch
    return q.SerializeToString()

def Send_bytes(uuid, ch, content):
    msg = Command_pb2.CommandPOJO()
    msg.operation = Command_pb2.CommandPOJO.Operation.SEND
    msg.user.uuid = uuid
    msg.message.channel = ch
    msg.message.content = content
    return msg.SerializeToString()

def Receive_bytes(uuid):
    op = Command_pb2.CommandPOJO()
    op.operation = Command_pb2.CommandPOJO.Operation.RECEIVE
    op.user.uuid = uuid
    return op.SerializeToString()

if __name__ == "__main__":
    client = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    client.connect(('127.0.0.1', 8080))
    print("Login")
    client.send(Login_bytes())
    data = client.recv(1024)
    response = Response_pb2.ResponsePOJO()
    response.ParseFromString(data)
    print(response)
    uuid = response.result.reason
    while True:
        tmp = input('[1]Logout [2]Join Channel [3]Quit Channel [4]Send [5]Receive\n')
        if tmp == '1':
            client.send(Logout_bytes(uuid))
        elif tmp == '2':
            ch = input('Please enter the channel name\n')
            nick = input('Please enter the nick name\n')
            client.send(Join_Channel_bytes(uuid, ch, nick))
        elif tmp == '3':
            ch = input('Please enter the channel name\n')
            client.send(Quit_Channel_bytes(uuid, ch))
        elif tmp == '4':
            ch = input('Please enter the channel name\n')
            content = input('Please enter the content\n')
            client.send(Send_bytes(uuid, ch, content))
        elif tmp == '5':
            client.send(Receive_bytes(uuid))
        data = client.recv(2048)
        response = Response_pb2.ResponsePOJO()
        response.ParseFromString(data)
        print(response)