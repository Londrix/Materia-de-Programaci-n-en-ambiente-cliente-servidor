import socket
from datetime import datetime  # Actualización 4: Importar datetime

HOST = "0.0.0.0"
PUERTO = 6066

servidor = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
servidor.bind((HOST, PUERTO))
servidor.listen(1)

print("=============================")
print("      SERVIDOR MINICHAT      ")
print("=============================")
print(f"Esperando conexión en el puerto {PUERTO}...")

cliente, direccion = servidor.accept()
print(f"Cliente conectado desde: {direccion}")

while True:
    mensaje = cliente.recv(1024).decode("utf-8")
    if not mensaje:
        break

    print(f"\n{mensaje}")

    # Extraer el texto limpiando prefijos de usuario si los hay
    texto_mensaje = mensaje.split(":", 1)[-1].strip().lower()

    # Manejo del comando /salir
    if texto_mensaje == "/salir":
        respuesta = "Conexión finalizada."
        cliente.sendall(respuesta.encode("utf-8"))
        break

    # Manejo del comando /ayuda
    elif texto_mensaje == "/ayuda":
        respuesta = "\nComandos disponibles:\n/hora\n/ayuda\n/salir"
        cliente.sendall(respuesta.encode("utf-8"))
        continue

    # Actualización 4: Respuesta automática al comando /hora
    elif texto_mensaje == "/hora":
        hora = datetime.now().strftime("%H:%M:%S")
        respuesta = f"Hora del servidor: {hora}"
        cliente.sendall(respuesta.encode("utf-8"))
        continue  # Salta el input manual y espera la siguiente petición

    # Respuesta manual para cualquier otro mensaje
    respuesta = input("Servidor: ")
    cliente.sendall(respuesta.encode("utf-8"))

    if respuesta.lower() == "/salir":
        break

cliente.close()
servidor.close()
print("\nConexión cerrada.")