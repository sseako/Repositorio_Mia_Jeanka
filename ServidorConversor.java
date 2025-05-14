import java.io.*;
import java.net.*;

public class ServidorConversor {
    
    // Puerto en el que escuchará el servidor
    private static final int PUERTO = 9876;
    
    public static void main(String[] args) {
        try {
            // Crear el socket del servidor
            ServerSocket servidorSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado en el puerto " + PUERTO);
            System.out.println("Esperando conexiones...");
            
            // El servidor se mantiene en ejecución indefinidamente
            while (true) {
                // Esperar a que un cliente se conecte
                Socket clienteSocket = servidorSocket.accept();
                System.out.println("Cliente conectado desde: " + clienteSocket.getInetAddress().getHostAddress());
                
                // Crear hilos para manejar múltiples clientes
                new ManejadorCliente(clienteSocket).start();
            }
            
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Clase interna que maneja las comunicaciones con un cliente específico
    private static class ManejadorCliente extends Thread {
        private Socket clienteSocket;
        private BufferedReader entrada;
        private PrintWriter salida;
        
        public ManejadorCliente(Socket socket) {
            this.clienteSocket = socket;
        }
        
        public void run() {
            try {
                // Configurar los streams de entrada y salida
                entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                salida = new PrintWriter(clienteSocket.getOutputStream(), true);
                
                String lineaEntrada;
                
                // Procesar las solicitudes del cliente
                while ((lineaEntrada = entrada.readLine()) != null) {
                    try {
                        // Convertir la entrada a doble precisión
                        double kilometros = Double.parseDouble(lineaEntrada);
                        
                        // Realizar la conversión
                        double metros = kilometros * 1000;
                        
                        // Enviar el resultado al cliente
                        salida.println(String.format("%.2f kilómetros equivalen a %.2f metros", kilometros, metros));
                        
                        System.out.println("Conversión realizada: " + kilometros + " km = " + metros + " m");
                        
                    } catch (NumberFormatException e) {
                        // Enviar mensaje de error si la entrada no es un número válido
                        salida.println("ERROR: Por favor, ingrese un número válido");
                    }
                }
                
                // Cerrar conexión
                entrada.close();
                salida.close();
                clienteSocket.close();
                System.out.println("Cliente desconectado");
                
            } catch (IOException e) {
                System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
            }
        }
    }
}
