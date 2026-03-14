package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StudentRegisterDialog extends JDialog {
    private final ERPService service;
    private final AuthService auth;
    private final DefaultListModel<Section> model = new DefaultListModel<>();
    private final JList<Section> list = new JList<>(model);
    private final JTextArea preview = new JTextArea();
    private final JTextField searchField = new JTextField();
    private List<Section> allSections = new ArrayList<>();

    public StudentRegisterDialog(Frame owner, AuthService auth, ERPService service) {
        super(owner, "Register for Courses", true);
        this.auth = auth;
        this.service = service;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(owner);
        buildUI();
        loadSections();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(Color.decode("#F7FAFF"));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        // LEFT
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(450, 0));

        // Search
        JPanel searchWrap = new JPanel(new BorderLayout(5, 0));
        searchWrap.setOpaque(false);
        searchWrap.add(new JLabel("Search:"), BorderLayout.WEST);
        searchField.setPreferredSize(new Dimension(0, 35));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        searchWrap.add(searchField, BorderLayout.CENTER);
        leftPanel.add(searchWrap, BorderLayout.NORTH);

        // List
        list.setCellRenderer(new SectionCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(e -> updatePreview(list.getSelectedValue()));
        JScrollPane leftScroll = new JScrollPane(list);
        leftPanel.add(leftScroll, BorderLayout.CENTER);

        // RIGHT
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setOpaque(false);
        preview.setEditable(false);
        preview.setFont(new Font("Monospaced", Font.PLAIN, 13));
        preview.setMargin(new Insets(10,10,10,10));
        JScrollPane rightScroll = new JScrollPane(preview);
        rightScroll.setBorder(BorderFactory.createTitledBorder("Section Details"));
        rightPanel.add(rightScroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        JButton btnRegister = new JButton("Register Selected");
        btnRegister.setBackground(Color.decode("#234E8A"));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.addActionListener(e -> doRegister());
        btnPanel.add(btnClose);
        btnPanel.add(btnRegister);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setResizeWeight(0.5);
        split.setBorder(null);
        split.setOpaque(false);
        root.add(split, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void loadSections() {
        allSections = service.listSections();
        filter();
    }

    private void filter() {
        String query = searchField.getText().toLowerCase().trim();
        model.clear();
        for (Section s : allSections) {
            String title = s.getCourseTitle() == null ? "" : s.getCourseTitle();
            if (s.getId().toLowerCase().contains(query) ||
                    s.getCourseCode().toLowerCase().contains(query) ||
                    title.toLowerCase().contains(query)) {
                model.addElement(s);
            }
        }
        if (!model.isEmpty()) list.setSelectedIndex(0);
    }

    private void updatePreview(Section s) {
        if (s == null) { preview.setText("Select a section."); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("Section ID   : ").append(s.getId()).append("\n");
        sb.append("Course Code  : ").append(s.getCourseCode()).append("\n");
        sb.append("Course Title : ").append(s.getCourseTitle() == null ? "N/A" : s.getCourseTitle()).append("\n");
        sb.append("Credits      : ").append(s.getCourseCredits()).append("\n"); // NEW
        sb.append("Instructor   : ").append(s.getInstructorId() == null ? "TBA" : s.getInstructorId()).append("\n");
        sb.append("Capacity     : ").append(s.getCapacity()).append("\n");
        sb.append("-------------------------------\n");
        sb.append("Location     : ").append(s.getRoom()).append("\n");
        sb.append("Schedule     : ").append(s.getDayTime()).append("\n");
        if(s.getRegDeadline()!=null) sb.append("Deadline     : ").append(s.getRegDeadline());
        preview.setText(sb.toString());
    }

    private void doRegister() {
        Section chosen = list.getSelectedValue();
        if (chosen == null) return;
        try {
            service.registerSection(auth.getSessionUser(), auth.getSessionUser().getUserId(), chosen.getId());
            JOptionPane.showMessageDialog(this, "Successfully registered!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- CLEANER RENDERER (Code + Name + Credits) ---
    private static class SectionCellRenderer extends JPanel implements ListCellRenderer<Section> {
        private JLabel lblCode = new JLabel();
        private JLabel lblTitle = new JLabel();
        private JLabel lblSection = new JLabel();

        public SectionCellRenderer() {
            setLayout(new BorderLayout(5, 0));
            setBorder(new EmptyBorder(8, 10, 8, 10));
            setOpaque(true);

            lblCode.setFont(new Font("SansSerif", Font.BOLD, 13));
            lblCode.setForeground(Color.decode("#234E8A"));

            lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblTitle.setForeground(Color.DARK_GRAY);

            lblSection.setFont(new Font("Monospaced", Font.PLAIN, 11));
            lblSection.setForeground(Color.GRAY);

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            left.setOpaque(false);
            left.add(lblCode);
            left.add(lblTitle);

            add(left, BorderLayout.CENTER);
            add(lblSection, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Section> list, Section value, int index, boolean isSelected, boolean cellHasFocus) {
            lblCode.setText(value.getCourseCode());

            // SHOW CREDITS HERE
            String titleStr = value.getCourseTitle() != null ? value.getCourseTitle() : "";
            if (value.getCourseCredits() > 0) {
                titleStr += " (" + value.getCourseCredits() + " Cr)";
            }
            lblTitle.setText(titleStr);

            lblSection.setText("[" + value.getId() + "]");

            if (isSelected) {
                setBackground(Color.decode("#DCEFFD"));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }
}