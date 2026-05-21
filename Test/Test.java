package Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dao.ConnexionPostgres;

public class Test {

    public static void afficherArticles() throws SQLException {
        String sql = "SELECT * FROM article";
        Connection con = ConnexionPostgres.ouvrir();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println("Nom: " + rs.getString("nom"));
        }
    }

    public static void main(String[] args) throws SQLException {
        afficherArticles();
    }
}
