package solver;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

public class SettingsPanel extends JPanel implements ActionListener {

    private final JEditorPane[] windows;

    SpringLayout layout = new SpringLayout();
    private final JTextArea answer = new JTextArea();
    private final EquationSolver solver;
    private final CalculationWindow owner;

    public SettingsPanel(CalculationWindow owner) {

        this.owner = owner;
        solver = owner.computer;

        setPreferredSize(new Dimension(230, 700));
        setLayout(layout);

        JLabel commonFormula = new JLabel("<html>общая формула: <br>ax\u00b3 + bx\u00b2 + cx + d = 0</html>");
        JLabel interval = new JLabel("<html>найти корни уравнения <br>на промежутке</html>");
        JButton calculate = new JButton("рассчитать");

        windows = new JEditorPane[6];

        Dimension standardSize = new Dimension(120, 26);

        add(commonFormula);
        JLabel[] parameters = new JLabel[7];
        String[] names = {"a", "b", "c", "d", "от:", "до:"};

        for (int i = 0; i < names.length; i++) {
            windows[i] = new JEditorPane();
            windows[i].setPreferredSize(standardSize);
            ((AbstractDocument)windows[i].getDocument()).setDocumentFilter(new Filter());

            add(windows[i]);
            parameters[i] = new JLabel(names[i]);
            add(parameters[i]);
        }

        add(interval);
        calculate.setPreferredSize(standardSize);
        calculate.addActionListener(this);
        calculate.setFocusPainted(false);
        add(calculate);

        answer.setLineWrap(true); //перенос строк в окне ответа
        answer.setWrapStyleWord(true); //по словам
        answer.setBounds(20, 200, 120, 200);
        answer.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(answer);

        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setBounds(20, 200, 120, 200);
        scrollPane.setPreferredSize(new Dimension(190, 300));
        add(scrollPane);

        {
            layout.putConstraint(SpringLayout.WEST, commonFormula, 20, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, commonFormula, 30, SpringLayout.NORTH, this);

            layout.putConstraint(SpringLayout.WEST, parameters[0], 20, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, parameters[0], 45, SpringLayout.NORTH, commonFormula);
            layout.putConstraint(SpringLayout.WEST, windows[0], 50, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, windows[0], 45, SpringLayout.NORTH, commonFormula);

            layout.putConstraint(SpringLayout.WEST, parameters[1], 20, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, parameters[1], 35, SpringLayout.NORTH, parameters[0]);
            layout.putConstraint(SpringLayout.WEST, windows[1], 50, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, windows[1], 35, SpringLayout.NORTH, windows[0]);

            layout.putConstraint(SpringLayout.WEST, parameters[2], 20, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, parameters[2], 35, SpringLayout.NORTH, parameters[1]);
            layout.putConstraint(SpringLayout.WEST, windows[2], 50, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, windows[2], 35, SpringLayout.NORTH, windows[1]);

            layout.putConstraint(SpringLayout.WEST, parameters[3], 20, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, parameters[3], 35, SpringLayout.NORTH, parameters[2]);
            layout.putConstraint(SpringLayout.WEST, windows[3], 50, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, windows[3], 35, SpringLayout.NORTH, windows[2]);

            layout.putConstraint(SpringLayout.WEST, interval, 20, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, interval, 45, SpringLayout.NORTH, parameters[3]);

            layout.putConstraint(SpringLayout.WEST, parameters[4], 20, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, parameters[4], 45, SpringLayout.NORTH, interval);

            layout.putConstraint(SpringLayout.WEST, windows[4], 50, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, windows[4], 45, SpringLayout.NORTH, interval);

            layout.putConstraint(SpringLayout.WEST, parameters[5], 20, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, parameters[5], 35, SpringLayout.NORTH, parameters[4]);
            layout.putConstraint(SpringLayout.WEST, windows[5], 50, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, windows[5], 35, SpringLayout.NORTH, parameters[4]);

            layout.putConstraint(SpringLayout.WEST, calculate, 50, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, calculate, 35, SpringLayout.NORTH, parameters[5]);


            layout.putConstraint(SpringLayout.WEST, answer, 20, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, answer, 45, SpringLayout.NORTH, calculate);
            layout.putConstraint(SpringLayout.WEST, scrollPane, 20, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, answer);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        solver.calculate(getCoefficient(windows[0]), getCoefficient(windows[1]), getCoefficient(windows[2]),
                getCoefficient(windows[3]), getCoefficient(windows[4]),
                getCoefficient(windows[5]));

        answer.setText(solver.getAnswer().toString());
        owner.scrollPane.getViewport().setViewPosition(solver.viewPosition);
    }

    double getCoefficient(JEditorPane je)
    {
        return je.getText().equals("") ? 0 : Double.parseDouble(je.getText());
    }

    private static class Filter extends DocumentFilter {

        private final Pattern allowedChars = Pattern.compile("-|\\.|\\d");
        private final Pattern integer = Pattern.compile("(\\d+)|(\\d*)");
        private final Pattern negativeNumber = Pattern.compile("-\\.+");

        @Override
        public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
            if (str == null) {
                return;
            }

            if (integer.matcher(str).matches()) {

                super.insertString(fb, offs, str, a);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet attrs)
                throws BadLocationException {
            String text = fb.getDocument().getText(0, fb.getDocument().getLength());

            if (str == null) {
                return;
            }

            if (allowedChars.matcher(str).matches()) {
                if (str.equals("-")) {
                    if (!negativeNumber.matcher(text).matches() && offset == 0) fb.replace(offset, length, str, attrs);
                }

                else if (str.equals(".")) {
                    if (integer.matcher(text).matches()) fb.replace(offset, length, str, attrs);
                }

                else fb.replace(offset, length, str, attrs);
            }
        }

        @Override
        public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
            fb.remove(offset, length);
        }
    }
}