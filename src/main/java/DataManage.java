import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.*;
import java.util.List;

class DataManage {
    static class Create {
        private JFrame frame;
        private JPanel panel;
        private JButton submitButton;
        private JButton cancelButton;
        private JButton addRowButton;
        private JTable table;
        private DefaultTableModel tableModel;
        private Vector<String> columnNames = new Vector<>();

        public Create() {
            GUI();
        }

        private void GUI() {
            frame = new JFrame("Добавить данные");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

            panel = new JPanel(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);

            columnNames.add("Name");
            tableModel = new DefaultTableModel(columnNames, 0);
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);

            addRowButton = new JButton("Добавить строку");
            addRowButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tableModel.addRow(new Object[]{});
                }
            });
            panel.add(addRowButton, BorderLayout.NORTH);

            submitButton = new JButton("Отправить данные");
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addData();
                }
            });
            panel.add(submitButton, BorderLayout.SOUTH);

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

        public void addData() {
            int rowCount = tableModel.getRowCount();
            int columnCount = columnNames.size();

            if (rowCount == 0) {
                JOptionPane.showMessageDialog(frame, "Таблица пуста!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection connection = DB.getConnection()) {
                StringBuilder sql = buildSql(columnCount);

                try {
                    assert connection != null;
                    try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                        connection.setAutoCommit(false);

                        for (int i = 0; i < rowCount; i++) {
                            for (int j = 0; j < columnCount; j++) {
                                Object value = tableModel.getValueAt(i, j);
                                String strValue = (value == null) ? "" : value.toString().trim();

                                if (strValue.isEmpty()) {
                                    JOptionPane.showMessageDialog(frame, "Добавление пустой строки!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                    connection.rollback();
                                    return;
                                }
                                statement.setString(j + 1, strValue);
                            }
                            statement.addBatch();
                        }

                        statement.executeBatch();
                        connection.commit();
                        JOptionPane.showMessageDialog(frame, "Данные успешно отправлены!");
                    }
                }
                catch (SQLException e) {
                    connection.rollback();
                    JOptionPane.showMessageDialog(frame, "Ошибка отправки: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Ошибка соединения: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        public StringBuilder buildSql(int columnCount) {
            StringBuilder sql = new StringBuilder("INSERT INTO client (");
            for (int i = 0; i < columnCount; i++) {
                sql.append(columnNames.get(i));
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
    }

    static class Read {
        private JFrame frame;
        private JPanel panel;
        private JLabel successLabel;
        private JButton refreshButton;
        private JButton cancelButton;
        private DefaultTableModel tableModel;

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
                    showData();
                    frame.revalidate();
                    frame.repaint();
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

        public void showData() {
            List<Object[]> data = getData();
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

        public List<Object[]> getData() {
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
                System.err.println("Ошибка: " + e.getMessage());
            }
            return rows;
        }
    }

    static class Delete {
        private JFrame frame;
        private JPanel panel;
        private JButton submitButton;
        private JButton cancelButton;
        private JTable table;
        private DefaultTableModel tableModel;
        private Vector<String> columnNames = new Vector<>(Arrays.asList("ID", "Name"));

        public Delete() {
            GUI();
        }

        private void GUI() {
            frame = new JFrame("Удалить данные");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

            panel = new JPanel(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);

            tableModel = new DefaultTableModel(columnNames, 0);
            table = new JTable(tableModel);

            submitButton = new JButton("Удалить данные");
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteData();
                }
            });
            panel.add(submitButton, BorderLayout.SOUTH);

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

        public void deleteData() {
            int[] selectedRows = table.getSelectedRows();

            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(frame, "Выберите строки!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(frame, "Таблица пуста!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection connection = DB.getConnection()) {
                String sql = "DELETE FROM client WHERE ID = ?";

                try {
                    assert connection != null;
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        connection.setAutoCommit(false);

                        for (int row : selectedRows) {
                            Object value = tableModel.getValueAt(row, 0);
                            String idString = (value == null) ? "" : value.toString().trim();

                            if (idString.isEmpty()) {
                                JOptionPane.showMessageDialog(frame, "Не выбран ID!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            int id = Integer.parseInt(idString);
                            statement.setInt(1, id);
                            statement.addBatch();
                        }

                        statement.executeBatch();
                        connection.commit();

                        for (int i = selectedRows.length - 1; i >= 0; i--) {
                            tableModel.removeRow(selectedRows[i]);
                        }

                        JOptionPane.showMessageDialog(frame, "Данные успешно удалены!", "Инфо", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                catch (SQLException | NumberFormatException e) {
                    connection.rollback();
                    JOptionPane.showMessageDialog(frame, "Ошибка удаления: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Ошибка создания соединения: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }

        public void showData() {
            List<Object[]> data = getData();
            String[] columnNames = {"ID", "Name"};

            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Таблица пуста!", "Инфо", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Object[][] dataArray = data.toArray(new Object[0][0]);
            table = new JTable(dataArray, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            panel.add(scrollPane, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        }

        public List<Object[]> getData() {
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
                System.err.println("Ошибка: " + e.getMessage());
            }
            return rows;
        }
    }
}
