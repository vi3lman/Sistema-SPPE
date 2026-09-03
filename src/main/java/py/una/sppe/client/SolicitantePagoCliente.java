package py.una.sppe.client;

import org.json.simple.JSONObject;
import py.una.sppe.server.SppeServidorPagos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Simula un sistema externo cualquiera (no necesariamente SIMBE) que
 * invoca por TCP el Servicio 1 de SPPE: Procesar pago. Sirve para
 * probar este repositorio de forma completamente aislada.
 */
public class SolicitantePagoCliente {

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";

        Scanner scanner = new Scanner(System.in, "UTF-8");
        System.out.print("idCliente (ej: TARJETA-001): ");
        String idCliente = limpiar(scanner.nextLine());
        System.out.print("idMedioPago (ej: TARJETA_SIMBE): ");
        String idMedioPago = limpiar(scanner.nextLine());
        System.out.print("monto (ej: 5000): ");
        double monto = Double.parseDouble(limpiar(scanner.nextLine()));

        JSONObject solicitud = new JSONObject();
        solicitud.put("idCliente", idCliente);
        solicitud.put("idMedioPago", idMedioPago);
        solicitud.put("monto", monto);
        solicitud.put("concepto", "Prueba directa a SPPE");
        solicitud.put("fechaHora", LocalDateTime.now().toString());

        try (Socket socket = new Socket(host, SppeServidorPagos.PUERTO);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            System.out.println("[Solicitante] Enviando: " + solicitud.toJSONString());
            out.println(solicitud.toJSONString());

            String respuesta = in.readLine();
            System.out.println("[Solicitante] Respuesta de SPPE: " + respuesta);
        }
    }

    private static String limpiar(String texto) {
        return texto.replace("\uFEFF", "").trim();
    }
}