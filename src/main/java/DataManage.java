import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;
import static javax.swing.SortOrder.*;

class DataManage {

    public static void showData(JFrame frame, DefaultTableModel tableModel, SortOrder sortOrder) {
        List<Object[]> data = DB.getData();

        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Таблица пуста!", "Инфо", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Object[]> sortedData = sortData(data, sortOrder);
        tableModel.setRowCount(0);

        for (Object[] row : sortedData) {
            tableModel.addRow(row);
        }
    }

    public static List<Object[]> sortData(List<Object[]> data, SortOrder sortOrder) {
        if (data.isEmpty()) {
            return data;
        }

        List<Object[]> sortedData = new ArrayList<>(data);
        switch (sortOrder) {
            case ASCENDING:
                sortedData.sort(Comparator.comparing(row -> row[1].toString()));
                break;
            case DESCENDING:
                sortedData.sort(Comparator.comparing(row -> row[1].toString(), Comparator.reverseOrder()));
                break;
            case UNSORTED:
            default:
                break;
        }
        return sortedData;
    }

    static class Create {
        private JFrame frame;
        private DefaultTableModel tableModel;
        public static final Vector<String> columnNames = new Vector<>(List.of("Name"));

        public Create() {
            GUI();
        }

        private void GUI() {
            frame = new JFrame("Добавить данные");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

            JPanel panel = new JPanel(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);

            tableModel = new DefaultTableModel(columnNames, 0);
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);

            JButton addRowButton = new JButton("Добавить строку");
            addRowButton.addActionListener(e -> {
                tableModel.addRow(new Object[]{});
                tableModel.fireTableDataChanged();
            });
            panel.add(addRowButton, BorderLayout.NORTH);

            JButton submitButton = new JButton("Отправить данные");
            submitButton.addActionListener(e -> addData());
            panel.add(submitButton, BorderLayout.SOUTH);

            JButton cancelButton = new JButton("Отмена");
            cancelButton.addActionListener(e -> {
                frame.dispose();
                new App();
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
                StringBuilder sql = DB.getInsertSql();
                try {
                    assert connection != null;
                    try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                        connection.setAutoCommit(false);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        for (int i = 0; i < rowCount; i++) {
                            int paramIndex = 1;
                            for (int j = 0; j < columnCount; j++) {
                                Object value = tableModel.getValueAt(i, j);
                                String strValue = (value == null) ? "" : value.toString().trim();

                                if (strValue.isEmpty()) {
                                    JOptionPane.showMessageDialog(frame, "Обнаружена пустая строка!\n" +
                                            "Убедитесь, что Вы нажали Enter после ввода данных в ячейку!", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                                    return;
                                }
                                else {
                                    statement.setObject(paramIndex++, value);
                                }
                            }
                            String formattedDate = sdf.format(new Date());
                            statement.setString(paramIndex, formattedDate);
                            statement.addBatch();
                        }

                        statement.executeBatch();
                        connection.commit();
                        JOptionPane.showMessageDialog(frame, "Данные успешно отправлены!", "Инфо", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                catch (SQLException e) {
                    try {
                        connection.rollback();
                    }
                    catch (SQLException rollbackException) {
                        JOptionPane.showMessageDialog(frame, "Ошибка отката транзакции: " + rollbackException.getMessage(), "Критическая ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                    JOptionPane.showMessageDialog(frame, "Ошибка отправки данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Ошибка создания соединения: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static class Read {
        private JFrame frame;
        private DefaultTableModel tableModel;
        private final Map<Integer, Map<Integer, Object>> changedCells = new HashMap<>();
        public static final Vector<String> columnNames = new Vector<>(Arrays.asList("ID", "Name", "CreatedAt", "UpdatedAt"));
        private SortOrder currentOrder = UNSORTED;

        public Read() {
            GUI();
            showData(frame, tableModel, UNSORTED);
        }

        private void GUI() {
            frame = new JFrame("Получить данные");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

            JPanel panel = new JPanel(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);

            JPanel sortPanel = new JPanel();
            sortPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

            JRadioButton ascendingButton = new JRadioButton("По возрастанию");
            ascendingButton.addActionListener(e -> {
                currentOrder = ASCENDING;
                showData(frame, tableModel, currentOrder);
            });

            JRadioButton descendingButton = new JRadioButton("По убыванию");
            descendingButton.addActionListener(e -> {
                currentOrder = DESCENDING;
                showData(frame, tableModel, currentOrder);
            });
            JRadioButton noSort = new JRadioButton("Без сортировки", true);
            noSort.addActionListener(e -> {
                currentOrder = UNSORTED;
                showData(frame, tableModel, currentOrder);
            });

            ButtonGroup sortGroup = new ButtonGroup();
            sortGroup.add(ascendingButton);
            sortGroup.add(descendingButton);
            sortGroup.add(noSort);

            sortPanel.add(ascendingButton);
            sortPanel.add(descendingButton);
            sortPanel.add(noSort);

            panel.add(sortPanel, BorderLayout.NORTH);
            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column > 0 && column < columnNames.size() - 2;
                }
            };
            tableModel.addTableModelListener(listener -> {
                if (listener.getType() == TableModelEvent.UPDATE) {
                    int row = listener.getFirstRow();
                    int column = listener.getColumn();
                    Object newValue = tableModel.getValueAt(row, column);

                    changedCells.computeIfAbsent(row, k -> new HashMap<>()).put(column, newValue);
                }
            });
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);

            JButton editButton = new JButton("Редактировать данные");
            editButton.addActionListener(e -> {
                Object[] options = {"Да", "Нет"};
                int confirm = JOptionPane.showOptionDialog(frame, "Отредактировать измененные данные?", "Подтверждение", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                if (confirm == JOptionPane.YES_OPTION) {
                    updateData();
                }
            });
            panel.add(editButton, BorderLayout.WEST);

            JButton refreshButton = new JButton("Обновить");
            refreshButton.addActionListener(e -> showData(frame, tableModel, currentOrder));
            panel.add(refreshButton, BorderLayout.SOUTH);

            JButton cancelButton = new JButton("Отмена");
            cancelButton.addActionListener(e -> {
                frame.dispose();
                new App();
            });
            panel.add(cancelButton, BorderLayout.EAST);

            frame.setVisible(true);
        }

        public void updateData() {
            if (changedCells.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Изменений в таблице нет!", "Инфо", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            try (Connection connection = DB.getConnection()) {
                assert connection != null;
                connection.setAutoCommit(false);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (Map.Entry<Integer, Map<Integer, Object>> entry : changedCells.entrySet()) {
                    int row = entry.getKey();
                    Map<Integer, Object> rowChanges = entry.getValue();
                    StringBuilder sql = DB.getUpdateSql();

                    try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                        int paramIndex = 1;
                        for (int i = 0; i < columnNames.size(); i++) {
                            Object value;
                            if (rowChanges.containsKey(i)) {
                                value = rowChanges.get(i);
                            }
                            else {
                                value = tableModel.getValueAt(row, i);
                            }
                            if (columnNames.get(i).equals("UpdatedAt")) {
                                String formattedDate = sdf.format(new Date());
                                statement.setString(paramIndex++, formattedDate);
                            }
                            else {
                                if (value != null) {
                                    statement.setObject(paramIndex++, value);
                                }
                                else {
                                    statement.setNull(paramIndex++, java.sql.Types.VARCHAR);
                                }
                            }
                        }
                        try {
                            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                            statement.setInt(paramIndex, id);
                        }
                        catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(frame, "Неверный ID!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            connection.rollback();
                            return;
                        }

                        statement.executeUpdate();
                    }
                    catch (SQLException e) {
                        try {
                            connection.rollback();
                        }
                        catch (SQLException rollbackException) {
                            JOptionPane.showMessageDialog(frame, "Ошибка отката транзакции: " + rollbackException.getMessage(), "Критическая ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                        JOptionPane.showMessageDialog(frame, "Ошибка обновления данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }

                connection.commit();
                JOptionPane.showMessageDialog(frame, "Данные успешно обновлены!", "Инфо", JOptionPane.INFORMATION_MESSAGE);
                changedCells.clear();
            }
            catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Ошибка создания соединения: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static class Delete {
        private JFrame frame;
        private JTable table;
        private DefaultTableModel tableModel;
        private final Vector<String> columnNames = new Vector<>(Arrays.asList("ID", "Name", "CreatedAt", "UpdatedAt"));
        private SortOrder currentOrder = UNSORTED;

        public Delete() {
            GUI();
            DataManage.showData(frame, tableModel, UNSORTED);
        }

        private void GUI() {
            frame = new JFrame("Удалить данные");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

            JPanel panel = new JPanel(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);

            JPanel sortPanel = new JPanel();
            sortPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

            JRadioButton ascendingButton = new JRadioButton("По возрастанию");
            ascendingButton.addActionListener(e -> {
                currentOrder = ASCENDING;
                showData(frame, tableModel, currentOrder);
            });

            JRadioButton descendingButton = new JRadioButton("По убыванию");
            descendingButton.addActionListener(e -> {
                currentOrder = DESCENDING;
                showData(frame, tableModel, currentOrder);
            });
            JRadioButton noSort = new JRadioButton("Без сортировки", true);
            noSort.addActionListener(e -> {
                currentOrder = UNSORTED;
                showData(frame, tableModel, currentOrder);
            });

            ButtonGroup sortGroup = new ButtonGroup();
            sortGroup.add(ascendingButton);
            sortGroup.add(descendingButton);
            sortGroup.add(noSort);

            sortPanel.add(ascendingButton);
            sortPanel.add(descendingButton);
            sortPanel.add(noSort);

            panel.add(sortPanel, BorderLayout.NORTH);

            tableModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column > 0 && column < columnNames.size() - 2;
                }
            };
            table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);

            JButton submitButton = new JButton("Удалить данные");
            submitButton.addActionListener(e -> {
                Object[] options = {"Да", "Нет"};
                int confirm = JOptionPane.showOptionDialog(frame, "Удалить выбранные строки?", "Подтверждение", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteData();
                }
            });
            panel.add(submitButton, BorderLayout.SOUTH);

            JButton cancelButton = new JButton("Отмена");
            cancelButton.addActionListener(e -> {
                frame.dispose();
                new App();
            });
            panel.add(cancelButton, BorderLayout.EAST);

            frame.setVisible(true);
        }

        public void deleteData() {
            int[] selectedRows = table.getSelectedRows();

            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(frame, "Выберите строки!", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(frame, "Таблица пуста!", "Инфо", JOptionPane.INFORMATION_MESSAGE);
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
                    try {
                        connection.rollback();
                    }
                    catch (SQLException rollbackException) {
                        JOptionPane.showMessageDialog(frame, "Ошибка отката транзакции: " + rollbackException.getMessage(), "Критическая ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                    JOptionPane.showMessageDialog(frame, "Ошибка удаления данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Ошибка создания соединения: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
