package Cliente;

//se importan las clases necesarias
import Modelos.Comprobante;
import Servidor.CajeroRemoto;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClienteRMI extends JFrame {
    //se definen las constantes para el puerto del registry y el nombre del servicio
    private static final int PUERTO_REGISTRY = 1099;
    private static final String NOMBRE_SERVICIO = "CajeroRemoto";

    //se definen los componentes de la interfaz grafica, como los campos de texto, botones y etiquetas
    private final JTextField txtIp = new JTextField("localhost");
    private final JTextField txtTarjeta = new JTextField();
    private final JTextField txtVencimiento = new JTextField();
    private final JPasswordField txtCvv = new JPasswordField();
    private final JTextField txtArticulo = new JTextField();
    private final JTextField txtCantidad = new JTextField();
    private final JPasswordField txtCodigo = new JPasswordField();

    //se definen los botones de comprar y consultar saldo, y las etiquetas de resultado y saldo
    private final JButton btnComprar = new JButton("Comprar");
    private final JButton btnConsultarSaldo = new JButton("Consultar saldo");
    private final JLabel lblResultado = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblSaldo = new JLabel("Saldo: --", SwingConstants.CENTER);

    private CajeroRemoto servicio; // el stub del objeto remoto

    //este constructor es para crear la interfaz grafica del cliente RMI
    public ClienteRMI(String titulo) {
        super(titulo);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        agregarFila(form, 0, "IP del servidor:", txtIp);
        agregarFila(form, 1, "Tarjeta (16 dígitos):", txtTarjeta);
        agregarFila(form, 2, "Vencimiento (MM/AA):", txtVencimiento);
        agregarFila(form, 3, "CVV:", txtCvv);
        agregarFila(form, 4, "Artículo a comprar:", txtArticulo);
        agregarFila(form, 5, "Cantidad (monto):", txtCantidad);
        agregarFila(form, 6, "Código de 6 dígitos:", txtCodigo);

        lblResultado.setFont(lblResultado.getFont().deriveFont(Font.BOLD, 15f));
        lblResultado.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        lblSaldo.setFont(lblSaldo.getFont().deriveFont(Font.BOLD, 14f));

        JPanel botones = new JPanel(new GridLayout(1, 2, 8, 0));
        botones.add(btnComprar);
        botones.add(btnConsultarSaldo);

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));
        abajo.add(botones, BorderLayout.NORTH);
        abajo.add(lblResultado, BorderLayout.CENTER);
        abajo.add(lblSaldo, BorderLayout.SOUTH);

        add(form, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);

        btnComprar.addActionListener(e -> comprar());
        btnConsultarSaldo.addActionListener(e -> consultarSaldo());
        getRootPane().setDefaultButton(btnComprar);
    }

    private void agregarFila(JPanel p, int fila, String etiqueta, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = fila;
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        p.add(new JLabel(etiqueta), g);

        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        campo.setPreferredSize(new Dimension(220, 26));
        p.add(campo, g);
    }

    // Busca el objeto remoto en el Registry. Se llama una sola vez por operación
    // (o se podría cachear tras el primer éxito; aquí se busca cada vez por
    // simplicidad y para tolerar que el servidor se reinicie).
    private CajeroRemoto obtenerServicio() throws Exception {
        String ip = txtIp.getText().trim();
        Registry registry = LocateRegistry.getRegistry(ip, PUERTO_REGISTRY);
        return (CajeroRemoto) registry.lookup(NOMBRE_SERVICIO);
    }

    private void comprar() {
        final String tarjeta = txtTarjeta.getText().trim().replace(" ", "");
        final String venc = txtVencimiento.getText().trim();
        final String cvv = new String(txtCvv.getPassword()).trim();
        final String articulo = txtArticulo.getText().trim();
        final String cantidadTxt = txtCantidad.getText().trim().replace(",", ".");
        final String codigo = new String(txtCodigo.getPassword()).trim();

        String error = validar(tarjeta, venc, cvv, articulo, cantidadTxt, codigo);
        if (error != null) {
            mostrarResultado(error, new Color(200, 110, 0));
            return;
        }
        final double monto = Double.parseDouble(cantidadTxt);

        btnComprar.setEnabled(false);
        mostrarResultado("Enviando...", Color.GRAY);

        // Se utiliza SwingWorker para ejecutar la operación de compra en un hilo 
        // separado y no bloquear la interfaz gráfica
        new SwingWorker<Comprobante, Void>() {
            @Override
            protected Comprobante doInBackground() throws Exception {
                CajeroRemoto s = obtenerServicio();
                return s.autorizarCompra(codigo, tarjeta, venc, cvv, articulo, monto);
            }

            @Override
            protected void done() {
                btnComprar.setEnabled(true);
                try {
                    Comprobante c = get();
                    Color color = c.fueAprobada() ? new Color(0, 130, 0) : Color.RED;
                    mostrarResultado(c.getMensaje(), color);
                    if (c.getSaldoRestante() >= 0) {
                        lblSaldo.setText(String.format("Saldo: $%.2f", c.getSaldoRestante()));
                    }
                    txtCvv.setText("");
                    txtCodigo.setText("");
                } catch (Exception e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    mostrarResultado("Error de conexión: " + causa.getMessage(), Color.RED);
                }
            }
        }.execute();
    }

    // Este método consulta el saldo actual de la tarjeta sin realizar una compra
    private void consultarSaldo() {
        final String tarjeta = txtTarjeta.getText().trim().replace(" ", "");
        final String codigo = new String(txtCodigo.getPassword()).trim();

        if (!tarjeta.matches("\\d{16}")) {
            mostrarResultado("La tarjeta debe tener 16 dígitos.", new Color(200, 110, 0));
            return;
        }
        if (!codigo.matches("\\d{6}")) {
            mostrarResultado("El código debe tener 6 dígitos.", new Color(200, 110, 0));
            return;
        }

        btnConsultarSaldo.setEnabled(false);

        new SwingWorker<Double, Void>() {
            @Override
            protected Double doInBackground() throws Exception {
                CajeroRemoto s = obtenerServicio();
                return s.consultarSaldo(tarjeta, codigo);
            }

            @Override
            protected void done() {
                btnConsultarSaldo.setEnabled(true);
                try {
                    double saldo = get();
                    lblSaldo.setText(String.format("Saldo: $%.2f", saldo));
                    mostrarResultado("Saldo consultado correctamente", new Color(0, 130, 0));
                } catch (Exception e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    mostrarResultado("No se pudo consultar: " + causa.getMessage(), Color.RED);
                }
            }
        }.execute();
    }

    // Este método valida los datos ingresados por el usuario antes de enviarlos al servidor
    private String validar(String tarjeta, String venc, String cvv,
                            String articulo, String cantidad, String codigo) {
        if (!tarjeta.matches("\\d{16}")) return "La tarjeta debe tener 16 dígitos.";
        if (!venc.matches("(0[1-9]|1[0-2])/\\d{2}")) return "Vencimiento inválido (usa MM/AA).";
        if (!cvv.matches("\\d{3,4}")) return "El CVV debe tener 3 o 4 dígitos.";
        if (articulo.isEmpty()) return "Escribe el artículo a comprar.";
        if (!cantidad.matches("\\d+(\\.\\d{1,2})?") || Double.parseDouble(cantidad) <= 0)
            return "La cantidad debe ser un número mayor a 0.";
        if (!codigo.matches("\\d{6}")) return "El código debe tener 6 dígitos.";
        return null;
    }

    // Este método muestra el resultado de la operación en la etiqueta lblResultado con el color especificado
    private void mostrarResultado(String texto, Color color) {
        lblResultado.setForeground(color);
        lblResultado.setText(texto);
    }

    // El método main crea una instancia de ClienteRMI y la muestra en pantalla
    public static void main(String[] args) {
        // El título permite distinguir visualmente al Cliente A del Cliente B
        String titulo = args.length > 0 ? "Cliente RMI - " + args[0] : "Cliente RMI";

        SwingUtilities.invokeLater(() -> {
            ClienteRMI c = new ClienteRMI(titulo);
            c.pack();
            c.setLocationByPlatform(true);
            c.setVisible(true);
        });
    }
}