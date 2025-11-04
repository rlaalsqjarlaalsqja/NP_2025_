import socket
port = int(input("Port No:"))
address = ("127.0.0.1", port)
BUFSIZE = 1024
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(address)
while True:
    msg = input("Message to send:")
    s.send(msg.encode())
    data = s.recv(BUFSIZE)
    print("Received message: %s" %data.decode())