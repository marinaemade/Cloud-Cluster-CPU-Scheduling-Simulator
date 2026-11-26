import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GUI extends JFrame {

    private DefaultTableModel model;
    private JTable table;
    private JTextField qField = new JTextField("3", 4);
    private JTextField csField = new JTextField("0", 4);
    private JComboBox<String> algoBox = new JComboBox<>(new String[]{"SJF", "ACS", "Both"});
    private JTextArea outputArea = new JTextArea();
    private JPanel ganttHolder = new JPanel();

    public GUI() {
        setTitle("CPU Scheduling Simulator - SJF & ACS");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout(5, 5));

        // ---------- process table ----------
        model = new DefaultTableModel(new String[]{"Process", "Arrival", "Burst", "Priority"}, 0) {
            public boolean isCellEditable(int row, int col) {
                return col != 0; // process name is automatic
            }
        };
        table = new JTable(model);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(350, 150));

        JButton addButton = new JButton("Add Process");
        JButton removeButton = new JButton("Remove Selected");
        JButton tc1Button = new JButton("Load Test Case 1");
        JButton tc2Button = new JButton("Load Test Case 2");
        JButton runButton = new JButton("Run");

        addButton.addActionListener(e -> {
            model.addRow(new Object[]{"P" + (model.getRowCount() + 1), 0, 1, 1});
        });
        removeButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                model.removeRow(row);
                renumber();
            }
        });
        tc1Button.addActionListener(e ->
            loadRows(new int[][]{{0, 4, 2}, {1, 7, 3}, {3, 2, 1}}, 3, 0));
        tc2Button.addActionListener(e ->
            loadRows(new int[][]{{0, 5, 4}, {1, 6, 2}, {4, 4, 3}}, 3, 1));
        runButton.addActionListener(e -> runSimulation());

        // ---------- controls ----------
        JPanel settings = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settings.add(new JLabel("Quantum (Q):"));
        settings.add(qField);
        settings.add(new JLabel("Context Switch (CS):"));
        settings.add(csField);
        settings.add(new JLabel("Algorithm:"));
        settings.add(algoBox);
        settings.add(runButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addButton);
        buttons.add(removeButton);
        buttons.add(tc1Button);
        buttons.add(tc2Button);

        JPanel top = new JPanel(new BorderLayout());
        top.add(tableScroll, BorderLayout.CENTER);
        JPanel right = new JPanel(new GridLayout(2, 1));
        right.add(buttons);
        right.add(settings);
        top.add(right, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // ---------- output tabs ----------
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setEditable(false);

        ganttHolder.setLayout(new BoxLayout(ganttHolder, BoxLayout.Y_AXIS));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Log & Results", new JScrollPane(outputArea));
        tabs.addTab("Gantt Chart", new JScrollPane(ganttHolder));
        add(tabs, BorderLayout.CENTER);

        loadRows(new int[][]{{0, 4, 2}, {1, 7, 3}, {3, 2, 1}}, 3, 0);
    }

    // put test data into the table
    private void loadRows(int[][] data, int q, int cs) {
        model.setRowCount(0);
        for (int i = 0; i < data.length; i++) {
            model.addRow(new Object[]{"P" + (i + 1), data[i][0], data[i][1], data[i][2]});
        }
        qField.setText("" + q);
        csField.setText("" + cs);
    }

    private void renumber() {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt("P" + (i + 1), i, 0);
        }
    }

    // build a fresh process list from the table
    private List<Process> readProcesses() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        if (model.getRowCount() == 0) {
            throw new IllegalArgumentException("Add at least one process.");
        }
        List<Process> list = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            int arrival = Integer.parseInt(model.getValueAt(i, 1).toString().trim());
            int burst = Integer.parseInt(model.getValueAt(i, 2).toString().trim());
            int priority = Integer.parseInt(model.getValueAt(i, 3).toString().trim());
            list.add(new Process("P" + (i + 1), arrival, burst, priority));
        }
        return list;
    }

    private void runSimulation() {
        try {
            int q = Integer.parseInt(qField.getText().trim());
            int cs = Integer.parseInt(csField.getText().trim());
            String algo = (String) algoBox.getSelectedItem();

            StringBuilder all = new StringBuilder();
            ganttHolder.removeAll();

            if (algo.equals("SJF") || algo.equals("Both")) {
                String text = runSJF(readProcesses(), cs);
                all.append(text).append("\n");
                ganttHolder.add(new GanttPanel("SJF", parseSlots(text, true)));
            }
            if (algo.equals("ACS") || algo.equals("Both")) {
                String text = runACS(readProcesses(), q, cs);
                all.append(text).append("\n");
                ganttHolder.add(new GanttPanel("ACS", parseSlots(text, false)));
            }

            outputArea.setText(all.toString());
            outputArea.setCaretPosition(0);
            ganttHolder.revalidate();
            ganttHolder.repaint();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter whole numbers only.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    // runs SJF and returns everything it prints
    private String runSJF(List<Process> list, int cs) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            SJFScheduler sjf = new SJFScheduler(list, cs);
            sjf.run();
            sjf.printTimeline();
            sjf.printResults();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }

    // runs ACS and returns everything it prints
    private String runACS(List<Process> list, int q, int cs) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            ACSSimulator acs = new ACSSimulator(list, q, cs);
            acs.run();
            acs.printResults();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }

    // reads the printed log and gets one label per time unit
    private List<String> parseSlots(String text, boolean sjf) {
        List<String> slots = new ArrayList<>();
        for (String line : text.split("\n")) {
            line = line.trim();
            if (sjf && line.startsWith("Time ") && line.contains(": ")) {
                slots.add(line.substring(line.lastIndexOf(": ") + 2).trim());
            } else if (!sjf && line.startsWith("T=") && line.contains("Running: ")) {
                int start = line.indexOf("Running: ") + 9;
                int end = line.indexOf(" |", start);
                slots.add(line.substring(start, end));
            }
        }
        return slots;
    }

    // draws the Gantt chart
    static class GanttPanel extends JPanel {
        private String title;
        private List<String> slots;
        private Color[] palette = {
            new Color(120, 180, 240), new Color(250, 170, 120), new Color(140, 210, 140),
            new Color(230, 140, 200), new Color(240, 220, 120), new Color(160, 150, 230)
        };

        GanttPanel(String title, List<String> slots) {
            this.title = title;
            this.slots = slots;
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        public Dimension getPreferredSize() {
            return new Dimension(40 + slots.size() * 30, 110);
        }

        private Color colorFor(String label) {
            if (label.equals("Idle")) return Color.LIGHT_GRAY;
            if (label.equals("CS")) return new Color(200, 90, 90);
            try {
                int n = Integer.parseInt(label.substring(1));
                return palette[n % palette.length];
            } catch (Exception e) {
                return Color.WHITE;
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g.setColor(Color.BLACK);
            g.drawString(title + " Gantt Chart", 10, 18);

            int w = 30, h = 40, x0 = 10, y0 = 30;
            for (int i = 0; i < slots.size(); i++) {
                int x = x0 + i * w;
                g.setColor(colorFor(slots.get(i)));
                g.fillRect(x, y0, w, h);
                g.setColor(Color.BLACK);
                g.drawRect(x, y0, w, h);
                g.drawString(slots.get(i), x + 6, y0 + 25);
                g.drawString("" + i, x, y0 + h + 15);
            }
            g.drawString("" + slots.size(), x0 + slots.size() * w, y0 + h + 15);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}