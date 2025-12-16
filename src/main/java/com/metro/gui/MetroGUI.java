package com.metro.gui;

import com.metro.algorithm.MetroPathFinder;
import com.metro.model.PathResult;
import com.metro.utils.ResourceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MetroGUI extends JFrame {

    private static final Logger logger = LogManager.getLogger(MetroGUI.class);

    private DrawingPanel drawingPanel;
    private JTextField startStationField;
    private JTextField endStationField;
    private List<Station> stations;
    private Station selectedStart = null;
    private Station selectedEnd = null;
    private List<String> currentPath = new ArrayList<>();

    public MetroGUI() {
        super("Metro Navigator");
        stations = ResourceLoader.loadStations();

        // Простая инициализация смещений
        initializeSimpleTextOffsets();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 1000);
        setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        Image iconImage = ResourceLoader.loadImage("metro_icon.png");
        if (iconImage != null) {
            this.setIconImage(iconImage);
        }

        add(createInputPanel(), BorderLayout.SOUTH);
        setLocationRelativeTo(null); // Центрируем окно
        setVisible(true);
    }

    public static class Station {
        String name;
        int x, y;
        String line;
        int textX, textY; // Координаты текста

        public Station(String name, int x, int y, String line) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.line = line;
            this.textX = x + 8; // По умолчанию справа
            this.textY = y + 4; // По умолчанию немного ниже
        }
    }

    // Простая инициализация смещений текста
    private void initializeSimpleTextOffsets() {
        // Только для самых проблемных станций
        for (Station station : stations) {
            // По умолчанию все станции имеют текст справа
            station.textX = station.x + 8;
            station.textY = station.y + 4;

            // Для станций слева от линии - сдвигаем текст влево
            if (station.x < 300) {
                station.textX = station.x - 60;
                station.textY = station.y;
            }

            // Для станций в центре - сдвигаем текст вверх
            if (station.y > 300 && station.y < 600 && station.x > 300 && station.x < 600) {
                station.textY = station.y - 10;
            }

            // Особые случаи для конкретных станций
            switch (station.name) {
                case "Невский проспект":
                    station.textY = station.y - 15;
                    break;
                case "Маяковская":
                    station.textY = station.y - 25;
                    station.textX = station.x - 10;
                    break;
                case "Владимирская":
                case "Пушкинская":
                case "Достоевская":
                    station.textX = station.x - 15;
                    station.textY = station.y;
                    break;
                case "Чернышевская":
                case "Площадь Ленина":
                    station.textX = station.x + 10;
                    station.textY = station.y;
                    break;
                case "Гостиный двор":
                    station.textX = station.x - 75;
                    station.textY = station.y + 4;
                    break;
                case "Технологический институт-1":
                case "Технологический институт-2":
                case "Площадь Александра Невского-1":
                case "Площадь Александра Невского-2":
                    station.textY = station.y - 15;
                    break;
                case "Сенная площадь":
                case "Спасская":
                case "Садовая":
                case "Адмиралтейская":
                    station.textY = station.y + 5;
                    break;
            }
        }
    }

    private Station findStationByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String normalizedName = name.trim();
        for (Station station : stations) {
            if (station.name.equals(normalizedName)) {
                return station;
            }
        }

        return null;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(new Color(240, 240, 240));

        JButton deselect = new JButton("Очистить маршрут");
        startStationField = new JTextField(15);
        endStationField = new JTextField(15);
        JButton findButton = new JButton("Найти путь");

        // Простая стилизация
        Font buttonFont = new Font("Arial", Font.PLAIN, 12);
        deselect.setFont(buttonFont);
        findButton.setFont(buttonFont);

        // Убираем сложные стили
        deselect.setBackground(new Color(220, 220, 220));
        findButton.setBackground(new Color(70, 130, 180));
        findButton.setForeground(Color.WHITE);

        Font fieldFont = new Font("Arial", Font.PLAIN, 12);
        startStationField.setFont(fieldFont);
        endStationField.setFont(fieldFont);

        findButton.addActionListener(e -> {
            String startText = startStationField.getText().trim();
            String endText = endStationField.getText().trim();

            if (startText.isEmpty() || endText.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Пожалуйста, введите начальную и конечную станции",
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Station startStation = findStationByName(startText);
            Station endStation = findStationByName(endText);

            if (startStation == null) {
                JOptionPane.showMessageDialog(this,
                        "Станция не найдена: " + startText,
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (endStation == null) {
                JOptionPane.showMessageDialog(this,
                        "Станция не найдена: " + endText,
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (startStation.equals(endStation)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Начальная и конечная станции должны быть разными",
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            selectedStart = startStation;
            selectedEnd = endStation;

            try {
                MetroPathFinder pathFinder = new MetroPathFinder("metro.txt");
                PathResult result = pathFinder.findShortestPath(
                        selectedStart.name,
                        selectedEnd.name
                );

                if (result.getErrorMessage() != null) {
                    JOptionPane.showMessageDialog(
                            this,
                            result.getErrorMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                currentPath = result.getPath();

                // Обновляем панель с путем
                drawingPanel.setCurrentPath(currentPath);
                drawingPanel.setSelectedStart(selectedStart);
                drawingPanel.setSelectedEnd(selectedEnd);
                drawingPanel.repaint();

                // Показываем результат
                StringBuilder message = new StringBuilder();
                message.append("Время в пути: ").append(result.getTotalTime()).append(" минут\n\n");
                message.append("Маршрут:\n");

                for (int i = 0; i < currentPath.size(); i++) {
                    message.append(i + 1).append(". ").append(currentPath.get(i)).append("\n");
                }

                JTextArea textArea = new JTextArea(message.toString());
                textArea.setFont(new Font("Arial", Font.PLAIN, 12));
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));

                JOptionPane.showMessageDialog(
                        this,
                        scrollPane,
                        "Маршрут найден",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Ошибка поиска пути: " + ex.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        deselect.addActionListener(e -> {
            // Простой и надежный сброс
            selectedStart = null;
            selectedEnd = null;
            startStationField.setText("");
            endStationField.setText("");
            currentPath.clear();

            // Очищаем состояние панели
            drawingPanel.clearAll();
            drawingPanel.repaint();
        });

        panel.add(deselect);
        panel.add(new JLabel("От:"));
        panel.add(startStationField);
        panel.add(new JLabel("До:"));
        panel.add(endStationField);
        panel.add(findButton);

        return panel;
    }

    private class DrawingPanel extends JPanel {

        private double scale = 1.0;
        private double offsetX = 0;
        private double offsetY = 0;
        private Station hoveredStation = null;
        private Point lastDragPoint = null;
        private List<String> currentPath = new ArrayList<>();
        private Station selectedStart = null;
        private Station selectedEnd = null;

        public void setCurrentPath(List<String> path) {
            this.currentPath = new ArrayList<>(path);
        }

        public void setSelectedStart(Station station) {
            this.selectedStart = station;
        }

        public void setSelectedEnd(Station station) {
            this.selectedEnd = station;
        }

        public void clearAll() {
            this.currentPath.clear();
            this.selectedStart = null;
            this.selectedEnd = null;
            this.hoveredStation = null;
        }

        public DrawingPanel() {
            setBackground(Color.WHITE);

            addMouseWheelListener(e -> {
                int mouseX = e.getX();
                int mouseY = e.getY();

                double oldScale = scale;

                if (e.getPreciseWheelRotation() < 0) {
                    scale *= 1.1;
                } else {
                    scale /= 1.1;
                }

                scale = Math.max(0.5, Math.min(scale, 3.0));

                offsetX = mouseX - (mouseX - offsetX) * (scale / oldScale);
                offsetY = mouseY - (mouseY - offsetY) * (scale / oldScale);
                repaint();
            });

            MouseAdapter mouseAdapter = new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    Station clicked = getStationAt(e.getX(), e.getY());
                    if (clicked != null) {
                        if (selectedStart == null) {
                            selectedStart = clicked;
                            startStationField.setText(clicked.name);
                        } else if (selectedEnd == null) {
                            selectedEnd = clicked;
                            endStationField.setText(clicked.name);
                        } else {
                            selectedStart = clicked;
                            selectedEnd = null;
                            startStationField.setText(clicked.name);
                            endStationField.setText("");
                        }
                        repaint();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    lastDragPoint = e.getPoint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    Point current = e.getPoint();
                    offsetX += current.x - lastDragPoint.x;
                    offsetY += current.y - lastDragPoint.y;
                    lastDragPoint = current;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    lastDragPoint = null;
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    Station hovered = getStationAt(e.getX(), e.getY());

                    if (hovered != hoveredStation) {
                        hoveredStation = hovered;

                        if (hoveredStation != null) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        } else {
                            setCursor(Cursor.getDefaultCursor());
                        }

                        repaint();
                    }
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        private Station getStationAt(int mouseX, int mouseY) {
            double mapX = (mouseX - offsetX) / scale;
            double mapY = (mouseY - offsetY) / scale;

            for (Station s : stations) {
                double dx = mapX - s.x;
                double dy = mapY - s.y;

                // Простая проверка радиуса 8px
                if (Math.abs(dx) <= 8 && Math.abs(dy) <= 8) {
                    return s;
                }
            }

            return null;
        }

        private void drawMetroLine(Graphics2D g2, String lineCode, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            List<Station> lineStations = stations.stream()
                    .filter(s -> lineCode.equals(s.line))
                    .sorted(Comparator.comparingInt(s -> s.y))
                    .collect(Collectors.toList());

            for (int i = 0; i < lineStations.size() - 1; i++) {
                Station s1 = lineStations.get(i);
                Station s2 = lineStations.get(i + 1);
                g2.drawLine(s1.x, s1.y, s2.x, s2.y);
            }
        }

        private void drawMetroLineWithConnections(Graphics2D g2, String lineCode, Color color,
                                                  List<Connection> connections) {
            drawMetroLine(g2, lineCode, color);

            g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Connection conn : connections) {
                Station s1 = findStationByName(conn.station1);
                Station s2 = findStationByName(conn.station2);
                if (s1 != null && s2 != null) {
                    g2.drawLine(s1.x, s1.y, s2.x, s2.y);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            AffineTransform saved = g2.getTransform();

            g2.translate(offsetX, offsetY);
            g2.scale(scale, scale);

            // Очищаем фон
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Рисуем линии метро
            List<Connection> redConnections = List.of(
                    new Connection("Площадь Восстания", "Маяковская"),
                    new Connection("Владимирская", "Достоевская"),
                    new Connection("Пушкинская", "Звенигородская"),
                    new Connection("Технологический институт-1", "Технологический институт-2")
            );
            drawMetroLineWithConnections(g2, "r", Color.RED, redConnections);

            List<Connection> blueConnections = List.of(
                    new Connection("Невский проспект", "Гостиный двор")
            );
            drawMetroLineWithConnections(g2, "b", new Color(0, 120, 190), blueConnections);

            List<Connection> greenConnections = List.of(
                    new Connection("Гостиный двор", "Маяковская"),
                    new Connection("Василеостровская", "Гостиный двор"),
                    new Connection("Площадь Александра Невского-1", "Площадь Александра Невского-2")
            );
            drawMetroLineWithConnections(g2, "g", new Color(70, 180, 90), greenConnections);

            List<Connection> orangeConnections = List.of(
                    new Connection("Спасская", "Садовая"),
                    new Connection("Спасская", "Достоевская")
            );
            drawMetroLineWithConnections(g2, "o", new Color(245, 130, 30), orangeConnections);

            List<Connection> purpleConnections = List.of(
                    new Connection("Садовая", "Звенигородская"),
                    new Connection("Адмиралтейская", "Садовая")
            );
            drawMetroLineWithConnections(g2, "p", new Color(145, 75, 155), purpleConnections);

            // Рисуем станции
            for (Station s : stations) {
                boolean isInPath = currentPath.contains(s.name);
                boolean isStartOrEnd = (selectedStart != null && s.name.equals(selectedStart.name)) ||
                        (selectedEnd != null && s.name.equals(selectedEnd.name));
                boolean isHovered = hoveredStation != null && s.name.equals(hoveredStation.name);

                // Определяем цвет для станции
                Color stationColor;
                switch (s.line) {
                    case "r": stationColor = Color.RED; break;
                    case "b": stationColor = new Color(0, 120, 190); break;
                    case "g": stationColor = new Color(70, 180, 90); break;
                    case "o": stationColor = new Color(245, 130, 30); break;
                    case "p": stationColor = new Color(145, 75, 155); break;
                    default: stationColor = Color.BLACK;
                }

                // Рисуем станцию
                drawStation(g2, s, stationColor, isInPath, isStartOrEnd, isHovered);

                // Рисуем название станции
                drawStationName(g2, s, isInPath, isStartOrEnd, isHovered);
            }

            g2.setTransform(saved);
        }

        private void drawStation(Graphics2D g2, Station s, Color stationColor,
                                 boolean isInPath, boolean isStartOrEnd, boolean isHovered) {
            // Внешний круг (цвет линии)
            g2.setColor(stationColor);
            g2.fillOval(s.x - 6, s.y - 6, 12, 12);

            // Внутренний круг
            Color innerColor;
            if (isHovered || isStartOrEnd || isInPath) {
                innerColor = Color.RED; // Красный при активности
            } else {
                innerColor = Color.WHITE; // Белый по умолчанию
            }

            g2.setColor(innerColor);
            g2.fillOval(s.x - 3, s.y - 3, 6, 6);

            // Черная обводка
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1f));
            g2.drawOval(s.x - 6, s.y - 6, 12, 12);
        }

        private void drawStationName(Graphics2D g2, Station s,
                                     boolean isInPath, boolean isStartOrEnd, boolean isHovered) {
            // Цвет текста
            Color textColor;
            if (isHovered || isStartOrEnd || isInPath) {
                textColor = Color.RED; // Красный при активности
            } else {
                textColor = Color.BLACK; // Черный по умолчанию
            }

            g2.setColor(textColor);
            g2.setFont(new Font("Arial", Font.BOLD, 11)); // Жирный шрифт, немного меньше

            // Рисуем текст в предопределенных координатах
            g2.drawString(s.name, s.textX, s.textY);
        }
    }

    private static class Connection {
        String station1;
        String station2;

        Connection(String station1, String station2) {
            this.station1 = station1;
            this.station2 = station2;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MetroGUI();
        });
    }
}
