import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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

    public static List<Object[]> getData() {
        List<Object[]> rows = new ArrayList<>();

        try (Connection connection = DB.getConnection()) {
            assert connection != null;
            try (Statement statement = connection.createStatement();
                 ResultSet set = statement.executeQuery("SELECT * FROM client")) {

                while (set.next()) {
                    Object[] row = {set.getInt("ID"), set.getString("Name")};
                    rows.add(row);
                }
            }
        }
        catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка получения данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
        return rows;
    }

    public static StringBuilder getInsertSql(int columnCount) {
        StringBuilder sql = new StringBuilder("INSERT INTO client (");
        for (int i = 0; i < columnCount; i++) {
            sql.append(DataManage.Create.columnNames.get(i));
            if (i < columnCount - 1) {
                sql.append(", ");
            }
        }
        sql.append(") VALUES (");

        for (int i = 0; i < columnCount; i++) {
            sql.append("?");
            if (i < columnCount - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");
        return sql;
    }

    public static StringBuilder getUpdateSql() {
        Vector<String> columnNames = DataManage.Read.columnNames;
        StringBuilder sql = new StringBuilder("UPDATE client SET ");
        boolean firstUpdate = true;

        for (int i = 0; i < columnNames.size(); i++) {
            if (i > 0) {
                if (!firstUpdate) {
                    sql.append(", ");
                }
                sql.append(columnNames.get(i)).append(" = ?");
                firstUpdate = false;
            }
        }
        sql.append(" WHERE id = ?");
        return sql;
    }
}
