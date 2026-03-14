package edu.univ.erp.ui;

import edu.univ.erp.domain.EnrolledView;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import edu.univ.erp.util.ErrorHandler;

public class StudentCoursesPanel extends JPanel {
    private JComboBox<EnrolledView> dropCombo;
    private DefaultComboBoxModel<EnrolledView> dropModel;
    private final ERPService service;
    private final User user;
    private final JPanel cardsPanel;
    private final Runnable onBack;

    public StudentCoursesPanel(User user, ERPService service, Runnable onBack) {
        this.user = user;
        this.service = service;
        this.onBack = onBack;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(25, 40, 15, 40));

        JLabel title = new JLabel("My Enrolled Courses");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(30, 30, 30));

        JLabel subtitle = new JLabel("Manage your active classes and schedules.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        JPanel textWrap = new JPanel(new GridLayout(2,1, 0, 5));
        textWrap.setOpaque(false);
        textWrap.add(title);
        textWrap.add(subtitle);
        header.add(textWrap, BorderLayout.WEST);

        // --- NEW CIRCULAR BACK BUTTON ---
        JButton backBtn = new JButton("←") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#F3F4F6")); // Light gray circle
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        backBtn.setPreferredSize(new Dimension(40, 40));
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        backBtn.setForeground(Color.DARK_GRAY);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> onBack.run());

        header.add(backBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // 2. Scrollable Grid Area
        cardsPanel = new JPanel(new GridLayout(0, 3, 25, 25));
        cardsPanel.setBackground(new Color(248, 250, 252));
        cardsPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // --- Bottom panel for Drop feature ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBorder(new EmptyBorder(15, 16, 15, 16));
        bottom.setBackground(Color.WHITE);

        dropModel = new DefaultComboBoxModel<>();
        dropCombo = new JComboBox<>(dropModel);
        dropCombo.setPreferredSize(new Dimension(300, 35));

        dropCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof EnrolledView ev) {
                    lbl.setText(ev.getSectionId() + " — " + ev.getCourseCode());
                } else {
                    lbl.setText("No courses");
                }
                return lbl;
            }
        });

        JButton dropBtn = new JButton("Drop Selected Section");
        dropBtn.setBackground(Color.decode("#EF4444")); // Red for Drop
        dropBtn.setForeground(Color.WHITE);
        dropBtn.setFocusPainted(false);
        dropBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        dropBtn.addActionListener(e -> onDropClicked());

        bottom.add(new JLabel("Select to Drop: "));
        bottom.add(dropCombo);
        bottom.add(dropBtn);

        add(bottom, BorderLayout.SOUTH);

        loadCards();
    }

    private void loadCards() {
        cardsPanel.removeAll();
        List<EnrolledView> list = service.getStudentSchedule(user.getUserId());

        if (dropModel != null) {
            dropModel.removeAllElements();
            for (EnrolledView ev : list) dropModel.addElement(ev);
        }

        if (list.isEmpty()) {
            JLabel l = new JLabel("<html><center><h3 style='color:#666;'>No Enrollments Yet</h3><p style='color:#999;'>Go to 'Register' to add classes.</p></center></html>");
            cardsPanel.add(l);
        } else {
            for (EnrolledView ev : list) {
                cardsPanel.add(new CourseCard(ev));
            }
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    // Add import

    private void onDropClicked() {
        EnrolledView selected = (EnrolledView) dropCombo.getSelectedItem();

        // 1. Validation
        if (selected == null) {
            ErrorHandler.showWarning(this, "Please select a course to drop.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to drop " + selected.getCourseCode() + "?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // 2. Call Service
            service.dropSection(user, user.getUserId(), selected.getSectionId());

            ErrorHandler.showInfo(this, "Section dropped successfully.");
            loadCards(); // Refresh UI

        } catch (Exception ex) {
            // 3. Handle Business Logic Errors (e.g., "Deadline Passed")
            if (ex.getMessage().contains("deadline")) {
                ErrorHandler.showWarning(this, ex.getMessage());
            } else {
                ErrorHandler.showError(this, "Could not drop section.", ex);
            }
        }
    }

    private static class CourseCard extends JPanel {
        private final EnrolledView data;
        private static final Color BG_PINK = new Color(240, 156, 189);
        private static final Color TXT_MAROON = new Color(120, 28, 68);

        public CourseCard(EnrolledView data) {
            this.data = data;
            setOpaque(false);
            setPreferredSize(new Dimension(280, 320));
            setLayout(null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight(); int arc = 25;

            // Shadow
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fillRoundRect(3, 3, w - 6, h - 6, arc, arc);

            // Clip
            RoundRectangle2D roundedShape = new RoundRectangle2D.Float(0, 0, w - 6, h - 6, arc, arc);
            g2.clip(roundedShape);

            // 1. Top Color Block
            int headerHeight = 160;
            g2.setColor(BG_PINK);
            g2.fillRect(0, 0, w, headerHeight);

            // 2. BIG TEXT: COURSE TITLE (Name)
            // We use HTML to wrap text if it is long
            String titleText = "<html><div style='text-align:center; width:180px;'>" + data.getCourseTitle() + "</div></html>";

            JLabel renderer = new JLabel(titleText);
            renderer.setFont(new Font("SansSerif", Font.BOLD, 28)); // Slightly smaller to fit names
            renderer.setForeground(TXT_MAROON);
            renderer.setSize(w, headerHeight);
            renderer.setHorizontalAlignment(SwingConstants.CENTER);
            renderer.setVerticalAlignment(SwingConstants.CENTER);

            // Draw the label onto the panel image
            g2.translate(0, 0);
            renderer.paint(g2);
            g2.translate(0, 0);

            // 3. Bottom White Block
            g2.setColor(Color.WHITE);
            g2.fillRect(0, headerHeight, w, h - headerHeight);

            int startY = headerHeight + 30; int leftPad = 20;

            // 4. Details: COURSE CODE is now here
            g2.setColor(new Color(40, 40, 40));
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.drawString(data.getCourseCode(), leftPad, startY); // e.g. "CS101"

            g2.setColor(Color.GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.drawString("Section " + data.getSectionId() + "  |  " + data.getCredits() + " Credits", leftPad, startY + 20);

            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(leftPad, startY + 35, w - leftPad, startY + 35);

            g2.setColor(new Color(80, 80, 80));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            String info = "🕒 " + (data.getDayTime() == null ? "TBA" : data.getDayTime()) + "   📍 " + (data.getRoom() == null ? "?" : data.getRoom());
            g2.drawString(info, leftPad, startY + 60);

            g2.dispose();
        }
        private String truncate(String str, int limit) {
            if (str.length() > limit) return str.substring(0, limit) + "...";
            return str;
        }
    }
}