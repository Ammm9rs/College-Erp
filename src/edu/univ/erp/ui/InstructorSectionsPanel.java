package edu.univ.erp.ui;

import edu.univ.erp.domain.GradeRecord;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorSectionsPanel extends JPanel {

    // --- Colors ---
    private static final Color PRIMARY_BLUE = Color.decode("#234E8A");
    private static final Color BG_TABLE_HEADER = Color.decode("#F1F5F9");
    private static final Color GRID_COLOR = Color.decode("#E2E8F0");

    private final User user;
    private final ERPService service;
    private final Runnable onBack;

    private JList<Section> sectionList;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statsLabel;

    private List<GradeRecord> currentGradeRecords = new ArrayList<>();

    public InstructorSectionsPanel(User user, ERPService service, Runnable onBack) {
        this.user = user;
        this.service = service;
        this.onBack = onBack;

        setBackground(Color.WHITE);
        buildUI();
        loadSections();
    }

    private void buildUI() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // ---- 1. Header ----
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Gradebook Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.decode("#111827"));

        JButton back = new JButton("←") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#F3F4F6"));
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        back.setPreferredSize(new Dimension(40, 40));
        back.setFont(new Font("SansSerif", Font.BOLD, 18));
        back.setForeground(Color.DARK_GRAY);
        back.setBorderPainted(false);
        back.setContentAreaFilled(false);
        back.setFocusPainted(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> onBack.run());

        header.add(title, BorderLayout.WEST);
        header.add(back, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ---- 2. Split View ----
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createSectionListPanel(), createGradeTablePanel());
        split.setDividerSize(5);
        split.setBorder(null);
        split.setOpaque(false);
        split.setResizeWeight(0.15);

        add(split, BorderLayout.CENTER);

        // ---- 3. Bottom Action Bar ----
        add(createBottomBar(), BorderLayout.SOUTH);
    }

    private JPanel createSectionListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel lbl = new JLabel("Select Section");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.add(lbl, BorderLayout.NORTH);

        sectionList = new JList<>();
        sectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionList.setFixedCellHeight(45);
        sectionList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        sectionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                if (value instanceof Section) {
                    Section s = (Section) value;
                    l.setText("<html><b>" + s.getCourseCode() + "</b> <span style='color:gray;font-size:10px;'>(" + s.getId() + ")</span></html>");
                }
                if (isSelected) {
                    l.setBackground(Color.decode("#DCEFFD"));
                    l.setForeground(PRIMARY_BLUE);
                }
                return l;
            }
        });

        sectionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Section s = sectionList.getSelectedValue();
                if (s != null) loadGradebook(s.getId());
            }
        });

        JScrollPane scroll = new JScrollPane(sectionList);
        scroll.setBorder(BorderFactory.createLineBorder(GRID_COLOR));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel createGradeTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        String[] cols = {"Roll No", "Quiz (20%)", "Midterm (30%)", "Final (50%)", "Total Score"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 1 || col == 2 || col == 3;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.setFont(new Font("SansSerif", Font.PLAIN, 15));
        table.setGridColor(GRID_COLOR);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(BG_TABLE_HEADER);
        header.setForeground(Color.decode("#374151"));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(GRID_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel createBottomBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Left: Stats Label
        statsLabel = new JLabel("Select a section.");
        statsLabel.setFont(new Font("Monospaced", Font.BOLD, 15));
        statsLabel.setForeground(Color.decode("#4B5563"));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        JButton btnExport = new RoundedButton("Export CSV", Color.WHITE, PRIMARY_BLUE);
        btnExport.setPreferredSize(new Dimension(120, 45));
        btnExport.addActionListener(e -> exportCSV());

        JButton btnImport = new RoundedButton("Import CSV", Color.WHITE, PRIMARY_BLUE);
        btnImport.setPreferredSize(new Dimension(120, 45));
        btnImport.addActionListener(e -> importCSV());

        leftPanel.add(btnExport);
        leftPanel.add(btnImport);
        leftPanel.add(statsLabel);

        p.add(leftPanel, BorderLayout.WEST);

        // Right: Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btns.setOpaque(false);

        JButton btnStats = new RoundedButton("View Full Stats", Color.WHITE, PRIMARY_BLUE);
        btnStats.addActionListener(e -> showStatsDialog());

        JButton btnSave = new RoundedButton("Save & Compute Grades", PRIMARY_BLUE, Color.WHITE);
        btnSave.setPreferredSize(new Dimension(240, 45));
        btnSave.addActionListener(e -> saveAllGrades());

        btns.add(btnStats);
        btns.add(btnSave);

        p.add(btns, BorderLayout.EAST);
        return p;
    }

    // --- CSV Logic (Updated to include Total) ---
    private void exportCSV() {
        if (currentGradeRecords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("grades.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
                // UPDATED HEADER
                pw.println("RollNo,Quiz,Midterm,Final,TotalScore");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    pw.printf("%s,%s,%s,%s,%s%n",
                            tableModel.getValueAt(i, 0),
                            tableModel.getValueAt(i, 1) == null ? "" : tableModel.getValueAt(i, 1),
                            tableModel.getValueAt(i, 2) == null ? "" : tableModel.getValueAt(i, 2),
                            tableModel.getValueAt(i, 3) == null ? "" : tableModel.getValueAt(i, 3),
                            tableModel.getValueAt(i, 4) == null ? "" : tableModel.getValueAt(i, 4) // Include Total
                    );
                }
                JOptionPane.showMessageDialog(this, "Export Successful!");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void importCSV() {
        Section section = sectionList.getSelectedValue();
        if (section == null) return;

        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()))) {
                String line = br.readLine(); // Skip header
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length < 4) continue; // We need at least Roll, Q, M, F
                    String roll = parts[0].trim();

                    // Find row in table
                    for(int i=0; i<tableModel.getRowCount(); i++) {
                        if(tableModel.getValueAt(i, 0).toString().equals(roll)) {
                            try {
                                double q = Double.parseDouble(parts[1]);
                                double m = Double.parseDouble(parts[2]);
                                double f = Double.parseDouble(parts[3]);

                                if(q>100 || m>100 || f>100 || q<0 || m<0 || f<0) throw new Exception("Score > 100");

                                tableModel.setValueAt(q, i, 1);
                                tableModel.setValueAt(m, i, 2);
                                tableModel.setValueAt(f, i, 3);
                            } catch(Exception e) { System.err.println("Skipping invalid row: " + roll); }
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "Import Complete! Click 'Save' to commit changes.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Import Failed: " + e.getMessage());
            }
        }
    }

    // --- Validation & Saving ---
    private void saveAllGrades() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        Section section = sectionList.getSelectedValue();
        if (section == null) return;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (i >= currentGradeRecords.size()) break;
            GradeRecord rec = currentGradeRecords.get(i);

            Double q = parseDouble(tableModel.getValueAt(i, 1));
            Double m = parseDouble(tableModel.getValueAt(i, 2));
            Double f = parseDouble(tableModel.getValueAt(i, 3));

            // VALIDATION
            if ((q!=null && (q<0 || q>100)) || (m!=null && (m<0 || m>100)) || (f!=null && (f<0 || f>100))) {
                JOptionPane.showMessageDialog(this,
                        "Error at Row " + (i+1) + ": Scores must be between 0 and 100.",
                        "Invalid Grade", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Call service with User object for security
                service.saveGrade(this.user, rec.getStudentId(), section.getId(), q, m, f);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        JOptionPane.showMessageDialog(this, "Grades Saved Successfully!");
        loadGradebook(section.getId());
    }

    // --- Standard Methods ---
    private void loadSections() {
        DefaultListModel<Section> model = new DefaultListModel<>();
        for (Section s : service.listSectionsForInstructor(user.getUserId())) {
            model.addElement(s);
        }
        sectionList.setModel(model);
        if (!model.isEmpty()) sectionList.setSelectedIndex(0);
    }

    private void loadGradebook(String sectionId) {
        currentGradeRecords = service.getGradebook(sectionId);
        tableModel.setRowCount(0);
        for (GradeRecord g : currentGradeRecords) {
            tableModel.addRow(new Object[]{
                    g.getRollNo(), g.getQuiz(), g.getMidterm(), g.getFinalExam(), g.getFinalScore()
            });
        }
        updateStats(sectionId);
    }

    private void updateStats(String sectionId) {
        ERPService.SectionStats stats = service.getSectionStats(sectionId);
        if (stats == null || stats.count == 0) {
            statsLabel.setText("No grades.");
        } else {
            statsLabel.setText(String.format("Avg: %.1f  |  High: %.1f  |  Low: %.1f", stats.avg, stats.max, stats.min));
        }
    }

    private void showStatsDialog() {
        Section section = sectionList.getSelectedValue();
        if (section == null) return;

        ERPService.SectionStats stats = service.getSectionStats(section.getId());
        String msg = String.format(
                "<html><div style='padding:15px; font-family:SansSerif; font-size:14px;'>" +
                        "<h2 style='margin-bottom:5px; color:#234E8A;'>Class Statistics: %s</h2>" +
                        "<hr>" +
                        "<table style='width:100%%; margin-top:10px;'>" +
                        "<tr><td><b>Total Students:</b></td><td>%d</td></tr>" +
                        "<tr><td><b>Average Score:</b></td><td>%.2f</td></tr>" +
                        "<tr><td><b style='color:green;'>Highest Score:</b></td><td>%.2f</td></tr>" +
                        "<tr><td><b style='color:red;'>Lowest Score:</b></td><td>%.2f</td></tr>" +
                        "</table>" +
                        "</div></html>",
                section.getCourseCode(), stats.count, stats.avg, stats.max, stats.min
        );

        JOptionPane.showMessageDialog(this, new JLabel(msg), "Statistics", JOptionPane.PLAIN_MESSAGE);
    }

    private Double parseDouble(Object value) {
        if (value == null) return null;
        String s = value.toString().trim();
        if (s.isEmpty()) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private static class RoundedButton extends JButton {
        private final Color bg;
        private final Color fg;

        public RoundedButton(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            this.fg = fg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setForeground(fg);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(160, 45));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            if(bg.equals(Color.WHITE)) {
                g2.setColor(fg);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}