package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;

public class StudentGradesPanel extends JPanel {

    private final User user;
    private final ERPService service;
    private final Runnable onBack;
    private final DefaultTableModel tableModel;
    private List<ERPService.GradeRow> currentRows = new java.util.ArrayList<>();

    // CHANGED: Added Runnable onBack for navigation
    public StudentGradesPanel(User user, ERPService service, Runnable onBack) {
        this.user = user;
        this.service = service;
        this.onBack = onBack;

        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));
        setBackground(Color.WHITE);

        // --- HEADER WITH ROUND BACK BUTTON ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("My Grade Sheet");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.decode("#111827"));

        // Circular Back Button
        JButton backBtn = new JButton("←") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#F3F4F6")); // Light gray circle
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        backBtn.setPreferredSize(new Dimension(40, 40));
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        backBtn.setForeground(Color.DARK_GRAY);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> onBack.run());

        header.add(title, BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- TABLE ---
        String[] cols = {"Course", "Title", "Section", "Quiz", "Midterm", "End Sem", "Final Score"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(40); // Taller rows
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(Color.decode("#E2E8F0"));

        JTableHeader th = table.getTableHeader();
        th.setPreferredSize(new Dimension(0, 40));
        th.setFont(new Font("SansSerif", Font.BOLD, 13));
        th.setBackground(Color.decode("#F8FAFC"));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.decode("#E2E8F0")));
        scroll.getViewport().setBackground(Color.WHITE);

        add(scroll, BorderLayout.CENTER);

        // --- BOTTOM ACTIONS ---
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadGrades());

        JButton downloadBtn = new JButton("Download Transcript (CSV)");
        downloadBtn.setBackground(Color.decode("#234E8A"));
        downloadBtn.setForeground(Color.WHITE);
        downloadBtn.setFocusPainted(false);
        downloadBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        downloadBtn.addActionListener(e -> downloadTranscriptCsv());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(downloadBtn);
        bottom.add(refreshBtn);

        add(bottom, BorderLayout.SOUTH);

        loadGrades();
    }

    private void loadGrades() {
        tableModel.setRowCount(0);
        currentRows.clear();
        try {
            currentRows = service.getStudentGrades(user.getUserId());
            for (ERPService.GradeRow gr : currentRows) {
                tableModel.addRow(new Object[]{
                        gr.getCourseCode(), gr.getCourseTitle(), gr.getSectionId(),
                        gr.getQuiz(), gr.getMidterm(), gr.getFinalExam(), gr.getFinalScore()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void downloadTranscriptCsv() {
        if (currentRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No grades to export.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("transcript.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter out = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {
                out.println("Course,Title,Section,Quiz,Midterm,Final,Total");
                for (ERPService.GradeRow gr : currentRows) {
                    out.printf("%s,\"%s\",%s,%s,%s,%s,%s%n",
                            gr.getCourseCode(), gr.getCourseTitle(), gr.getSectionId(),
                            gr.getQuiz(), gr.getMidterm(), gr.getFinalExam(), gr.getFinalScore());
                }
                JOptionPane.showMessageDialog(this, "Transcript saved!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
}