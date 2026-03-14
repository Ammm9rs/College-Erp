package edu.univ.erp.ui;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AssignInstructorDialog extends JDialog {

    private final ERPService service;
    private JComboBox<String> cbSections;
    private JComboBox<String> cbInstructors;

    private List<Section> sectionList;
    private List<User> instructorList;

    public AssignInstructorDialog(Frame owner, ERPService service) {
        super(owner, "Assign Instructor", true);
        this.service = service;

        setSize(500, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 1, 10, 10));
        form.setBorder(new EmptyBorder(25, 25, 25, 25));

        form.add(new JLabel("1. Select Section:"));
        cbSections = new JComboBox<>();
        loadSections();
        form.add(cbSections);

        form.add(new JLabel("2. Select Instructor:"));
        cbInstructors = new JComboBox<>();
        loadInstructors();
        form.add(cbInstructors);

        add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // NEW: Unassign Button
        JButton clearBtn = new JButton("Unassign Current");
        clearBtn.setForeground(Color.RED);
        clearBtn.addActionListener(e -> doUnassign());

        JButton save = new JButton("Assign");
        save.setBackground(Color.decode("#234E8A"));
        save.setForeground(Color.WHITE);
        save.addActionListener(e -> doAssign());

        JButton cancel = new JButton("Close");
        cancel.addActionListener(e -> dispose());

        btns.add(clearBtn);
        btns.add(cancel);
        btns.add(save);
        add(btns, BorderLayout.SOUTH);
    }

    private void loadSections() {
        cbSections.removeAllItems();
        sectionList = service.listSections();
        for (Section s : sectionList) {
            String label = s.getId() + " - " + s.getCourseCode();
            if (s.getInstructorId() != null && !s.getInstructorId().equals("null")) {
                label += " (Taken by: " + s.getInstructorId() + ")";
            } else {
                label += " (Free)";
            }
            cbSections.addItem(label);
        }
    }

    private void loadInstructors() {
        instructorList = service.listInstructors();
        for (User u : instructorList) {
            cbInstructors.addItem(u.getUsername());
        }
    }

    private void doAssign() {
        if (sectionList.isEmpty() || instructorList.isEmpty()) return;
        int secIdx = cbSections.getSelectedIndex();
        int instIdx = cbInstructors.getSelectedIndex();
        if (secIdx == -1 || instIdx == -1) return;

        String sectionId = sectionList.get(secIdx).getId();
        String instructorId = instructorList.get(instIdx).getUserId();

        try {
            service.assignInstructorToSection(sectionId, instructorId);
            JOptionPane.showMessageDialog(this, "Instructor Assigned Successfully!");
            loadSections(); // Refresh UI
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void doUnassign() {
        if (sectionList.isEmpty()) return;
        int secIdx = cbSections.getSelectedIndex();
        if (secIdx == -1) return;

        String sectionId = sectionList.get(secIdx).getId();

        try {
            // Unassign by passing NULL
            service.assignInstructorToSection(sectionId, null);
            JOptionPane.showMessageDialog(this, "Section is now free.");
            loadSections();
        } catch (Exception ex) {
            // Fallback SQL if needed
            try {
                java.sql.Connection conn = edu.univ.erp.data.DBConnection.getERPConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE sections SET instructor_id = NULL WHERE id = ?");
                ps.setString(1, sectionId);
                ps.executeUpdate();
                conn.close();
                JOptionPane.showMessageDialog(this, "Section unassigned.");
                loadSections();
            } catch(Exception e2) { e2.printStackTrace(); }
        }
    }
}