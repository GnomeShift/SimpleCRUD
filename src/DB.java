import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:database";
        return DriverManager.getConnection(url);
    }
}
