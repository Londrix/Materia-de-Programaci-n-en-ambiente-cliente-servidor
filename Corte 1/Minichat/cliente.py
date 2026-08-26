# Se importa la librería socket
import socket

# Se define la dirección IP y el puerto del servidor
HOST = "192.168.1.40"
PUERTO = 6066

# Crear socket TCP
cliente = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Se colocan prints para mostrar información al usuario
print("==============================")
print("      CLIENTE MINICHAT        ")  
print("==============================")

# Actualizacion 1
nombre = input("Ingresa tu nombre: ").strip()

print(f"\nConectando con {HOST}:{PUERTO}...")

# Conectarse al servidor
cliente.connect((HOST, PUERTO))

print("Conexión establecida.")
print("Comandos disponibles:")
print("/ayuda - Muestra esta ayuda")
print("/salir - Finaliza la conexión")
print("/hora - Muestra la hora del servidor")
print()

# Este while permite al cliente enviar mensajes al servidor y recibir respuestas
while True:
    # Escribir mensaje mostrando el nombre del usuario en el prompt
    mensaje = input(f"{nombre}: ")

    # Si es comando /salir
    if mensaje.lower() == "/salir":
        cliente.sendall(mensaje.encode("utf-8"))
        break

    # para que el cliente pueda enviar comandos al servidor, se verifica si el mensaje comienza con "/"
    if mensaje.startswith("/"):
        cliente.sendall(mensaje.encode("utf-8"))
    else:
        mensaje_a_enviar = f"{nombre}: {mensaje}"
        cliente.sendall(mensaje_a_enviar.encode("utf-8"))

    # Recibir respuesta del servidor
    respuesta = cliente.recv(1024).decode("utf-8")
    print(f"Servidor: {respuesta}")

    if respuesta.lower() == "/salir":
        break

# Cerrar la conexión con el servidor
cliente.close()
print("\nConexión cerrada.")