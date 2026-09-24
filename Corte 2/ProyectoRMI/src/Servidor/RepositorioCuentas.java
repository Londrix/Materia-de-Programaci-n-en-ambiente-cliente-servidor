package Servidor;

//se importan las clases necesarias y el archivo de cuenta
import Modelos.Cuenta;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class RepositorioCuentas {
    //estos private hacen que la ruta del archivo y el mapa de cuentas solo puedan ser accedidos desde esta clase
    private final Path archivo;
    private final Map<String, Cuenta> cuentas = new HashMap<>();

    //funcion que recibe la ruta del archivo de cuentas y llama a la funcion cargar para cargar las cuentas en memoria
    public RepositorioCuentas(String ruta) throws IOException {
        this.archivo = Paths.get(ruta);
        cargar();
    }

    //esta funcion carga las cuentas desde el archivo de texto y las guarda en un mapa de cuentas
    private void cargar() throws IOException {
        for (String linea : Files.readAllLines(archivo)) {
            if (linea.isBlank()) continue;
            String[] p = linea.split(";");
            cuentas.put(p[0], new Cuenta(p[0], p[1], p[2], Double.parseDouble(p[3])));
        }
    }

    //esta funcion busca una cuenta en el mapa de cuentas y la devuelve, si no la encuentra devuelve null
    public synchronized Cuenta buscar(String tarjeta) {
        return cuentas.get(tarjeta);
    }
    //esta funcion guarda las cuentas en el archivo de texto, sobrescribiendo el archivo anterior
    public synchronized void guardar() throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Cuenta c : cuentas.values()) {
            lineas.add(c.getTarjeta() + ";" + c.getVencimiento() + ";"
                     + c.getCvv() + ";" + c.getSaldo());
        }
        Files.write(archivo, lineas);
    }
}