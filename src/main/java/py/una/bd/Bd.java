package py.una.bd;

import java.sql.Connection;
import java.sql.DriverManager;

public class Bd {

    private static final String URL = System.getProperty("sppe.db.url", "jdbc:postgresql://localhost:5432/sppe");
    private static final String USER = System.getProperty("sppe.db.user", "postgres");
    private static final String PASSWORD = System.getProperty("sppe.db.password", "admin");

    public static Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}