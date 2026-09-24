import socket 
import threading

HOST = "192.168.1.15"
PUERTO = 6066

cliente = socket.socket(
    socket.AF_INET, 
    socket.SOCK_STREAM
)

cliente.connect((HOST, PUERTO))

print("==========================")
print("   MINICHAT CLIENTE   ")
print("==========================")

nombre = input("Ingrese su nombre: ")
cliente.sendall(nombre.encode("utf-8"))

def recibir_mensajes():
    while True:
        try:

            mensaje = cliente.recv(1024).decode("utf-8")

            if not mensaje:
                break

            print(f"\n{mensaje}")
            print("Mensaje: ", end="", flush=True)

        except:
            break

hilo_receptor = threading.Thread(
    target=recibir_mensajes
)

hilo_receptor.daemon = True
hilo_receptor.start()

while True:
    mensaje = input("Mensaje: ")

    cliente.sendall(mensaje.encode("utf-8"))
    if mensaje.lower() == "/salir":
        break
cliente.close()
