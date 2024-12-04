import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.*;
import java.util.List;

public class DataManage {
    static class Read {
        private JFrame frame;
        private JPanel panel;
        private JLabel successLabel;
        private JButton refreshButton;
        private JButton cancelButton;

        public Read() {
            GUI();
        }

        private void GUI() {
            frame = new JFrame("Получить данные");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

            panel = new JPanel(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);

            refreshButton = new JButton("Обновить");
            refreshButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        showData();
                        frame.revalidate();
                        frame.repaint();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Ошибка при получении данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }

                    successLabel.setText("Данные успешно получены!");
                }
            });
            panel.add(refreshButton, BorderLayout.SOUTH);

            successLabel = new JLabel();
            panel.add(successLabel, BorderLayout.WEST);

            cancelButton = new JButton("Отмена");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.dispose();
                    new App();
                }
            });
            panel.add(cancelButton, BorderLayout.EAST);

            frame.setVisible(true);
        }

        public void showData() throws SQLException {
            java.util.List<Object[]> data = getData();
            String[] columnNames = {"ID", "Name"};

            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Таблица пуста!", "Инфо", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Object[][] dataArray = data.toArray(new Object[0][0]);
            JTable table = new JTable(dataArray, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            panel.add(scrollPane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        }

        public java.util.List<Object[]> getData() throws SQLException {
            List<Object[]> rows = new ArrayList<>();

            try (Connection connection = DB.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT * FROM client")) {

                while (resultSet.next()) {
                    Object[] row = {resultSet.getInt("ID"), resultSet.getString("Name")};
                    rows.add(row);
                }
            }
            return rows;
        }
    }
}
