package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddUserDialog extends JDialog {

    private final AuthService auth;

    private JTextField tfUsername = new JTextField();
    private JPasswordField tfPassword = new JPasswordField();
    private JComboBox<String> cbRole = new JComboBox<>(new String[]{"STUDENT", "INSTRUCTOR"});

    // Student fields container
    private JPanel studentFieldsPanel;
    private JTextField tfRoll = new JTextField();
    private JTextField tfProgram = new JTextField("CSE");
    private JTextField tfYear = new JTextField("1");

    public AddUserDialog(Frame owner, AuthService auth) {
        super(owner, "Add User", true);
        this.auth = auth;

        setSize(400, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Common Fields ---
        mainContainer.add(createLabel("Username:"));
        mainContainer.add(tfUsername);
        mainContainer.add(Box.createVerticalStrut(10));

        mainContainer.add(createLabel("Password:"));
        mainContainer.add(tfPassword);
        mainContainer.add(Box.createVerticalStrut(10));

        mainContainer.add(createLabel("Role:"));
        mainContainer.add(cbRole);
        mainContainer.add(Box.createVerticalStrut(10));

        // --- Student Specific Fields (Grouped) ---
        studentFieldsPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        studentFieldsPanel.setBorder(BorderFactory.createTitledBorder("Student Profile"));

        studentFieldsPanel.add(new JLabel("Roll No:"));
        studentFieldsPanel.add(tfRoll);
        studentFieldsPanel.add(new JLabel("Program:"));
        studentFieldsPanel.add(tfProgram);
        studentFieldsPanel.add(new JLabel("Year:"));
        studentFieldsPanel.add(tfYear);

        mainContainer.add(studentFieldsPanel);

        // Logic to hide/show student fields
        cbRole.addActionListener(e -> toggleFields());

        add(mainContainer, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Create User");
        ok.setBackground(Color.decode("#234E8A"));
        ok.setForeground(Color.WHITE);
        ok.addActionListener(e -> doSave());

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());

        btns.add(cancel);
        btns.add(ok);
        add(btns, BorderLayout.SOUTH);

        // Initial state check
        toggleFields();
    }

    private void toggleFields() {
        String role = (String) cbRole.getSelectedItem();
        boolean isStudent = "STUDENT".equals(role);
        studentFieldsPanel.setVisible(isStudent);
        pack(); // Resize window to fit content
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void doSave() {
        try {
            String user = tfUsername.getText().trim();
            String pass = new String(tfPassword.getPassword()).trim();
            String role = cbRole.getSelectedItem().toString();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Password required!");
                return;
            }

            // 1) Insert into AUTH DB
            String userSql = "INSERT INTO users_auth (user_id, username, role, password_hash) VALUES (?, ?, ?, ?)";
            try (var conn = DBConnection.getAuthConnection();
                 var ps = conn.prepareStatement(userSql)) {
                ps.setString(1, user);
                ps.setString(2, user);
                ps.setString(3, role);
                ps.setString(4, auth.hash(pass));
                ps.executeUpdate();
            }

            // 2) If STUDENT, insert into ERP DB
            if (role.equals("STUDENT")) {
                String sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
                try (var conn = DBConnection.getERPConnection();
                     var ps = conn.prepareStatement(sql)) {
                    ps.setString(1, user);
                    ps.setString(2, tfRoll.getText().trim());
                    ps.setString(3, tfProgram.getText().trim());
                    ps.setInt(4, Integer.parseInt(tfYear.getText().trim()));
                    ps.executeUpdate();
                }
            } else if (role.equals("INSTRUCTOR")) {
                // Ensure instructor exists in erp db
                String sql = "INSERT INTO instructors (user_id, department) VALUES (?, 'General')";
                try (var conn = DBConnection.getERPConnection();
                     var ps = conn.prepareStatement(sql)) {
                    ps.setString(1, user);
                    ps.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "User created successfully!");
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}