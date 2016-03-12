#!/usr/bin/env python

import socket
import sys
import threading

#TCP_IP = '127.0.0.1'
# IP da maquina:
TCP_IP = socket.gethostbyname(socket.gethostname())
#TCP_PORT = 5005
# Porta recebida como argumento
TCP_PORT = int(sys.argv[1])
BUFFER_SIZE = 1024

# Abrir a conexao
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((TCP_IP, TCP_PORT))
s.listen(5)

userlist = dict()

class ClientThread(threading.Thread):
    def __init__(self, ip, port, socket):
        threading.Thread.__init__(self)
        self.ip = ip
        self.port = port
        self.socket = socket

    def run(self):
        # use self.socket to send/receive
        print "[+] Received connection: " + self.ip + ":" + str(self.port)

        while True:
            data = self.socket.recv(BUFFER_SIZE)
            if data == "EXIT": break
            #elif data == REGISTER
            #userlist[ip+':'+str(port)] = "andre"
            if data:
                print "> Client: " + self.ip + ":" + str(self.port)
                print "> Command:" + data
                self.socket.send(data)
            else:
                break

        self.socket.close()
        print "[+] Closed connection: " + self.ip + ":" + str(self.port)

while True:
   (clientsock, (ip, port)) = s.accept()
   new = ClientThread(ip, port, clientsock)
   new.start()

s.close()
