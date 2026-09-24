package Servidor;

//se importan las clases necesarias y el archivo Comprobante.java que contiene la clase Comprobante
import Modelos.Comprobante;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CajeroRemoto extends Remote {

    // Valida el código de autenticidad, la tarjeta, vencimiento, CVV y saldo,
    // y si todo es correcto descuenta el monto. Devuelve un Comprobante con el resultado 
    Comprobante autorizarCompra(String codigo, String tarjeta, String vencimiento,
                                 String cvv, String articulo, double monto) throws RemoteException;

    // Método extra para aprovechar RMI: cualquier cliente autenticado puede
    // consultar el saldo actual sin tener que repetir una "compra"
    double consultarSaldo(String tarjeta, String codigo) throws RemoteException;
}
