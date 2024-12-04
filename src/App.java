import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class App {
    private JFrame mainFrame;
    private JLabel helloLabel;
    private JButton getDataButton;
    private JPanel panel;
    private JPanel controlPanel;


    public App() {
        createMain();
    }

    public void createMain() {
        mainFrame = new JFrame("SimpleCRUD");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(400, 300);
        mainFrame.setLayout(new BorderLayout());

        panel = new JPanel(new BorderLayout());
        mainFrame.add(panel, BorderLayout.CENTER);

        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);


        helloLabel = new JLabel("Выберите действие:");
        controlPanel.add(helloLabel);
        controlPanel.add(Box.createVerticalStrut(10));

        getDataButton = new JButton("Получить данные");
        getDataButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        getDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    showData();
                    helloLabel.setVisible(false);
                    mainFrame.revalidate();
                    mainFrame.repaint();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Ошибка при получении данных: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        controlPanel.add(getDataButton);
        mainFrame.add(controlPanel, BorderLayout.NORTH);

        mainFrame.setVisible(true);
    }

    private void showData() throws SQLException {
        List<Object[]> data = getData();
        String[] columnNames = {"ID", "Name"};

        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "Таблица пуста!", "Инфо", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object[][] dataArray = data.toArray(new Object[0][0]);
        JTable table = new JTable(dataArray, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private List<Object[]> getData() throws SQLException {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new App();
            }
        });
    }
}
