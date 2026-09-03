package py.una.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class TransaccionDAO {

    public void registrar(String idTransaccion, String idCliente, String idMedioPago,
                           double monto, String concepto, String estado, String fechaHora) throws Exception {
        try (Connection conn = Bd.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO transacciones " +
                "(id_transaccion, id_cliente, id_medio_pago, monto, concepto, estado, fecha_hora) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, idTransaccion);
            ps.setString(2, idCliente);
            ps.setString(3, idMedioPago);
            ps.setDouble(4, monto);
            ps.setString(5, concepto);
            ps.setString(6, estado);
            ps.setString(7, fechaHora);
            ps.executeUpdate();
        }
    }
}