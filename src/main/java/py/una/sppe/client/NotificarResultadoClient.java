package py.una.sppe.client;

import org.json.simple.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Cliente UDP interno del Modulo de Notificaciones de SPPE hacia el
 * Modulo de Notificaciones de SIMBE (Sistema 1, otro repositorio).
 * Avisa el resultado de un pago ya procesado, de forma asincrona
 * (no bloquea ni afecta la respuesta TCP que ya se le dio a SIMBE).
 */
public class NotificarResultadoClient {

    private static final String SIMBE_HOST = System.getProperty("simbe.host", "localhost");
    private static final int SIMBE_PORT = Integer.parseInt(System.getProperty("simbe.notif.port", "6002"));
    private static final int TIMEOUT_MS = 2000;

    public void notificar(String idTransaccion, String estado, double monto, String fechaHora) {
        JSONObject notificacion = new JSONObject();
        notificacion.put("idTransaccion", idTransaccion);
        notificacion.put("estado", estado);
        notificacion.put("monto", monto);
        notificacion.put("fechaHora", fechaHora);
        notificacion.put("codigoResultado", "APROBADO".equals(estado) ? "COD-OK" : "COD-RECH");

        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress ip = InetAddress.getByName(SIMBE_HOST);

            byte[] data = notificacion.toJSONString().getBytes(StandardCharsets.UTF_8);
            System.out.println("[SPPE->SIMBE][UDP] Notificando a " + SIMBE_HOST + ":" + SIMBE_PORT + " -> " + notificacion.toJSONString());

            socket.send(new DatagramPacket(data, data.length, ip, SIMBE_PORT));
            socket.setSoTimeout(TIMEOUT_MS);

            byte[] buffer = new byte[1024];
            DatagramPacket respuestaPacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(respuestaPacket);
                String ack = new String(respuestaPacket.getData(), 0, respuestaPacket.getLength(), StandardCharsets.UTF_8);
                System.out.println("[SIMBE->SPPE][UDP] ACK recibido: " + ack);
            } catch (Exception timeout) {
                // UDP: la perdida del ACK es tolerable, no se reintenta ni se bloquea nada
                System.out.println("[SPPE][UDP] Sin ACK de SIMBE (tolerable en UDP, no afecta el pago ya procesado)");
            }

            socket.close();

        } catch (Exception e) {
            System.err.println("[SPPE][UDP] No se pudo notificar a SIMBE: " + e.getMessage());
        }
    }
}