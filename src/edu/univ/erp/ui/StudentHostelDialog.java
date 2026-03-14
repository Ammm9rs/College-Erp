package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StudentHostelDialog extends JDialog {

    public StudentHostelDialog(Frame owner, User user, ERPService service) {
        super(owner, "Hostel Request", true);
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 1, 15, 15));
        form.setBorder(new EmptyBorder(30, 30, 30, 30));

        form.add(new JLabel("Select Room Type:"));
        JComboBox<String> cbType = new JComboBox<>(new String[]{"Single Occupancy", "Double Occupancy", "Dormitory"});
        form.add(cbType);

        JButton submit = new JButton("Submit Request");
        submit.setBackground(Color.decode("#234E8A"));
        submit.setForeground(Color.WHITE);
        submit.setFont(new Font("SansSerif", Font.BOLD, 14));

        submit.addActionListener(e -> {
            try {
                service.requestHostel(user.getUserId(), cbType.getSelectedItem().toString());
                JOptionPane.showMessageDialog(this, "Request Submitted Successfully!");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(submit);
        add(form, BorderLayout.CENTER);
    }
}