package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionPostgres {
    private static final String URL = "jdbc:postgresql://localhost:5432/stock";
    private static final String USER = "postgres";
    private static final String PASSWORD = "antema";

    public static Connection ouvrir() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
