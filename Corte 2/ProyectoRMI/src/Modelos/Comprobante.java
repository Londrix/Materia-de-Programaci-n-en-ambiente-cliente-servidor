package Modelos;

//se importan las clases necesarias para la clase Comprobante
import java.io.Serializable;
import java.time.LocalDateTime;

public class Comprobante implements Serializable {
    //este private es para que la clase Comprobante pueda ser serializada y enviada a traves de la red
    private static final long serialVersionUID = 1L;

    //este enum es para definir los posibles estados de la transaccion, como aprobada, 
    // fondos insuficientes, codigo incorrecto, tarjeta no encontrada, tarjeta vencida, 
    // cvv incorrecto y datos invalidos
    public enum Estado {
        APROBADA, FONDOS_INSUFICIENTES, CODIGO_INCORRECTO,
        TARJETA_NO_ENCONTRADA, TARJETA_VENCIDA, CVV_INCORRECTO, DATOS_INVALIDOS
    }

    //estos private son para que los atributos de la clase Comprobante solo puedan ser accedidos desde esta clase
    private final Estado estado;
    private final String mensaje;
    private final double saldoRestante; // -1 si la operación no fue aprobada
    private final LocalDateTime fecha;

    //este constructor es para crear un comprobante con el estado, mensaje y saldo restante
    public Comprobante(Estado estado, String mensaje, double saldoRestante) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.saldoRestante = saldoRestante;
        this.fecha = LocalDateTime.now();
    }

    //finalmente, estos public son para que los atributos de la clase Comprobante puedan ser accedidos desde otras clases
    public Estado getEstado()         { return estado; }
    public String getMensaje()        { return mensaje; }
    public double getSaldoRestante()  { return saldoRestante; }
    public LocalDateTime getFecha()   { return fecha; }

    public boolean fueAprobada() { return estado == Estado.APROBADA; }
}