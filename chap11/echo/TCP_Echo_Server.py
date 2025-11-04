#TCP_Echo_Server.py
from socket import *

port = 2500
BUFSIZE = 1024

sock = socket(AF_INET, SOCK_STREAM)
sock.bind(('0.0.0.0', port))
sock.listen(1)
conn, (remotehost, remoteprot) = sock.accept()
print('connected by', remotehost, remoteprot)
while True:
    data = conn.recv(BUFSIZE)
    if not data:
        break
    print("Received message:", data.decode())
    conn.send(data)
conn.close()