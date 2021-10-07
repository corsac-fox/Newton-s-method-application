package solver;

import javax.swing.*;
import java.awt.*;

public class CalculationWindow extends JFrame {

    EquationSolver.Drawing drawing;
    EquationSolver computer;
    JScrollPane scrollPane;

    public CalculationWindow() {


        computer = new EquationSolver(this);
        drawing = computer.drawing;
        SettingsPanel settings = new SettingsPanel(this);

        scrollPane = new JScrollPane(drawing);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setBounds(320, 200, 700, 700);
        scrollPane.setPreferredSize(new Dimension(700, 700));

        JPanel panel = (JPanel) this.getContentPane();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(scrollPane);
        panel.add(settings);

        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        pack();
        setVisible(true);
    }
}