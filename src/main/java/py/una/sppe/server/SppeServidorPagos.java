package py.una.sppe.server;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor TCP del SPPE. Representa el Modulo de Cobros y expone el
 * Servicio 1: Procesar pago.
 *
 * Este es el servidor que espera SppeClient del repositorio SIMBE:
 * por defecto escucha en el puerto 6001 (configurable via -Dsppe.port).
 */
public class SppeServidorPagos {

    public static final int PUERTO = Integer.parseInt(System.getProperty("sppe.port", "6001"));

    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("[SPPE] Modulo de Cobros escuchando en el puerto " + PUERTO);

            while (true) {
                Socket socketCliente = serverSocket.accept();
                System.out.println("[SPPE] Conexion aceptada desde " + socketCliente.getRemoteSocketAddress());
                new Thread(new ProcesarPagoHandler(socketCliente)).start();
            }
        }
    }
}