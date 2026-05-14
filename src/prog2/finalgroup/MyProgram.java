package prog2.finalgroup;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

// ─────────────────────────────────────────────────────────────────────────────
//  Entry point
// ─────────────────────────────────────────────────────────────────────────────
public class MyProgram {
    public static void main(String[] args) {
        List<String[]> data;
        try {
            data = CSVReader.readCSV("res/data.csv");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Could not read data.csv:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        final List<String[]> finalData = data;
        SwingUtilities.invokeLater(() -> new Program(finalData).setVisible(true));
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CSV Reader
// ─────────────────────────────────────────────────────────────────────────────
class CSVReader {
    public static List<String[]> readCSV(String filename) throws Exception {
        List<String[]> rows = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] values = parseCSVLine(line);
            for (int i = 0; i < values.length; i++) values[i] = values[i].trim();
            if (values.length >= 8) rows.add(values);
        }
        br.close();
        return rows;
    }

    private static String[] parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') { inQuotes = !inQuotes; }
            else if (c == ',' && !inQuotes) { tokens.add(current.toString()); current.setLength(0); }
            else { current.append(c); }
        }
        tokens.add(current.toString());
        return tokens.toArray(new String[0]);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  RoundedButton — pill-shaped custom button
// ─────────────────────────────────────────────────────────────────────────────
class RoundedButton extends JButton {
    private final Color normalBg;
    private final Color hoverBg;
    private final int arc;
    private boolean hovered = false;

    public RoundedButton(String text, Color normalBg, Color hoverBg, int arc) {
        super(text);
        this.normalBg = normalBg;
        this.hoverBg  = hoverBg;
        this.arc      = arc;
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("SansSerif", Font.BOLD, 12));
        setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true;  repaint(); }
            public void mouseExited (java.awt.event.MouseEvent e) { hovered = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hovered ? hoverBg : normalBg);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
        g2.dispose();
        super.paintComponent(g);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Program — main JFrame
// ─────────────────────────────────────────────────────────────────────────────
class Program extends JFrame {

    private static final String[] COLUMNS = {
            "First Name", "Last Name", "Email", "Address", "Age", "Status", "District", "Gender"
    };

    // ── White & Blue palette ──────────────────────────────────────────────
    private static final Color BG          = new Color(0xF0F5FF);
    private static final Color PANEL_BG    = new Color(0xFFFFFF);
    private static final Color SIDEBAR_BG  = new Color(0xFFFFFF);
    private static final Color HEADER_BG   = new Color(0x1A4DB3);
    private static final Color ACCENT      = new Color(0x2563EB);
    private static final Color ACCENT_HOV  = new Color(0x1D4ED8);
    private static final Color RESET_COLOR = new Color(0xEF4444);
    private static final Color RESET_HOV   = new Color(0xDC2626);
    private static final Color ROW_ODD     = new Color(0xFFFFFF);
    private static final Color ROW_EVEN    = new Color(0xEFF6FF);
    private static final Color SEL_BG      = new Color(0xBFDBFE);
    private static final Color SEL_FG      = new Color(0x1E3A8A);
    private static final Color TEXT_MAIN   = new Color(0x1E293B);
    private static final Color TEXT_MUTED  = new Color(0x64748B);
    private static final Color BORDER_C    = new Color(0xCBD5E1);

    // State
    private final List<String[]> allRows;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;

    // Filter widgets
    private JTextField nameField;

    // Sort
    private JCheckBox sortAZ, sortZA;

    // Age range (18–70)
    private JTextField ageMinField, ageMaxField;

    // Status
    private JCheckBox cbResident, cbNonResident;

    // District (1–20)
    private JTextField districtField;

    // Gender
    private JCheckBox cbMale, cbFemale;


    // Sidebar toggle
    private JPanel sidebarPanel;
    private boolean sidebarOpen = true;

    public Program(List<String[]> data) {
        this.allRows = data;
        setTitle("Resident Database");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        applyFilters();
    }

    // ── Header ────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(HEADER_BG);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 28));

        // Left side: hamburger + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JButton toggleBtn = new JButton("☰");
        toggleBtn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        toggleBtn.setForeground(Color.WHITE);
        toggleBtn.setBackground(new Color(0x2D62C8));
        toggleBtn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setOpaque(true);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.addActionListener(e -> toggleSidebar());
        left.add(toggleBtn);

        JLabel title = new JLabel("Resident Database");
        title.setFont(new Font("Georgia", Font.BOLD, 21));
        title.setForeground(Color.WHITE);
        left.add(title);

        JLabel sub = new JLabel("  —  " + allRows.size() + " records");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(new Color(0xA5C4FF));
        left.add(sub);

        // Right side: Reset button
        RoundedButton resetBtn = new RoundedButton("  Reset Filters", RESET_COLOR, RESET_HOV, 20);
        resetBtn.addActionListener(e -> resetFilters());

        p.add(left,     BorderLayout.WEST);
        p.add(resetBtn, BorderLayout.EAST);
        return p;
    }

    // ── Center wrapper (sidebar + table) ──────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(BG);
        sidebarPanel = buildFilterBar();
        center.add(sidebarPanel,      BorderLayout.WEST);
        center.add(buildTablePanel(), BorderLayout.CENTER);
        return center;
    }

    // ── Toggle sidebar visibility ─────────────────────────────────────────
    private void toggleSidebar() {
        sidebarOpen = !sidebarOpen;
        sidebarPanel.setVisible(sidebarOpen);
        revalidate();
        repaint();
    }

    // ── Filter sidebar ────────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel outer = new JPanel();
        outer.setBackground(SIDEBAR_BG);
        outer.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_C));
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setPreferredSize(new Dimension(240, 0));

        // Blue accent stripe at top
        JPanel stripe = new JPanel();
        stripe.setBackground(ACCENT);
        stripe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        stripe.setAlignmentX(LEFT_ALIGNMENT);
        outer.add(stripe);

        // "FILTERS" heading
        JLabel heading = new JLabel("FILTERS");
        heading.setFont(new Font("SansSerif", Font.BOLD, 11));
        heading.setForeground(ACCENT);
        heading.setBorder(BorderFactory.createEmptyBorder(18, 16, 10, 16));
        heading.setAlignmentX(LEFT_ALIGNMENT);
        outer.add(heading);

        // ── Name Search ─────────────────────────────────────────────────
        outer.add(sectionLabel("Search"));
        nameField = buildTextField("Search by first or last name...");
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        nameField.getDocument().addDocumentListener(new LiveDocListener(this::applyFilters));
        outer.add(padded(nameField));
        outer.add(Box.createVerticalStrut(4));
        outer.add(divider());

        // ── Sort Order ───────────────────────────────────────────────────
        outer.add(sectionLabel("Sort Order"));
        sortAZ = styledCheckBox("A → Z  (Last Name)");
        sortZA = styledCheckBox("Z → A  (Last Name)");
        sortAZ.addActionListener(e -> { if (sortAZ.isSelected()) sortZA.setSelected(false); applyFilters(); });
        sortZA.addActionListener(e -> { if (sortZA.isSelected()) sortAZ.setSelected(false); applyFilters(); });
        outer.add(padded(sortAZ));
        outer.add(padded(sortZA));
        outer.add(Box.createVerticalStrut(4));
        outer.add(divider());

        // ── Age Range (18–70) ────────────────────────────────────────────
        outer.add(sectionLabel("Age Range  (18 – 70)"));
        ageMinField = buildTextField("Min  (18)");
        ageMaxField = buildTextField("Max  (70)");
        ageMinField.getDocument().addDocumentListener(new LiveDocListener(this::applyFilters));
        ageMaxField.getDocument().addDocumentListener(new LiveDocListener(this::applyFilters));
        JPanel ageRow = new JPanel(new GridLayout(1, 2, 10, 0));
        ageRow.setOpaque(false);
        ageRow.add(labeledField("Min", ageMinField));
        ageRow.add(labeledField("Max", ageMaxField));
        outer.add(padded(ageRow));
        outer.add(Box.createVerticalStrut(4));
        outer.add(divider());

        // ── Status ───────────────────────────────────────────────────────
        outer.add(sectionLabel("Status"));
        cbResident    = styledCheckBox("Resident");
        cbNonResident = styledCheckBox("Non-Resident");
        cbResident.setSelected(true);
        cbNonResident.setSelected(true);
        cbResident.addActionListener(e -> applyFilters());
        cbNonResident.addActionListener(e -> applyFilters());
        outer.add(padded(cbResident));
        outer.add(padded(cbNonResident));
        outer.add(Box.createVerticalStrut(4));
        outer.add(divider());

        // ── District (1–20) ──────────────────────────────────────────────
        outer.add(sectionLabel("District  (1 – 20)"));
        districtField = buildTextField("Enter district number...");
        districtField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        districtField.getDocument().addDocumentListener(new LiveDocListener(this::applyFilters));
        outer.add(padded(districtField));
        outer.add(Box.createVerticalStrut(4));
        outer.add(divider());

        // ── Gender ───────────────────────────────────────────────────────
        outer.add(sectionLabel("Gender"));
        cbMale   = styledCheckBox("Male");
        cbFemale = styledCheckBox("Female");
        cbMale.setSelected(true);
        cbFemale.setSelected(true);
        cbMale.addActionListener(e -> applyFilters());
        cbFemale.addActionListener(e -> applyFilters());
        outer.add(padded(cbMale));
        outer.add(padded(cbFemale));

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
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(SEL_BG);
        table.setSelectionForeground(SEL_FG);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        int[] widths = {90, 100, 220, 240, 50, 110, 60, 70};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setFont(new Font("SansSerif", Font.PLAIN, 13));
                if (!sel) {
                    setBackground(row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                    setForeground(TEXT_MAIN);
                }
                // Color-code Status column
                if (col == 5 && !sel) {
                    String v = val == null ? "" : val.toString();
                    setForeground(v.equalsIgnoreCase("Resident")
                            ? new Color(0x15803D)
                            : new Color(0xB91C1C));
                    setFont(new Font("SansSerif", Font.BOLD, 12));
                }
                setHorizontalAlignment(col == 4 || col == 6 ? CENTER : LEFT);
                return this;
            }
        });

        table.setRowSorter(new TableRowSorter<>(tableModel));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL_BG);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.add(scroll);
        return p;
    }

    // ── Status bar ────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(HEADER_BG);
        p.setBorder(BorderFactory.createEmptyBorder(7, 22, 7, 22));
        statusLabel = new JLabel("Loading…");
        statusLabel.setForeground(new Color(0xA5C4FF));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        p.add(statusLabel, BorderLayout.WEST);
        return p;
    }

    // ── Filter logic ──────────────────────────────────────────────────────
    private void applyFilters() {
        final String query = nameField == null ? "" : nameField.getText().trim().toLowerCase();

        int parsedMin = 18, parsedMax = 70;
        if (ageMinField != null && !ageMinField.getText().trim().isEmpty()) {
            try { parsedMin = Math.max(18, Math.min(70, Integer.parseInt(ageMinField.getText().trim()))); }
            catch (NumberFormatException ignored) {}
        }
        if (ageMaxField != null && !ageMaxField.getText().trim().isEmpty()) {
            try { parsedMax = Math.max(18, Math.min(70, Integer.parseInt(ageMaxField.getText().trim()))); }
            catch (NumberFormatException ignored) {}
        }
        final int minAge = parsedMin;
        final int maxAge = parsedMax;

        final int districtNum;
        int tmp = -1;
        if (districtField != null && !districtField.getText().trim().isEmpty()) {
            try {
                int v = Integer.parseInt(districtField.getText().trim());
                if (v >= 1 && v <= 20) tmp = v;
            } catch (NumberFormatException ignored) {}
        }
        districtNum = tmp;

        boolean showResident    = cbResident    == null || cbResident.isSelected();
        boolean showNonResident = cbNonResident == null || cbNonResident.isSelected();
        boolean showMale        = cbMale        == null || cbMale.isSelected();
        boolean showFemale      = cbFemale      == null || cbFemale.isSelected();

        List<String[]> filtered = allRows.stream().filter(row -> {
            if (!query.isEmpty()) {
                String hay = (row[0] + " " + row[1]).toLowerCase();
                if (!hay.contains(query)) return false;
            }
            try {
                int age = Integer.parseInt(row[4].trim());
                if (age < minAge || age > maxAge) return false;
            } catch (NumberFormatException ignored) {}

            boolean isResident = row[5].equalsIgnoreCase("Resident");
            if (isResident  && !showResident)    return false;
            if (!isResident && !showNonResident) return false;

            if (districtNum != -1) {
                try {
                    if (Integer.parseInt(row[6].trim()) != districtNum) return false;
                } catch (NumberFormatException ignored) { return false; }
            }

            boolean isMale = row[7].equalsIgnoreCase("Male");
            if (isMale  && !showMale)   return false;
            if (!isMale && !showFemale) return false;

            return true;
        }).collect(Collectors.toList());

        boolean az = sortAZ != null && sortAZ.isSelected();
        boolean za = sortZA != null && sortZA.isSelected();
        if (az || za) {
            filtered.sort((a, b) -> {
                String la = a[1].toLowerCase();
                String lb = b[1].toLowerCase();
                return az ? la.compareTo(lb) : lb.compareTo(la);
            });
        }

        tableModel.setRowCount(0);
        for (String[] r : filtered) tableModel.addRow(r);

        long residents    = filtered.stream().filter(r -> r[5].equalsIgnoreCase("Resident")).count();
        long nonResidents = filtered.stream().filter(r -> r[5].equalsIgnoreCase("Non-Resident")).count();
        statusLabel.setText(filtered.size() + " records shown   ·   "
                + residents + " Residents   ·   "
                + nonResidents + " Non-Residents");
    }

    private void resetFilters() {
        nameField.setText("");
        sortAZ.setSelected(false);
        sortZA.setSelected(false);
        ageMinField.setText("");
        ageMaxField.setText("");
        cbResident.setSelected(true);
        cbNonResident.setSelected(true);
        districtField.setText("");
        cbMale.setSelected(true);
        cbFemale.setSelected(true);
        applyFilters();
    }
    private JCheckBox styledCheckBox(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setForeground(TEXT_MAIN);
        cb.setBackground(SIDEBAR_BG);
        cb.setOpaque(true);
        cb.setFocusPainted(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cb.setAlignmentX(LEFT_ALIGNMENT);
        return cb;
    }

    // ── UI helpers ────────────────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(TEXT_MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField buildTextField(String placeholder) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(0xA0AABF));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    Insets ins = getInsets();
                    int y = (getHeight() + g2.getFontMetrics().getAscent()) / 2 - 2;
                    g2.drawString(placeholder, ins.left + 2, y);
                    g2.dispose();
                }
            }
        };
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setForeground(TEXT_MAIN);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return f;
    }

    private JComboBox<String> buildCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("SansSerif", Font.PLAIN, 13));
        c.setBackground(Color.WHITE);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return c;
    }

    private JPanel labeledField(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);
        p.add(lbl,   BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel padded(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(2, 14, 2, 14));
        p.add(c);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        return p;
    }

    private JPanel divider() {
        JPanel d = new JPanel();
        d.setBackground(BORDER_C);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        d.setAlignmentX(LEFT_ALIGNMENT);
        return d;
    }
}

class LiveDocListener implements javax.swing.event.DocumentListener {
    private final Runnable onChange;
    LiveDocListener(Runnable onChange) { this.onChange = onChange; }
    public void insertUpdate (javax.swing.event.DocumentEvent e) { onChange.run(); }
    public void removeUpdate (javax.swing.event.DocumentEvent e) { onChange.run(); }
    public void changedUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
}