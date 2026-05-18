package prog2.finalgroup;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class MyProgram extends JFrame {
    private static final String[] TABLE_COLUMNS = {
            "Full Name", "Email", "Address", "Age", "Residency", "District", "Gender"
    };

    private static final Color BG = new Color(0xF5F4F0);
    private static final Color PANEL_BG = new Color(0xFFFFFF);
    private static final Color HEADER_BG = new Color(0x1A1A2E);
    private static final Color ACCENT = new Color(0x4F7CAC);
    private static final Color ACCENT2 = new Color(0xC84B31);
    private static final Color ROW_ODD = new Color(0xFFFFFF);
    private static final Color ROW_EVEN = new Color(0xF0F4FA);
    private static final Color SEL_BG = new Color(0xD0E4F7);
    private static final Color SEL_FG = new Color(0x1A1A2E);
    private static final Color TEXT_MAIN = new Color(0x1A1A2E);
    private static final Color TEXT_MUTED = new Color(0x7A7A9A);
    private static final Color BORDER_C = new Color(0xDDDDE8);

    private final List<Citizen> allCitizens;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> genderFilter;
    private JSpinner ageMin;
    private JSpinner ageMax;

    public MyProgram(List<Citizen> citizens) {
        this.allCitizens = citizens;

        setTitle("Community Citizen Database");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 740);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        setJMenuBar(buildMenuBar());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildFilterBar(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        refreshTable(allCitizens);
    }

    public static void main(String[] args) {
        List<Citizen> citizens;
        try {
            citizens = MyProgramUtility.loadCitizens();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Could not read res/data.csv:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (citizens.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No citizen records were loaded from res/data.csv.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Citizen> finalCitizens = citizens;
        SwingUtilities.invokeLater(() -> {
            MyProgram window = new MyProgram(finalCitizens);
            window.setVisible(true);
        });
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu reportsMenu = new JMenu("Reports");
        reportsMenu.add(createReportItem("Gender Distribution",
                () -> MyProgramUtility.formatGenderReport(allCitizens)));
        reportsMenu.add(createReportItem("Residents per District",
                () -> MyProgramUtility.formatDistrictReport(allCitizens)));
        reportsMenu.add(createReportItem("Senior Citizens (60+)",
                () -> MyProgramUtility.formatSeniorReport(allCitizens)));
        reportsMenu.add(createReportItem("Average Age",
                () -> MyProgramUtility.formatAverageAgeReport(allCitizens)));
        reportsMenu.add(createReportItem("Names Sorted A–Z",
                () -> MyProgramUtility.formatSortedNamesReport(allCitizens)));
        reportsMenu.add(createReportItem("Residents vs Non-Residents",
                () -> MyProgramUtility.formatResidencyReport(allCitizens)));

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);
        menuBar.add(reportsMenu);
        return menuBar;
    }

    private JMenuItem createReportItem(String title, ReportSupplier supplier) {
        JMenuItem item = new JMenuItem(title);
        item.addActionListener(e -> showReport(title, supplier.get()));
        return item;
    }

    private void showReport(String title, String message) {
        JTextArea area = new JTextArea(message);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(480, 360));
        JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HEADER_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JLabel title = new JLabel("Community Citizen Database");
        title.setFont(new Font("Georgia", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(allCitizens.size() + " citizens loaded from res/data.csv");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(0xAABBDD));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(subtitle);

        JButton resetBtn = styledButton("Reset Filters", ACCENT2);
        resetBtn.addActionListener(e -> resetFilters());

        panel.add(left, BorderLayout.WEST);
        panel.add(resetBtn, BorderLayout.EAST);
        return panel;
    }

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
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });
        outer.add(padded(searchField));

        outer.add(sectionLabel("Residency"));
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
        agePanel.add(new JLabel("Min"));
        agePanel.add(new JLabel("Max"));
        ageMin = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        ageMax = new JSpinner(new SpinnerNumberModel(100, 0, 100, 1));
        styleSpinner(ageMin);
        styleSpinner(ageMax);
        ageMin.addChangeListener(e -> applyFilters());
        ageMax.addChangeListener(e -> applyFilters());
        agePanel.add(ageMin);
        agePanel.add(ageMax);
        outer.add(padded(agePanel));
        outer.add(Box.createVerticalGlue());
        return outer;
    }

    private JPanel buildTablePanel() {
        tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(SEL_BG);
        table.setSelectionForeground(SEL_FG);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        int[] widths = {140, 220, 260, 50, 110, 60, 70};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!selected) {
                    setBackground(row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                    setForeground(TEXT_MAIN);
                }
                if (col == 4 && !selected) {
                    String text = value == null ? "" : value.toString();
                    setForeground("Resident".equals(text)
                            ? new Color(0x2E7D32) : new Color(0xB71C1C));
                }
                setHorizontalAlignment(col == 3 || col == 5 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return this;
            }
        });

        table.setRowSorter(new TableRowSorter<>(tableModel));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL_BG);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HEADER_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));
        statusLabel = new JLabel();
        statusLabel.setForeground(new Color(0xAABBDD));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(statusLabel, BorderLayout.WEST);
        return panel;
    }

    private void applyFilters() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        String status = statusFilter == null ? "All" : (String) statusFilter.getSelectedItem();
        String gender = genderFilter == null ? "All" : (String) genderFilter.getSelectedItem();
        int minAge = ageMin == null ? 0 : (int) ageMin.getValue();
        int maxAge = ageMax == null ? 100 : (int) ageMax.getValue();

        List<Citizen> filtered = allCitizens.stream()
                .filter(c -> matchesFilters(c, query, status, gender, minAge, maxAge))
                .collect(Collectors.toList());

        refreshTable(filtered);
    }

    private boolean matchesFilters(Citizen c, String query, String status,
                                   String gender, int minAge, int maxAge) {
        if (!query.isEmpty()) {
            String haystack = (c.getFullName() + " " + c.getEmail() + " " + c.getAddress()).toLowerCase();
            if (!haystack.contains(query)) {
                return false;
            }
        }
        if (!"All".equals(status) && !c.getResidencyLabel().equalsIgnoreCase(status)) {
            return false;
        }
        if (!"All".equals(gender) && !c.getGenderLabel().equalsIgnoreCase(gender)) {
            return false;
        }
        return c.getAge() >= minAge && c.getAge() <= maxAge;
    }

    private void refreshTable(List<Citizen> citizens) {
        tableModel.setRowCount(0);
        for (Citizen c : citizens) {
            tableModel.addRow(new Object[]{
                    c.getFullName(),
                    c.getEmail(),
                    c.getAddress(),
                    c.getAge(),
                    c.getResidencyLabel(),
                    c.getDistrict(),
                    c.getGenderLabel()
            });
        }

        int[] residency = MyProgramUtility.countByResidency(citizens);
        statusLabel.setText(citizens.size() + " records shown  ·  "
                + residency[0] + " Residents  ·  "
                + residency[1] + " Non-Residents");
    }

    private void resetFilters() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        genderFilter.setSelectedIndex(0);
        ageMin.setValue(0);
        ageMax.setValue(100);
        applyFilters();
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(14, 14, 2, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField roundTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setForeground(TEXT_MAIN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return field;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return combo;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField()
                .setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    private JButton styledButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return button;
    }

    private JPanel padded(JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 12, 2, 12));
        panel.add(component);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        return panel;
    }

    @FunctionalInterface
    private interface ReportSupplier {
        String get();
    }
}
