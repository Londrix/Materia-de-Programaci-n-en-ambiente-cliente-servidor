package Modelos;

//se importan las clases necesarias
import java.io.Serializable;

public class Cuenta implements Serializable {
    //este private es para que la clase Cuenta pueda ser serializada y enviada a traves de la red
    private static final long serialVersionUID = 1L;

    private final String tarjeta;
    private final String vencimiento;
    private final String cvv;
    private double saldo;

    //este constructor es para crear una cuenta con la tarjeta, vencimiento, cvv y saldo
    public Cuenta(String tarjeta, String vencimiento, String cvv, double saldo) {
        this.tarjeta = tarjeta;
        this.vencimiento = vencimiento;
        this.cvv = cvv;
        this.saldo = saldo;
    }

    //finalmente, estos public son para que los atributos de la clase Cuenta puedan ser accedidos desde otras clases
    public String getTarjeta()     { return tarjeta; }
    public String getVencimiento() { return vencimiento; }
    public String getCvv()         { return cvv; }
    public double getSaldo()       { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
}