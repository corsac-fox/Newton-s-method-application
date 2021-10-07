package solver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.Arrays;

public class EquationSolver {

    Point viewPosition = new Point(0, 0);
    Drawing drawing;
    StringBuilder answer;
    CalculationWindow owner;

    private double argumentValue;
    private double[] extremumPoints;
    private double a, b, c, d;
    private double start, end;
    private boolean isQuadraticSolved, isQuadratic;

    private int segmentNumber;

    public EquationSolver(CalculationWindow owner) {
        this.owner = owner;
        answer = new StringBuilder();
        drawing = new Drawing();
    }

    double f(double x) {// возвращает значение функции
        return a * java.lang.Math.pow(x, 3) + b * java.lang.Math.pow(x, 2) + c * x + d;
    }

    double df(double x) {// возвращает значение производной
        return 3 * a * java.lang.Math.pow(x, 2) + 2 * b * x + c;
    }

    double d2f(double x) {// возвращает значение второй производной
        return 6 * x + 2 * b;
    }

    void calculate(double a, double b, double c, double d, double start, double end) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;

        this.start = Math.min(end, start);
        this.end = Math.max(end, start);

        segmentNumber = 0;
        isQuadraticSolved = false;
        isQuadratic = false;

        if (answer.length() > 0) answer.delete(0, answer.length());

        if (equalsZero(a) && !equalsZero(b)) {
            answer.append("квадратное уравнение\n");
            isQuadratic = true;
            quadraticEquationSolution(b, c, d);
        }

        else if (!equalsZero(a)) cubicEquationSolution(start, end, 6);

        try {
            drawing.calculate();
        } catch (NullPointerException npe) {
            answer.append("Введённых данных недостаточно для решения квадратного или кубического уравнения");
        }

    }

    private boolean equalsZero(double d) {
        return Math.abs(d - 0) < 0.000000001;
    }

    private void quadraticEquationSolution(double a, double b, double c) {

        double D = Math.pow(b, 2) - 4 * a * c;

        if (isQuadratic) answer.append("D = ").append(D).append("\n");

        if (D < 0) {
            extremumPoints = new double[2];
            if (isQuadratic) answer.append("нет корней");
        }

        else if (D == 0) {
            extremumPoints = new double[]{0, 0, (-b / 2 * a)};
            if (isQuadratic) answer.append("x = ").append(extremumPoints[2]);
        }

        else {
            extremumPoints = new double[]{0, 0, (-b - Math.sqrt(D)) / (2 * a), (-b + Math.sqrt(D)) / (2 * a)};
            if (isQuadratic) answer.append("x1 = ").append(extremumPoints[2]).append("\nx2 = ").append(extremumPoints[3]);
        }
        isQuadraticSolved = true;
    }

    private void cubicEquationSolution(double start, double end, int approximationsNumber) {

        if (!isQuadraticSolved) {//если не решено уравнение производной

            quadraticEquationSolution(3 * a, 2 * b, c);//решаем уравнение производной

            extremumPoints = setIntervals(extremumPoints); //распределяем исследуемые отрезки

            for (int i = 0; i + 1 < extremumPoints.length; i++, segmentNumber++) {
                cubicEquationSolution(extremumPoints[i], extremumPoints[i + 1], approximationsNumber); //исследуем каждый отрезок
            }

        } else {
            if (f(start) * f(end) > 0) //функция не меняет знак на промежутке
                answer.append("Нет корней на участке ").append(start).append("-").append(end).append("\n");
            else {// начинаем расчет касательных

                double x0, y0;
                x0 = start;
                while (f(x0) * d2f(x0) <= 0 && x0 < end - 0.1) x0 += 0.1;

                answer.append("x0 = ").append(x0).append("\n");

                for (int i = 1; i <= approximationsNumber; i++) {
                    y0 = f(x0);
                    x0 = tangentArgument(x0, y0, end);
                    if (equalsZero(x0)) {
                        x0 = 0;
                        answer.append("x").append(i).append(" = ").append(x0).append("\n");
                        break;
                    }
                    else answer.append("x").append(i).append(" = ").append(x0).append("\n");
                }
                answer.append("Корень уравнения на участке от ").append(start).append(" до ").append(end).append(": ").append(x0).append("\n");
            }
        }
    }

    private double[] setIntervals(double[] arr) { //распределяем участки по точкам экстремума функции
        arr[0] = start;
        arr[1] = end;
        return Arrays.stream(arr).distinct().
                sorted().filter(i -> i <= end).filter(i -> i >= start).
                toArray();
    }

    double tangentArgument(double x0, double y0, double end) {
        return (!equalsZero(df(x0)) /*|| -y0 / df(x0) + x0 > end*/) ? // проверяем, что
                // производная функции не равна нулю
                -y0 / df(x0) + x0 : tangentArgument(x0 + 0.15, f(x0 + 0.15), end); //эмпирически вычисленный шаг х0
    }

    StringBuilder getAnswer() {
        return answer;
    }

    protected class Drawing extends JPanel {

        private final Path2D function = new Path2D.Double();
        private final Path2D derivative = new Path2D.Double();
        private final Path2D secondDerivative = new Path2D.Double();

        private int axisCoefficient = 30;
        private int xAxisLevel = 350, yAxisLevel = 1;
        private int negativeXAxis;
        private int yAxisLength = 700, xAxisLength = 700;
        private boolean isStretched;

        Drawing() {

            setPreferredSize(new Dimension(700, 700));
            setBackground(new Color(100, 143, 190));
            setAutoscrolls(true);
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    isStretched = e.getButton() == 1;
                    changeProportions();
                }
            });
        }

        private void calculate() {

            negativeXAxis = Math.min((int)start, 0);
            xAxisLevel =
                    (int)Math.ceil(Arrays.stream(extremumPoints).map(x -> f(x)).max().getAsDouble() * axisCoefficient);

            setStartingPoints();
            yAxisLevel = (int)-start * axisCoefficient;

            for (argumentValue = start; argumentValue <= end; argumentValue += 0.125) {
                buildGraph();
            }

            setSize();
            viewPosition.move(Math.max(drawing.yAxisLevel - 350, 0), Math.max(drawing.xAxisLevel - 350, 0));
            owner.scrollPane.getViewport().setViewPosition(viewPosition);
            repaint();
        }

        private void changeProportions() {
            if (isStretched) {
                axisCoefficient = axisCoefficient << 1;
            }
            else {
                axisCoefficient = axisCoefficient >> 1;
            }

            calculate();
            owner.scrollPane.getVerticalScrollBar().setValue(drawing.function.getBounds().height);
            owner.scrollPane.getViewport().setViewPosition(viewPosition);

            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            for (int i = negativeXAxis; i <= start + getWidth(); i++) {
                g2d.drawString(String.valueOf(i), (i - negativeXAxis) * axisCoefficient + 2,
                        xAxisLevel - 10);
            }

            g2d.drawLine(0, xAxisLevel, xAxisLength, xAxisLevel); // ось x
            g2d.drawLine(yAxisLevel, 0, yAxisLevel, yAxisLength); // ось y

            g2d.draw(function); // график функции
            g2d.setColor(Color.RED);
            g2d.draw(derivative); // график производной функции
            g2d.setColor(Color.GREEN);
            g2d.draw(secondDerivative); // график производной второго порядка
        }

        void setSize() {
            yAxisLength = Math.max(function.getBounds().height, 700);
            xAxisLength = Math.max(function.getBounds().width, 700);

            setPreferredSize(new Dimension(function.getBounds().width,
                    function.getBounds().height));
        }

        void setStartingPoints() {
            function.reset();
            derivative.reset();
            secondDerivative.reset();

            function.moveTo((start - negativeXAxis) * axisCoefficient, xAxisLevel - f(start) * axisCoefficient);
            derivative.moveTo((start - negativeXAxis) * axisCoefficient, xAxisLevel - df(start) * axisCoefficient);
            secondDerivative.moveTo((start - negativeXAxis) * axisCoefficient, xAxisLevel - d2f(start) * axisCoefficient);
        }

        void buildGraph() {
            function.lineTo((argumentValue - negativeXAxis) * axisCoefficient,
                    xAxisLevel - f(argumentValue) * axisCoefficient);
            derivative.lineTo((argumentValue - negativeXAxis) * axisCoefficient,
                    xAxisLevel - df(argumentValue) * axisCoefficient);
            secondDerivative.lineTo((argumentValue - negativeXAxis) * axisCoefficient,
                    xAxisLevel - d2f(argumentValue) * axisCoefficient);
        }
    }
}