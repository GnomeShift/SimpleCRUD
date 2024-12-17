import javax.swing.*;
import java.awt.*;

public class App {
    private JFrame mainFrame;

    public App() {
        createMain();
    }

    public void createMain() {
        mainFrame = new JFrame("SimpleCRUD");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(400, 300);
        mainFrame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        mainFrame.add(panel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel helloLabel = new JLabel("Выберите действие:");
        controlPanel.add(helloLabel);
        controlPanel.add(Box.createVerticalStrut(10));

        JButton addDataButton = new JButton("Добавить данные");
        addDataButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addDataButton.addActionListener(e -> {
            new DataManage.Create();
            mainFrame.dispose();
        });
        controlPanel.add(addDataButton);

        JButton getDataButton = new JButton("Получить данные");
        getDataButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        getDataButton.addActionListener(e -> {
            new DataManage.Read();
            mainFrame.dispose();
        });
        controlPanel.add(getDataButton);

        JButton deleteDataButton = new JButton("Удалить данные");
        deleteDataButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteDataButton.addActionListener(e -> {
            new DataManage.Delete();
            mainFrame.dispose();
       });
        controlPanel.add(deleteDataButton);

        mainFrame.add(controlPanel, BorderLayout.NORTH);
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
