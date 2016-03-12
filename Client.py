#!/usr/bin/env python
# Porta recebida como argumento
import socket
import sys

#TCP_IP = '127.0.0.1'
# IP da maquina:
TCP_IP = socket.gethostbyname(socket.gethostname())
#TCP_PORT = 5005
# Porta recebida como argumento
TCP_PORT = int(sys.argv[1])
BUFFER_SIZE = 1024

# Abrir a conexao
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((TCP_IP, TCP_PORT))

msg = ""
while msg != 'EXIT':
    msg = raw_input("Command:\n")
    s.send(msg)
    data = s.recv(BUFFER_SIZE)
    if data:
        print "Response:\n", data
s.close()
