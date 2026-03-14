package edu.univ.erp.ui;

import edu.univ.erp.domain.EnrolledView;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentTimetableDialog extends JDialog {

    public StudentTimetableDialog(Frame owner, User user, ERPService service) {
        super(owner, "Weekly Timetable", true);
        setSize(950, 650);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel("Weekly Class Schedule", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Table Setup
        String[] days = {"Time", "Mon", "Tue", "Wed", "Thu", "Fri"};
        String[] times = {"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00"};

        DefaultTableModel model = new DefaultTableModel(days, 0);
        for (String time : times) {
            model.addRow(new Object[]{time, "", "", "", "", ""});
        }

        JTable table = new JTable(model);
        table.setRowHeight(75); // Increased height to fit Title
        table.setEnabled(false);

        // --- ADDING SEPARATORS ---
        table.setShowGrid(true);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.decode("#CBD5E1")); // Visible Gray Border
        table.setIntercellSpacing(new Dimension(1, 1));

        // Styling
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(Color.decode("#F1F5F9"));

        // Center text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setVerticalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        // --- Logic: Populate Table ---
        // This is the line that was red. It works if ERPService has the method below.
        List<EnrolledView> courses = service.getStudentSchedule(user.getUserId());

        for (EnrolledView c : courses) {
            String raw = c.getDayTime();
            if (raw != null && raw.contains(" ")) {
                String[] parts = raw.split(" ");
                if (parts.length >= 2) {
                    String day = parts[0];
                    String time = parts[1];

                    int colIdx = -1;
                    if (day.equalsIgnoreCase("Mon")) colIdx = 1;
                    else if (day.equalsIgnoreCase("Tue")) colIdx = 2;
                    else if (day.equalsIgnoreCase("Wed")) colIdx = 3;
                    else if (day.equalsIgnoreCase("Thu")) colIdx = 4;
                    else if (day.equalsIgnoreCase("Fri")) colIdx = 5;

                    int rowIdx = -1;
                    for (int i = 0; i < times.length; i++) {
                        if (times[i].equals(time)) rowIdx = i;
                    }

                    if (colIdx != -1 && rowIdx != -1) {
                        // Display Course Code, Title, and Room
                        String cellHtml = String.format(
                                "<html><center><b style='font-size:12px; color:#234E8A;'>%s</b><br>" +
                                        "<span style='font-size:10px;'>%s</span><br>" +
                                        "<span style='color:gray; font-size:9px;'>Room: %s</span></center></html>",
                                c.getCourseCode(),
                                c.getCourseTitle() == null ? "" : c.getCourseTitle(),
                                c.getRoom()
                        );
                        model.setValueAt(cellHtml, rowIdx, colIdx);
                    }
                }
            }
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        btnPanel.add(close);
        add(btnPanel, BorderLayout.SOUTH);
    }
}