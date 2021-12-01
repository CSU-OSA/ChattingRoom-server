import Command_pb2
import Response_pb2
import google.protobuf
import google.protobuf.any_pb2
import socket
import threading
import time

flag1 = True
flag2 = True
flag3 = True

def Heartbeat_bytes():
    heartbeat = Command_pb2.CommandPOJO()
    heartbeat.operation = Command_pb2.CommandPOJO.Operation.HEARTBEAT
    return heartbeat.SerializeToString()

def heartbeat(socket):
    while flag1:
        socket.send(Heartbeat_bytes())
        time.sleep(2)

def recvieve_msg(socket):
    while flag2:
        data = client.recv(2048)
        response = Response_pb2.ResponsePOJO()
        response.ParseFromString(data)
        if(response.result.reason != 'Heartbeat echo'):
            print(response)

def Logout_bytes():
    logout = Command_pb2.CommandPOJO()
    logout.operation = Command_pb2.CommandPOJO.Operation.LOGOUT
    return logout.SerializeToString()

def Join_Channel_bytes(ch, nick):
    join = Command_pb2.CommandPOJO()
    join.operation = Command_pb2.CommandPOJO.Operation.JOIN_CHA
    join.channel.channel = ch
    join.channel.nick = nick
    return join.SerializeToString()

def Quit_Channel_bytes(ch):
    q = Command_pb2.CommandPOJO()
    q.operation = Command_pb2.CommandPOJO.Operation.QUIT_CHA
    q.channel.channel = ch
    return q.SerializeToString()

def Send_bytes(ch, content):
    msg = Command_pb2.CommandPOJO()
    msg.operation = Command_pb2.CommandPOJO.Operation.SENDMSG
    msg.message.channel = ch
    msg.message.content = content
    return msg.SerializeToString()

def Receive_bytes():
    op = Command_pb2.CommandPOJO()
    op.operation = Command_pb2.CommandPOJO.Operation.GETMSG
    return op.SerializeToString()

if __name__ == "__main__":
    client = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    client.connect(('127.0.0.1', 8080))
    t1 = threading.Thread(target = heartbeat, args = (client, ))
    t2 = threading.Thread(target = recvieve_msg, args = (client, ))
    t1.start()
    t2.start()
    while flag3:
        tmp = input('[1]Logout [2]Join Channel [3]Quit Channel [4]Send [5]Receive [6]Heartbeat\n')
        if tmp == '1':
            flag1 = False
            flag2 = False
            flag3 = False
            client.send(Logout_bytes())
        elif tmp == '2':
            ch = input('Please enter the channel name\n')
            nick = input('Please enter the nick name\n')
            client.send(Join_Channel_bytes(ch, nick))
        elif tmp == '3':
            ch = input('Please enter the channel name\n')
            client.send(Quit_Channel_bytes(ch))
        elif tmp == '4':
            ch = input('Please enter the channel name\n')
            content = input('Please enter the content\n')
            client.send(Send_bytes(ch, content))
        elif tmp == '5':
            client.send(Receive_bytes())
        elif tmp == '6':
            client.send(Heartbeat_bytes())
    print('Bye')
