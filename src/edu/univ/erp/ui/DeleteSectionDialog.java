package edu.univ.erp.ui;

import edu.univ.erp.domain.Section;
import edu.univ.erp.service.ERPService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DeleteSectionDialog extends JDialog {

    private final ERPService service;
    private JComboBox<String> cbSections;
    private List<Section> sectionList;

    public DeleteSectionDialog(Frame owner, ERPService service) {
        super(owner, "Delete Section", true);
        this.service = service;

        setSize(450, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(2, 1, 10, 10));
        form.setBorder(new EmptyBorder(30, 30, 30, 30));

        form.add(new JLabel("Select Section to Delete:"));
        cbSections = new JComboBox<>();
        loadSections();
        form.add(cbSections);

        add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(Color.decode("#EF4444")); // Red
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        deleteBtn.addActionListener(e -> doDelete());

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());

        btns.add(cancel);
        btns.add(deleteBtn);
        add(btns, BorderLayout.SOUTH);
    }

    private void loadSections() {
        cbSections.removeAllItems();
        sectionList = service.listSections();
        for (Section s : sectionList) {
            String title = s.getCourseTitle();
            String code = s.getCourseCode();

            String label;

            // CHANGED: Name first, then Code
            if (title != null && !title.isEmpty()) {
                label = title + " - " + code;
            } else {
                label = code; // Fallback if no name
            }

            cbSections.addItem(label);
        }
    }

    private void doDelete() {
        if (sectionList.isEmpty()) return;
        int idx = cbSections.getSelectedIndex();
        if (idx == -1) return;

        Section s = sectionList.get(idx);

        // Confirmation Popup shows details
        String msg = String.format("Are you sure you want to delete this section?\n\nCourse: %s\nSection ID: %s\n\nThis action cannot be undone.",
                s.getCourseCode(), s.getId());

        int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            service.deleteSection(s.getId());
            JOptionPane.showMessageDialog(this, "Section deleted successfully.");
            loadSections(); // Refresh list
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
        }
    }
}