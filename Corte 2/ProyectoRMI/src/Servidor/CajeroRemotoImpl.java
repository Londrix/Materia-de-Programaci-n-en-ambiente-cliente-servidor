package Servidor;

//se importan las clases necesarias y el archivo Comprobante.java que contiene la clase Comprobante
//tambien se importa la clase Cuenta.java que contiene la clase Cuenta
import Modelos.Comprobante;
import Modelos.Cuenta;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CajeroRemotoImpl extends UnicastRemoteObject implements CajeroRemoto {
    // Codigo de 6 digitos que deben usar los clientes para autenticarse
    //para comprobar que pueden usar el servicio, este codigo es fijo y no cambia
    private static final String CODIGO_REGISTRO = "482915";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("MM/yy");

    private final RepositorioCuentas repo;

    protected CajeroRemotoImpl(RepositorioCuentas repo) throws RemoteException {
        super(); // exporta el objeto remoto en un puerto anonimo
        this.repo = repo;
    }

    @Override
    public Comprobante autorizarCompra(String codigo, String tarjeta, String vencimiento,
                                        String cvv, String articulo, double monto) throws RemoteException {

        if (monto <= 0) {
            return new Comprobante(Comprobante.Estado.DATOS_INVALIDOS, "MONTO INVALIDO", -1);
        }

        // Codigo de 6 digitos
        if (!CODIGO_REGISTRO.equals(codigo)) {
            return new Comprobante(Comprobante.Estado.CODIGO_INCORRECTO, "CÓDIGO INCORRECTO", -1);
        }

        synchronized (repo) {
            // aqui se hace todo el proceso de validacion de la tarjeta
            Cuenta cuenta = repo.buscar(tarjeta);
            if (cuenta == null) {
                return new Comprobante(Comprobante.Estado.TARJETA_NO_ENCONTRADA, "TARJETA NO ENCONTRADA", -1);
            }

            // este try catch es para validar que el formato de la fecha de vencimiento sea correcto 
            // y que la tarjeta no este vencida
            try {
                YearMonth vence = YearMonth.parse(vencimiento, FORMATO);
                if (YearMonth.now().isAfter(vence)) {
                    return new Comprobante(Comprobante.Estado.TARJETA_VENCIDA, "TARJETA VENCIDA", -1);
                }
            } catch (DateTimeParseException e) {
                return new Comprobante(Comprobante.Estado.TARJETA_VENCIDA, "TARJETA VENCIDA", -1);
            }

            // este if es para validar que el cvv de la tarjeta sea correcto
            if (!cuenta.getCvv().equals(cvv)) {
                return new Comprobante(Comprobante.Estado.CVV_INCORRECTO, "CVV INCORRECTO", -1);
            }

            // este if es para validar que el monto de la compra no sea mayor al saldo de la cuenta
            if (monto > cuenta.getSaldo()) {
                return new Comprobante(Comprobante.Estado.FONDOS_INSUFICIENTES, "FONDOS INSUFICIENTES", cuenta.getSaldo());
            }

            cuenta.setSaldo(cuenta.getSaldo() - monto);
            try {
                repo.guardar();
            } catch (Exception e) {
                cuenta.setSaldo(cuenta.getSaldo() + monto); // revertir el saldo si no se pudo guardar
                throw new RemoteException("Error al guardar la transacción", e);
            }

            return new Comprobante(Comprobante.Estado.APROBADA,
                    "APROBADA - Compra de \"" + articulo + "\" por $" + monto,
                    cuenta.getSaldo());
        }
    }

    //este metodo es para consultar el saldo de la cuenta, se le pasa la tarjeta y el codigo de autenticacion
    @Override
    public double consultarSaldo(String tarjeta, String codigo) throws RemoteException {
        if (!CODIGO_REGISTRO.equals(codigo)) {
            throw new RemoteException("Código incorrecto");
        }
        synchronized (repo) {
            Cuenta cuenta = repo.buscar(tarjeta);
            if (cuenta == null) throw new RemoteException("Tarjeta no encontrada");
            return cuenta.getSaldo();
        }
    }
}