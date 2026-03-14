package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DashboardFrame extends JFrame {

    // --- Palette ---
    private static final Color BG_WHITE = Color.decode("#FFFFFF");
    private static final Color PAGE_BG = Color.decode("#F7FAFF");
    private static final Color SIDEBAR_BG = Color.decode("#EFF6FF");
    private static final Color PRIMARY = Color.decode("#234E8A");
    private static final Color CARD_BORDER = Color.decode("#E6EDF6");
    private static final Color TILE_HOVER = Color.decode("#EEF6FF");
    private static final Color MENU_HOVER = Color.decode("#E6F0FF");
    private static final Color SELECTED_MENU = Color.decode("#DDEEFF");

    private final User user;
    private final ERPService service;
    private final AuthService auth;
    private final JLabel statusBar = new JLabel("Ready");

    // Navigation
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private JPanel maintenanceBanner;
    private MenuItem currentSelectedItem = null;

    public DashboardFrame(User user, ERPService service, AuthService auth) {
        super("University ERP — " + user.getRole());
        this.user = user;
        this.service = service;
        this.auth = auth;

        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {}

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);

        buildUI();
        refreshMaintenanceState();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE_BG);
        setContentPane(root);

        // --- Maintenance Banner ---
        maintenanceBanner = new JPanel(new FlowLayout(FlowLayout.CENTER));
        maintenanceBanner.setBackground(new Color(220, 53, 69));
        JLabel bannerText = new JLabel("⚠ MAINTENANCE MODE: System is Read-Only ⚠");
        bannerText.setForeground(Color.WHITE);
        bannerText.setFont(bannerText.getFont().deriveFont(Font.BOLD, 12f));
        maintenanceBanner.add(bannerText);
        maintenanceBanner.setVisible(false);

        JPanel mainContainer = new JPanel(new BorderLayout());
        root.add(maintenanceBanner, BorderLayout.NORTH);
        root.add(mainContainer, BorderLayout.CENTER);

        // --- TOP BAR ---
        JPanel topBar = new JPanel(new BorderLayout(8,8));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(15, 25, 10, 25));

        JLabel logo = new JLabel("<html><span style='color:#234E8A;font-weight:700;font-size:18px;'>University</span> <span style='color:#666;font-size:18px;'>ERP</span></html>");
        topBar.add(logo, BorderLayout.WEST);

        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        profilePanel.setOpaque(false);

        JLabel userLabel = new JLabel("<html>Welcome, <b style='color:#234E8A;'>" + user.getUsername() + "</b></html>");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        AvatarIcon avatar = new AvatarIcon(user.getUsername().substring(0, 1).toUpperCase());

        profilePanel.add(userLabel);
        profilePanel.add(avatar);
        topBar.add(profilePanel, BorderLayout.EAST);

        mainContainer.add(topBar, BorderLayout.NORTH);

        // --- SIDEBAR ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, 100));
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        sidebar.setBackground(SIDEBAR_BG);

        // Menu Title
        JLabel sbTitle = new JLabel("Menu");
        sbTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        sbTitle.setForeground(PRIMARY);
        sbTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sbTitle.setBorder(new EmptyBorder(10,0,10,0));
        sidebar.add(sbTitle);

        // --- MENU ITEMS ---
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setBackground(SIDEBAR_BG);

        addMenuItem(menuContainer, "Dashboard", true, this::showDashboard);

        if (user.getRole() == User.Role.STUDENT) {
            addMenuItem(menuContainer, "Register For Courses", false, this::showRegister);
            addMenuItem(menuContainer, "My Course List", false, this::showMyCourses);
            addMenuItem(menuContainer, "Grades", false, this::showGrades);
            addMenuItem(menuContainer, "Timetable", false, this::showTimetable);
            addMenuItem(menuContainer, "Fees", false, this::showFees);
            addMenuItem(menuContainer, "Hostel Request", false, this::showHostel);
        } else if (user.getRole() == User.Role.ADMIN) {
            addMenuItem(menuContainer, "Create Section", false, this::createSection);
            addMenuItem(menuContainer, "Add Users", false, this::showAddUser);
            addMenuItem(menuContainer, "Assign Instructor", false, () -> new AssignInstructorDialog(this, service).setVisible(true));
            addMenuItem(menuContainer, "Delete Section", false, () -> new DeleteSectionDialog(this, service).setVisible(true));
            addMenuItem(menuContainer, "Hostel Requests", false, this::showHostelRequests);
            addMenuItem(menuContainer, "Backup / Restore", false, () -> new BackupDialog(this).setVisible(true)); // NEW
            addMenuItem(menuContainer, "Toggle Maintenance", false, this::toggleMaintenance);
        } else {
            addMenuItem(menuContainer, "My Sections", false, this::showMyCourses);
        }

        sidebar.add(menuContainer);

        // --- BOTTOM ---
        sidebar.add(Box.createVerticalGlue());
        addMenuItem(sidebar, "Settings", false, () -> {
            new ChangePasswordDialog(this, user, auth, service).setVisible(true);
        });
        addMenuItem(sidebar, "Logout", false, this::doLogout);

        // --- MAIN CONTENT ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setOpaque(false);

        mainContentPanel.add(createHomeView(), "HOME");

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, mainContentPanel);
        split.setResizeWeight(0);
        split.setDividerSize(0);
        split.setBorder(null);
        split.setBackground(PAGE_BG);
        mainContainer.add(split, BorderLayout.CENTER);
    }

    // --- HOME VIEW ---
    private JPanel createHomeView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20,30,20,30));

        JPanel tiles = new JPanel(new GridLayout(0, 3, 25, 25));
        tiles.setOpaque(false);

        if (user.getRole() == User.Role.STUDENT) {
            tiles.add(createRoundedTile("Register", "Browse & Add Classes", this::showRegister));
            tiles.add(createRoundedTile("My Courses", "View Enrolled", this::showMyCourses));
            tiles.add(createRoundedTile("Timetable", "Weekly Schedule", this::showTimetable));
            tiles.add(createRoundedTile("Grades", "View Grade Sheet", this::showGrades));
            tiles.add(createRoundedTile("Fees", "View Invoices", this::showFees));
            tiles.add(createRoundedTile("Hostel", "Request Room", this::showHostel));
        } else if (user.getRole() == User.Role.ADMIN) {
            tiles.add(createRoundedTile("Create Section", "Add new class", this::createSection));
            tiles.add(createRoundedTile("Add Users", "Create new accounts", this::showAddUser));
            tiles.add(createRoundedTile("Assign Instructor", "Link teacher", () -> new AssignInstructorDialog(this, service).setVisible(true)));
            tiles.add(createRoundedTile("Delete Section", "Remove empty classes", () -> new DeleteSectionDialog(this, service).setVisible(true)));
            tiles.add(createRoundedTile("Hostel Requests", "View applications", this::showHostelRequests));

            // --- NEW BACKUP TILE ---
            tiles.add(createRoundedTile("Backup / Restore", "Database tools", () -> new BackupDialog(this).setVisible(true)));

            tiles.add(createRoundedTile("Maintenance", "Toggle Read-Only", this::toggleMaintenance));
        } else {
            tiles.add(createRoundedTile("My Sections", "View your classes", this::showMyCourses));
        }

        JPanel topAlign = new JPanel(new BorderLayout());
        topAlign.setOpaque(false);
        topAlign.add(tiles, BorderLayout.NORTH);
        panel.add(topAlign, BorderLayout.CENTER);

        JPanel sb = new JPanel(new BorderLayout());
        sb.setOpaque(false);
        statusBar.setBorder(new EmptyBorder(6,8,6,8));
        statusBar.setForeground(new Color(0, 100, 0));
        sb.add(statusBar, BorderLayout.CENTER);
        panel.add(sb, BorderLayout.SOUTH);

        return panel;
    }

    // --- ACTIONS ---
    private void showDashboard() { cardLayout.show(mainContentPanel, "HOME"); setStatus("Overview"); }
    private void showRegister() { new StudentRegisterDialog(this, auth, service).setVisible(true); }
    private void createSection() { new AdminSectionDialog(this, service, user).setVisible(true); }
    private void showAddUser() { new AddUserDialog(this, auth).setVisible(true); }
    private void showTimetable() { new StudentTimetableDialog(this, user, service).setVisible(true); }
    private void showFees() { new StudentFeesDialog(this, user, service).setVisible(true); }
    private void showHostel() { new StudentHostelDialog(this, user, service).setVisible(true); }

    private void showHostelRequests() {
        java.util.List<String> reqs = service.getHostelRequests();
        if (reqs.isEmpty()) JOptionPane.showMessageDialog(this, "No pending requests.");
        else JOptionPane.showMessageDialog(this, new JScrollPane(new JList<>(reqs.toArray(new String[0]))), "Requests", JOptionPane.PLAIN_MESSAGE);
    }

    private void showMyCourses() {
        if (user.getRole() == User.Role.STUDENT) {
            StudentCoursesPanel p = new StudentCoursesPanel(user, service, () -> showDashboard());
            mainContentPanel.add(p, "COURSES");
            cardLayout.show(mainContentPanel, "COURSES");
            setStatus("My Courses");
        } else if (user.getRole() == User.Role.INSTRUCTOR) {
            InstructorSectionsPanel p = new InstructorSectionsPanel(user, service, this::showDashboard);
            mainContentPanel.add(p, "INSTRUCTOR_SECTIONS");
            cardLayout.show(mainContentPanel, "INSTRUCTOR_SECTIONS");
            setStatus("My Sections");
        }
    }

    private void showGrades() {
        if (user.getRole() == User.Role.STUDENT) {
            StudentGradesPanel p = new StudentGradesPanel(user, service, this::showDashboard);
            mainContentPanel.add(p, "GRADES");
            cardLayout.show(mainContentPanel, "GRADES");
            setStatus("My Grades");
        }
    }

    private void toggleMaintenance() {
        service.setMaintenance(!service.isMaintenanceOn());
        refreshMaintenanceState();
    }

    private void refreshMaintenanceState() {
        boolean isOn = service.isMaintenanceOn();
        maintenanceBanner.setVisible(isOn);
        if(isOn) {
            statusBar.setText("Maintenance Mode is ON");
            statusBar.setForeground(Color.RED);
        } else {
            statusBar.setText("System Normal (Ready)");
            statusBar.setForeground(new Color(0, 100, 0));
        }
    }

    private void doLogout() {
        dispose();
        new LoginFrame(auth, service).setVisible(true);
    }

    private void setStatus(String msg) { statusBar.setText(msg); }

    // --- HELPER: Sidebar Items ---
    private void addMenuItem(JPanel parent, String text, boolean isSelected, Runnable action) {
        MenuItem item = new MenuItem(text, action);
        if (isSelected) { item.setSelected(true); currentSelectedItem = item; }
        parent.add(item);
        parent.add(Box.createVerticalStrut(2));
    }

    private JPanel createRoundedTile(String title, String subtitle, Runnable onClick) {
        RoundedTile card = new RoundedTile();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel t = new JLabel("<html><b style='color:#234E8A;font-size:16px;'>" + title + "</b></html>");
        JLabel s = new JLabel("<html><span style='color:#6B6B6B;font-size:12px;'>" + subtitle + "</span></html>");
        JPanel text = new JPanel(new GridLayout(2,1,0,5));
        text.setOpaque(false); text.add(t); text.add(s);
        card.add(text, BorderLayout.CENTER);
        MouseAdapter listener = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
            @Override public void mouseEntered(MouseEvent e) { card.setHover(true); }
            @Override public void mouseExited(MouseEvent e) { card.setHover(false); }
        };
        card.addMouseListener(listener); text.addMouseListener(listener); t.addMouseListener(listener); s.addMouseListener(listener);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return card;
    }

    // --- COMPONENTS ---
    private static class RoundedTile extends JPanel {
        private boolean hover = false;
        public RoundedTile() { setOpaque(false); }
        public void setHover(boolean h) { this.hover = h; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            g2.setColor(hover ? TILE_HOVER : BG_WHITE);
            g2.fillRoundRect(0, 0, w-1, h-1, 20, 20);
            g2.setColor(CARD_BORDER);
            g2.drawRoundRect(0, 0, w-1, h-1, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class MenuItem extends JPanel {
        private boolean selected = false;
        private final Runnable action;
        private final JLabel label;
        public MenuItem(String text, Runnable action) {
            this.action = action;
            setLayout(new BorderLayout());
            setBackground(selected ? SELECTED_MENU : SIDEBAR_BG);
            setPreferredSize(new Dimension(220, 34));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            setBorder(new EmptyBorder(0, 20, 0, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label = new JLabel(text);
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            label.setForeground(PRIMARY.darker());
            add(label, BorderLayout.CENTER);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (currentSelectedItem != null) currentSelectedItem.setSelected(false);
                    setSelected(true); currentSelectedItem = MenuItem.this; action.run();
                }
                public void mouseEntered(MouseEvent e) { if(!selected) setBackground(MENU_HOVER); }
                public void mouseExited(MouseEvent e) { if(!selected) setBackground(SIDEBAR_BG); }
            });
        }
        public void setSelected(boolean b) {
            this.selected = b; setBackground(b ? SELECTED_MENU : SIDEBAR_BG);
            label.setFont(b ? new Font("SansSerif", Font.BOLD, 14) : new Font("SansSerif", Font.PLAIN, 14));
        }
    }

    private static class AvatarIcon extends JPanel {
        private final String letter;
        public AvatarIcon(String letter) { this.letter = letter; setOpaque(false); setPreferredSize(new Dimension(36, 36)); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(200, 220, 255)); g2.fillOval(0, 0, 35, 35);
            g2.setColor(PRIMARY); g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(letter)) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(letter, x, y); g2.dispose();
        }
    }
}