import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    public static Connection getConnection() {
        String url = "jdbc:sqlite:database";
        try {
            return DriverManager.getConnection(url);
        }
        catch (SQLException e) {
            System.err.println("Ошибка создания подключения: " + e.getMessage());
        }
        return null;
    }
}
