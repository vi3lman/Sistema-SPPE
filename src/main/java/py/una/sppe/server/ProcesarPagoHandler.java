package py.una.sppe.server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import py.una.bd.TransaccionDAO;
import py.una.sppe.client.NotificarResultadoClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import py.una.sppe.client.NotificarResultadoClient;
public class ProcesarPagoHandler implements Runnable {

    private final Socket socketCliente;
    private final TransaccionDAO transaccionDAO = new TransaccionDAO();
    private final NotificarResultadoClient notificarResultadoClient = new NotificarResultadoClient(); // NUEVO
    public ProcesarPagoHandler(Socket socketCliente) {
        this.socketCliente = socketCliente;
    }

    @Override
    public void run() {
        try (Socket socket = socketCliente;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String lineaJson = in.readLine();
            System.out.println("[SPPE] Solicitud de pago recibida: " + lineaJson);

            JSONObject solicitud = (JSONObject) new JSONParser().parse(lineaJson);
            JSONObject respuesta = procesarPago(solicitud);

            out.println(respuesta.toJSONString());
            System.out.println("[SPPE] Respuesta de pago enviada: " + respuesta.toJSONString());

        } catch (Exception e) {
            System.err.println("[SPPE] Error atendiendo solicitud de pago: " + e.getMessage());
        }
    }

    private JSONObject procesarPago(JSONObject solicitud) {
        String idCliente = (String) solicitud.get("idCliente");
        String idMedioPago = (String) solicitud.get("idMedioPago");
        String concepto = (String) solicitud.get("concepto");
        String fechaHora = (String) solicitud.get("fechaHora");
        double monto = ((Number) solicitud.get("monto")).doubleValue();

        String idTransaccion = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        JSONObject respuesta = new JSONObject();
        respuesta.put("idTransaccion", idTransaccion);
        respuesta.put("fechaHora", fechaHora);

        String estado;
        String motivo;
        double montoProcesado;

        if (monto <= 0) {
            estado = "RECHAZADO";
            motivo = "Monto invalido";
            montoProcesado = 0.0;
        } else if (idMedioPago == null || idMedioPago.trim().isEmpty()) {
            estado = "RECHAZADO";
            motivo = "Medio de pago no informado";
            montoProcesado = 0.0;
        } else {
            estado = "APROBADO";
            motivo = "Pago procesado correctamente";
            montoProcesado = monto;
        }

        respuesta.put("estado", estado);
        respuesta.put("montoProcesado", montoProcesado);
        respuesta.put("motivo", motivo);

        try {
            transaccionDAO.registrar(idTransaccion, idCliente, idMedioPago, monto, concepto, estado, fechaHora);
        } catch (Exception e) {
            System.err.println("[SPPE] No se pudo persistir la transaccion: " + e.getMessage());
        }
        double montoFinal = montoProcesado;
        new Thread(() -> notificarResultadoClient.notificar(idTransaccion, estado, montoFinal, fechaHora)).start();
        return respuesta;
    }
   
}