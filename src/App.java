import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

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
                    new DataManage.Read().showData();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new App();
            }
        });
    }
}
