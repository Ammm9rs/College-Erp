package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;
import edu.univ.erp.util.ErrorHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;

public class AdminSectionDialog extends JDialog {
    private final ERPService service;
    private final User adminUser;

    // Fields
    private final JTextField tfCourseCode = new JTextField();
    private final JTextField tfCourseTitle = new JTextField();
    private final JSpinner spCredits = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));

    private final JTextField tfId = new JTextField();
    private final JSpinner spCap = new JSpinner(new SpinnerNumberModel(30, 1, 1000, 1));

    private final JTextField tfSem = new JTextField("Fall");
    private final JTextField tfYear = new JTextField("2025");
    private final JTextField tfTime = new JTextField("Mon 10:00");
    private final JTextField tfRoom = new JTextField("C101");
    private final JTextField tfRegDeadline = new JTextField("2025-12-01");
    private final JTextField tfDropDeadline = new JTextField("2025-12-15");

    public AdminSectionDialog(Frame owner, ERPService service, User adminUser) {
        super(owner, "Create New Section", true);
        this.service = service;
        this.adminUser = adminUser;

        setLayout(new BorderLayout());
        setSize(500, 600);
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- GROUP 1: Course Info ---
        JPanel pnlCourse = new JPanel(new GridLayout(3, 2, 10, 10));
        pnlCourse.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "1. Course Details", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), Color.decode("#234E8A")));

        pnlCourse.add(new JLabel("Course Code (e.g. CS101):")); pnlCourse.add(tfCourseCode);
        pnlCourse.add(new JLabel("Course Name (e.g. Java):"));  pnlCourse.add(tfCourseTitle);
        pnlCourse.add(new JLabel("Credits:"));                    pnlCourse.add(spCredits);

        mainPanel.add(pnlCourse);
        mainPanel.add(Box.createVerticalStrut(15));

        // --- GROUP 2: Section Info ---
        JPanel pnlSection = new JPanel(new GridLayout(8, 2, 10, 8));
        pnlSection.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "2. Section Logistics", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12), Color.decode("#234E8A")));

        pnlSection.add(new JLabel("Section ID (e.g. S1):"));      pnlSection.add(tfId);
        pnlSection.add(new JLabel("Capacity:"));                  pnlSection.add(spCap);
        pnlSection.add(new JLabel("Semester:"));                  pnlSection.add(tfSem);
        pnlSection.add(new JLabel("Year:"));                      pnlSection.add(tfYear);
        pnlSection.add(new JLabel("Day/Time:"));                  pnlSection.add(tfTime);
        pnlSection.add(new JLabel("Room:"));                      pnlSection.add(tfRoom);
        pnlSection.add(new JLabel("Reg Deadline (YYYY-MM-DD):")); pnlSection.add(tfRegDeadline);
        pnlSection.add(new JLabel("Drop Deadline (YYYY-MM-DD):"));pnlSection.add(tfDropDeadline);

        mainPanel.add(pnlSection);

        add(mainPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Create Section");
        ok.setBackground(Color.decode("#234E8A"));
        ok.setForeground(Color.WHITE);
        ok.addActionListener(e -> doSave());

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());

        btns.add(cancel);
        btns.add(ok);
        add(btns, BorderLayout.SOUTH);
    }

    private void doSave() {
        try {
            // 1. Basic Empty Checks
            if (tfId.getText().trim().isEmpty() || tfCourseCode.getText().trim().isEmpty()) {
                ErrorHandler.showWarning(this, "Section ID and Course Code are required.");
                return;
            }

            // 2. Capacity Check
            int capacity = (Integer) spCap.getValue();
            if (capacity <= 0) {
                ErrorHandler.showWarning(this, "Capacity must be a positive number.");
                return;
            }

            // 3. Parse Dates
            LocalDate regDeadline = null;
            LocalDate dropDeadline = null;
            String regTxt = tfRegDeadline.getText().trim();
            String dropTxt = tfDropDeadline.getText().trim();

            if (!regTxt.isEmpty()) regDeadline = LocalDate.parse(regTxt);
            if (!dropTxt.isEmpty()) dropDeadline = LocalDate.parse(dropTxt);

            // 4. Call Service
            service.createSection(
                    adminUser,
                    tfId.getText().trim(),
                    tfCourseCode.getText().trim(),
                    tfCourseTitle.getText().trim(),
                    (Integer) spCredits.getValue(),
                    capacity,
                    tfSem.getText().trim(),
                    Integer.parseInt(tfYear.getText().trim()),
                    tfTime.getText().trim(),
                    tfRoom.getText().trim(),
                    regDeadline,
                    dropDeadline
            );

            ErrorHandler.showInfo(this, "Success! Section created.");
            dispose();

        } catch (Exception ex) {
            String msg = ex.getMessage();

            // --- IMPROVED ERROR HANDLING HERE ---
            if (msg.contains("Duplicate entry")) {
                ErrorHandler.showWarning(this, "Section ID '" + tfId.getText() + "' already exists. Please choose a different ID.");
            } else if (msg.contains("Conflict")) {
                // Shows the "Course Code already exists as X" message nicely
                ErrorHandler.showWarning(this, msg);
            } else if (msg.contains("Parse")) {
                ErrorHandler.showWarning(this, "Invalid Date Format. Please use YYYY-MM-DD.");
            } else {
                ErrorHandler.showError(this, "Could not create section.", ex);
            }
        }
    }
}