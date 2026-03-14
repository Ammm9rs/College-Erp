package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private final AuthService auth;
    private final ERPService service;
    private final User user;

    private JPasswordField pfOld = new JPasswordField();
    private JPasswordField pfNew = new JPasswordField();
    private JPasswordField pfConfirm = new JPasswordField();

    public ChangePasswordDialog(Frame owner, User user, AuthService auth, ERPService service) {
        super(owner, "Change Password", true);
        this.user = user;
        this.auth = auth;
        this.service = service;

        setSize(350, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        form.add(new JLabel("Old Password:")); form.add(pfOld);
        form.add(new JLabel("New Password:")); form.add(pfNew);
        form.add(new JLabel("Confirm New:")); form.add(pfConfirm);
        add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton ok = new JButton("Update");
        ok.addActionListener(e -> doUpdate());
        btns.add(ok);
        add(btns, BorderLayout.SOUTH);
    }

    private void doUpdate() {
        String oldP = new String(pfOld.getPassword());
        String newP = new String(pfNew.getPassword());
        String confP = new String(pfConfirm.getPassword());

        // 1. Verify Old
        // Note: In real app, re-verify hash. For demo, assuming auth checks passed login.
        // But strict check:
        if (!auth.verify(user.getUsername(), oldP)) {
            JOptionPane.showMessageDialog(this, "Old password incorrect.");
            return;
        }

        // 2. Check Match
        if (!newP.equals(confP)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.");
            return;
        }

        // 3. Update
        try {
            String newHash = auth.hash(newP);
            service.changePassword(user.getUsername(), newHash);
            JOptionPane.showMessageDialog(this, "Password updated!");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}