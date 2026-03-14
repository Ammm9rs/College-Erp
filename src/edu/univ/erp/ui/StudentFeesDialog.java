package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StudentFeesDialog extends JDialog {

    public StudentFeesDialog(Frame owner, User user, ERPService service) {
        super(owner, "Fee Invoice", true);
        setSize(450, 550);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.decode("#234E8A"));
        header.setBorder(new EmptyBorder(25, 25, 25, 25));
        JLabel title = new JLabel("INVOICE");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Content
        double total = service.calculateTotalFee(user.getUserId());

        JTextArea invoice = new JTextArea();
        invoice.setFont(new Font("Monospaced", Font.PLAIN, 14));
        invoice.setEditable(false);
        invoice.setBorder(new EmptyBorder(25, 25, 25, 25));

        StringBuilder sb = new StringBuilder();
        sb.append("Student: ").append(user.getUsername()).append("\n");
        sb.append("Date:    ").append(java.time.LocalDate.now()).append("\n");
        sb.append("----------------------------------------\n\n");
        sb.append(String.format("%-25s %10s\n", "Description", "Amount"));
        sb.append(String.format("%-25s %10s\n", "-----------", "------"));
        sb.append(String.format("%-25s %10.2f\n", "Semester Base Fee", 5000.00));
        sb.append(String.format("%-25s %10.2f\n", "Tuition (Credits)", total - 5000));
        sb.append(String.format("%-25s %10.2f\n", "Library Charge", 0.00));
        sb.append("\n\n");
        sb.append(String.format("%-25s %10.2f\n", "TOTAL PAYABLE", total));

        invoice.setText(sb.toString());
        add(invoice, BorderLayout.CENTER);

        // Pay Button
        JButton payBtn = new JButton("Print / Pay Now");
        payBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        payBtn.setBackground(Color.decode("#234E8A"));
        payBtn.setForeground(Color.WHITE);
        payBtn.setPreferredSize(new Dimension(0, 50));
        payBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Redirecting to Payment Gateway... (Simulation)"));
        add(payBtn, BorderLayout.SOUTH);
    }
}