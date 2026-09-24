package Servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorRMI {

    private static final int PUERTO_REGISTRY = 1099; // puerto estándar de RMI
    private static final String NOMBRE_SERVICIO = "CajeroRemoto";

    public static void main(String[] args) {
        //este try catch es para manejar cualquier excepcion que pueda ocurrir al iniciar el servidor RMI
        try {
            RepositorioCuentas repo = new RepositorioCuentas("cuentas.txt");

            // Creamos el objeto remoto y lo publicamos en el RMI Registry
            CajeroRemotoImpl servicio = new CajeroRemotoImpl(repo);

            // Creamos un registro RMI en el puerto especificado
            Registry registry = LocateRegistry.createRegistry(PUERTO_REGISTRY);

            // Registramos el objeto remoto en el registro con un nombre único
            registry.rebind(NOMBRE_SERVICIO, servicio);

            System.out.println("Servidor RMI listo.");
            System.out.println("Servicio publicado como \"" + NOMBRE_SERVICIO
                    + "\" en el puerto " + PUERTO_REGISTRY);
            System.out.println("Esperando solicitudes de los clientes...");

        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor RMI:");
            e.printStackTrace();
        }
    }
}