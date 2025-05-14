import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClienteConversor extends JFrame {
    
    // Configuración del servidor
    private static final String HOST = "localhost";
    private static final int PUERTO = 9876;
    
    // Componentes de la interfaz gráfica
    private JTextField campoKilometros;
    private JLabel etiquetaResultado;
    private JLabel etiquetaEstado;
    
    // Componentes de comunicación
    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    
    public ClienteConversor() {
        // Configuración de la ventana principal
        setTitle("Cliente Conversor de Kilómetros a Metros");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la ventana
        setResizable(false);
        
        // Crear panel principal con un poco de espacio en los bordes
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel para la entrada de datos
        JPanel panelEntrada = new JPanel();
        panelEntrada.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JLabel etiquetaKilometros = new JLabel("Kilómetros:");
        campoKilometros = new JTextField(10);
        
        panelEntrada.add(etiquetaKilometros);
        panelEntrada.add(campoKilometros);
        
        // Botón de conversión
        JButton botonConvertir = new JButton("Convertir a Metros");
        botonConvertir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarSolicitudAlServidor();
            }
        });
        
        // Panel para mostrar el resultado
        JPanel panelResultado = new JPanel();
        panelResultado.setLayout(new FlowLayout(FlowLayout.CENTER));
        panelResultado.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        etiquetaResultado = new JLabel("El resultado aparecerá aquí");
        etiquetaResultado.setFont(new Font("Arial", Font.BOLD, 14));
        panelResultado.add(etiquetaResultado);
        
        // Etiqueta de estado de conexión
        etiquetaEstado = new JLabel("Estado: No conectado", JLabel.LEFT);
        etiquetaEstado.setForeground(Color.RED);
        
        // Botón para conectar/desconectar
        JButton botonConectar = new JButton("Conectar al servidor");
        botonConectar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (socket == null || socket.isClosed()) {
                    conectarAlServidor();
                    if (socket != null && !socket.isClosed()) {
                        botonConectar.setText("Desconectar");
                    }
                } else {
                    desconectarDelServidor();
                    botonConectar.setText("Conectar al servidor");
                }
            }
        });
        
        // Panel para botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panelBotones.add(botonConvertir);
        panelBotones.add(botonConectar);
        
        // Añadir componentes al panel principal
        panel.add(new JLabel("Ingrese la cantidad de kilómetros:", JLabel.CENTER), BorderLayout.NORTH);
        panel.add(panelEntrada, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        // Añadir el panel de resultado y estado
        JPanel contenedorSur = new JPanel();
        contenedorSur.setLayout(new BorderLayout());
        contenedorSur.add(panelResultado, BorderLayout.CENTER);
        contenedorSur.add(etiquetaEstado, BorderLayout.SOUTH);
        
        // Añadir panel principal a la ventana
        JPanel contenedorPrincipal = new JPanel();
        contenedorPrincipal.setLayout(new BorderLayout());
        contenedorPrincipal.add(panel, BorderLayout.CENTER);
        contenedorPrincipal.add(contenedorSur, BorderLayout.SOUTH);
        add(contenedorPrincipal);
        
        // También permitir presionar Enter para convertir
        campoKilometros.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enviarSolicitudAlServidor();
            }
        });
        
        // Intentar conectar al servidor al iniciar
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                conectarAlServidor();
                if (socket != null && !socket.isClosed()) {
                    botonConectar.setText("Desconectar");
                }
            }
        });
        
        // Asegurar que se cierre la conexión al cerrar la ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                desconectarDelServidor();
            }
        });
    }
    
    private void conectarAlServidor() {
        try {
            socket = new Socket(HOST, PUERTO);
            salida = new PrintWriter(socket.getOutputStream(), true);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            etiquetaEstado.setText("Estado: Conectado al servidor " + HOST + ":" + PUERTO);
            etiquetaEstado.setForeground(new Color(0, 128, 0)); // Verde
            
        } catch (IOException e) {
            etiquetaEstado.setText("Estado: Error al conectar - " + e.getMessage());
            etiquetaEstado.setForeground(Color.RED);
            
            // Mostrar un mensaje de error
            JOptionPane.showMessageDialog(this, 
                "No se pudo conectar al servidor.\nAsegúrese de que el servidor esté en ejecución.",
                "Error de conexión", 
                JOptionPane.ERROR_MESSAGE);
            
            socket = null;
        }
    }
    
    private void desconectarDelServidor() {
        try {
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null) socket.close();
            
            etiquetaEstado.setText("Estado: Desconectado");
            etiquetaEstado.setForeground(Color.RED);
            
        } catch (IOException e) {
            etiquetaEstado.setText("Estado: Error al desconectar - " + e.getMessage());
        }
    }
    
    private void enviarSolicitudAlServidor() {
        // Verificar que estemos conectados al servidor
        if (socket == null || socket.isClosed()) {
            etiquetaResultado.setText("No hay conexión con el servidor");
            return;
        }
        
        try {
            // Obtener el valor de kilómetros
            String textoKilometros = campoKilometros.getText().trim();
            
            // Verificar que no esté vacío
            if (textoKilometros.isEmpty()) {
                etiquetaResultado.setText("Por favor, ingrese un valor");
                return;
            }
            
            // Enviar el valor al servidor
            salida.println(textoKilometros);
            
            // Recibir y mostrar la respuesta
            String respuesta = entrada.readLine();
            etiquetaResultado.setText(respuesta);
            
        } catch (IOException e) {
            etiquetaResultado.setText("Error en la comunicación: " + e.getMessage());
            etiquetaEstado.setText("Estado: Error - " + e.getMessage());
            etiquetaEstado.setForeground(Color.RED);
        }
    }
    
    public static void main(String[] args) {
        // Ejecutar la aplicación en el Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClienteConversor().setVisible(true);
            }
        });
    }
}
