package prog2.finalgroup;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

// ─────────────────────────────────────────────────────────────────────────────
//  Entry point
// ─────────────────────────────────────────────────────────────────────────────
public class MyProgram {
    public static void main(String[] args) {
        // Load data from CSV first
        List<String[]> data;
        try {
            data = CSVReader.readCSV("res/data.csv");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Could not read data.csv:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Launch GUI on the Event Dispatch Thread
        final List<String[]> finalData = data;
        SwingUtilities.invokeLater(() -> {
            Program window = new Program(finalData);
            window.setVisible(true);
        });
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CSV Reader  — reads data.csv into a List<String[]>
//  Expected columns (tab or comma separated):
//    FirstName, LastName, Email, Address, Age, Status, Score, Gender
// ─────────────────────────────────────────────────────────────────────────────
class CSVReader {
    public static List<String[]> readCSV(String filename) throws Exception {
        List<String[]> rows = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            // Support both tab-separated and comma-separated files
            String[] values = line.contains("\t") ? line.split("\t") : line.split(",");

            // Trim whitespace from each field
            for (int i = 0; i < values.length; i++) {
                values[i] = values[i].trim();
            }

            if (values.length >= 8) {   // only add rows that have all 8 columns
                rows.add(values);
            }
        }
        br.close();
        return rows;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Program  — the main JFrame GUI
// ─────────────────────────────────────────────────────────────────────────────
class Program extends JFrame {

    // ── Column names matching the CSV layout ──────────────────────────────
    private static final String[] COLUMNS = {
            "First Name", "Last Name", "Email", "Address", "Age", "Status", "Score", "Gender"
    };

    // ── Colours ───────────────────────────────────────────────────────────
    private static final Color BG        = new Color(0xF5F4F0);
    private static final Color PANEL_BG  = new Color(0xFFFFFF);
    private static final Color HEADER_BG = new Color(0x1A1A2E);
    private static final Color ACCENT    = new Color(0x4F7CAC);
    private static final Color ACCENT2   = new Color(0xC84B31);
    private static final Color ROW_ODD   = new Color(0xFFFFFF);
    private static final Color ROW_EVEN  = new Color(0xF0F4FA);
    private static final Color SEL_BG    = new Color(0xD0E4F7);
    private static final Color SEL_FG    = new Color(0x1A1A2E);
    private static final Color TEXT_MAIN = new Color(0x1A1A2E);
    private static final Color TEXT_MUTED= new Color(0x7A7A9A);
    private static final Color BORDER_C  = new Color(0xDDDDE8);

    // ── State ─────────────────────────────────────────────────────────────
    private final List<String[]> allRows;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;

    // Filter widgets
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> genderFilter;
    private JSpinner ageMin, ageMax;

    // ── Constructor ───────────────────────────────────────────────────────
    public Program(List<String[]> data) {
        this.allRows = data;

        setTitle("Resident Database");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 740);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildFilterBar(), BorderLayout.WEST);
        add(buildTablePanel(),BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        applyFilters();
    }

    // ── Header ────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(HEADER_BG);
        p.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JLabel title = new JLabel("Resident Database");
        title.setFont(new Font("Georgia", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel(allRows.size() + " records loaded from data.csv");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(0xAABBDD));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        JButton resetBtn = styledButton("Reset Filters", ACCENT2);
        resetBtn.addActionListener(e -> resetFilters());

        p.add(left,    BorderLayout.WEST);
        p.add(resetBtn,BorderLayout.EAST);
        return p;
    }

    // ── Filter sidebar ────────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel outer = new JPanel();
        outer.setBackground(PANEL_BG);
        outer.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_C));
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setPreferredSize(new Dimension(220, 0));

        outer.add(sectionLabel("Search"));
        searchField = roundTextField();
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate (javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate (javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });
        outer.add(padded(searchField));

        outer.add(sectionLabel("Status"));
        statusFilter = styledCombo(new String[]{"All", "Resident", "Non-Resident"});
        statusFilter.addActionListener(e -> applyFilters());
        outer.add(padded(statusFilter));

        outer.add(sectionLabel("Gender"));
        genderFilter = styledCombo(new String[]{"All", "Male", "Female"});
        genderFilter.addActionListener(e -> applyFilters());
        outer.add(padded(genderFilter));

        outer.add(sectionLabel("Age Range"));
        JPanel agePanel = new JPanel(new GridLayout(2, 2, 6, 4));
        agePanel.setOpaque(false);
        agePanel.add(new JLabel("Min")); agePanel.add(new JLabel("Max"));
        ageMin = new JSpinner(new SpinnerNumberModel(0,   0, 150, 1));
        ageMax = new JSpinner(new SpinnerNumberModel(150, 0, 150, 1));
        styleSpinner(ageMin); styleSpinner(ageMax);
        ageMin.addChangeListener(e -> applyFilters());
        ageMax.addChangeListener(e -> applyFilters());
        agePanel.add(ageMin); agePanel.add(ageMax);
        outer.add(padded(agePanel));

        outer.add(Box.createVerticalGlue());
        return outer;
    }

    // ── Table panel ───────────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(SEL_BG);
        table.setSelectionForeground(SEL_FG);
        table.setFillsViewportHeight(true);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = {90, 100, 230, 240, 50, 110, 55, 70};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Cell renderer: alternating rows + green/red status
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setFont(new Font("SansSerif", Font.PLAIN, 13));
                if (!sel) {
                    setBackground(row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                    setForeground(TEXT_MAIN);
                }
                // Colour-code Status column
                if (col == 5 && !sel) {
                    String v = val == null ? "" : val.toString();
                    setForeground(v.equals("Resident")
                            ? new Color(0x2E7D32)
                            : new Color(0xB71C1C));
                }
                setHorizontalAlignment(col == 4 || col == 6 ? CENTER : LEFT);
                return this;
            }
        });

        // Allow clicking column headers to sort
        table.setRowSorter(new TableRowSorter<>(tableModel));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL_BG);

        JPanel p = new JPanel(new BorderLayout());
        p.add(scroll);
        return p;
    }

    // ── Status bar ────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(HEADER_BG);
        p.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));
        statusLabel = new JLabel("Loading…");
        statusLabel.setForeground(new Color(0xAABBDD));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        p.add(statusLabel, BorderLayout.WEST);
        return p;
    }

    // ── Filter logic ──────────────────────────────────────────────────────
    private void applyFilters() {
        String query  = searchField   == null ? "" : searchField.getText().trim().toLowerCase();
        String status = statusFilter  == null ? "All" : (String) statusFilter.getSelectedItem();
        String gender = genderFilter  == null ? "All" : (String) genderFilter.getSelectedItem();
        int minAge = ageMin == null ? 0   : (int) ageMin.getValue();
        int maxAge = ageMax == null ? 150 : (int) ageMax.getValue();

        List<String[]> filtered = allRows.stream().filter(row -> {
            // text search across name, email, address
            if (!query.isEmpty()) {
                String hay = row[0] + " " + row[1] + " " + row[2] + " " + row[3];
                if (!hay.toLowerCase().contains(query)) return false;
            }
            // status filter  (col 5)
            if (!status.equals("All") && !row[5].equalsIgnoreCase(status)) return false;
            // gender filter  (col 7)
            if (!gender.equals("All") && !row[7].equalsIgnoreCase(gender)) return false;
            // age filter     (col 4)
            try {
                int age = Integer.parseInt(row[4].trim());
                if (age < minAge || age > maxAge) return false;
            } catch (NumberFormatException ignored) {}
            return true;
        }).collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (String[] r : filtered) tableModel.addRow(r);

        long residents    = filtered.stream().filter(r -> r[5].equalsIgnoreCase("Resident")).count();
        long nonResidents = filtered.stream().filter(r -> r[5].equalsIgnoreCase("Non-Resident")).count();
        statusLabel.setText(filtered.size() + " records shown  ·  "
                + residents + " Residents  ·  "
                + nonResidents + " Non-Residents");
    }

    private void resetFilters() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        genderFilter.setSelectedIndex(0);
        ageMin.setValue(0);
        ageMax.setValue(150);
        applyFilters();
    }

    // ── UI helpers ────────────────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(TEXT_MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(14, 14, 2, 14));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField roundTextField() {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setForeground(TEXT_MAIN);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        c.setBackground(Color.WHITE);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return c;
    }

    private void styleSpinner(JSpinner s) {
        s.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField()
                .setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return b;
    }

    private JPanel padded(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(4, 12, 2, 12));
        p.add(c);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return p;
    }
}