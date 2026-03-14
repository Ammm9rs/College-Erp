package edu.univ.erp.ui;

import edu.univ.erp.service.BackupService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class BackupDialog extends JDialog {
    private final BackupService service = new BackupService();

    public BackupDialog(Frame owner) {
        super(owner, "System Backup & Restore", true);
        setSize(450, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(2, 1, 20, 20));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        panel.setBackground(Color.decode("#F7FAFF"));

        // --- BACKUP BUTTON ---
        JButton btnBackup = new JButton("Create Full Backup");
        styleButton(btnBackup, Color.decode("#234E8A")); // Blue
        btnBackup.addActionListener(e -> {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                String path = service.backupDatabase();
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this, "Backup Successful!\nSaved to: " + path);
            } catch (Exception ex) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Backup Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- RESTORE BUTTON ---
        JButton btnRestore = new JButton("Restore from SQL File");
        styleButton(btnRestore, Color.decode("#EF4444")); // Red (Danger)
        btnRestore.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(System.getProperty("user.home") + "/Downloads");
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "WARNING: This will overwrite all current data.\nAre you sure?",
                        "Confirm Restore", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        service.restoreDatabase(fc.getSelectedFile());
                        setCursor(Cursor.getDefaultCursor());
                        JOptionPane.showMessageDialog(this, "System Restored Successfully! Please restart the app.");
                        System.exit(0); // Close app to force refresh
                    } catch (Exception ex) {
                        setCursor(Cursor.getDefaultCursor());
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Restore Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        panel.add(btnBackup);
        panel.add(btnRestore);
        add(panel, BorderLayout.CENTER);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}