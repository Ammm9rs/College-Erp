package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.ERPService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import edu.univ.erp.util.ErrorHandler;

public class LoginFrame extends JFrame {

    // --- Colors ---
    private static final Color BG_LEFT = Color.decode("#EFF6FF");
    private static final Color PRIMARY_BLUE = Color.decode("#234E8A");
    private static final Color LABEL_COLOR = Color.decode("#4B5563");
    private static final Color INPUT_BORDER = Color.decode("#CBD5E1");

    private final AuthService auth;
    private final ERPService service;

    private JTextField tfUser;
    private JPasswordField pfPass;
    private JLabel lblStatus;

    // IMAGES
    private BufferedImage bgImage;   // The Mountain
    private BufferedImage logoImage; // The IIITD Logo

    // --- Fonts ---
    private final Font fontTitle = new Font("Helvetica Neue", Font.BOLD, 32);
    private final Font fontSub = new Font("Helvetica Neue", Font.PLAIN, 15);
    private final Font fontLabel = new Font("Helvetica Neue", Font.BOLD, 13);
    private final Font fontInput = new Font("Helvetica Neue", Font.PLAIN, 14);

    public LoginFrame(AuthService auth, ERPService service) {
        super("University ERP - Login");
        this.auth = auth;
        this.service = service;

        // --- LOAD IMAGES ---
        try {
            // Load Mountain
            URL bgUrl = getClass().getResource("/login_bg.jpg");
            if (bgUrl != null) bgImage = ImageIO.read(bgUrl);
            else bgImage = ImageIO.read(new File("login_bg.jpg"));

            // Load Logo (The new part)
            URL logoUrl = getClass().getResource("/logo.png");
            if (logoUrl != null) logoImage = ImageIO.read(logoUrl);
            else logoImage = ImageIO.read(new File("logo.png"));

        } catch (Exception ignored) {
            System.out.println("Check image paths: login_bg.jpg and logo.png");
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        // Split Pane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        split.setDividerLocation(400);
        split.setDividerSize(0);
        split.setBorder(null);

        setContentPane(split);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LEFT);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setOpaque(false);
        JLabel logo = new JLabel("<html><span style='color:#234E8A;font-size:24px;font-weight:800;font-family:SansSerif;'>University</span> <span style='color:#666666;font-size:24px;font-weight:300;font-family:SansSerif;'>ERP</span></html>");
        header.add(logo);
        panel.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_LEFT);

        JLabel title = new JLabel("Log in");
        title.setFont(fontTitle);
        title.setForeground(Color.decode("#111827"));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);

        JLabel sub = new JLabel("Welcome back to the portal.");
        sub.setFont(fontSub);
        sub.setForeground(Color.GRAY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sub);
        form.add(Box.createVerticalStrut(35));

        form.add(createLabel("Username"));
        form.add(Box.createVerticalStrut(8));
        tfUser = new RoundedTextField(15);
        styleField(tfUser);
        form.add(tfUser);
        form.add(Box.createVerticalStrut(20));

        form.add(createLabel("Password"));
        form.add(Box.createVerticalStrut(8));
        pfPass = new RoundedPasswordField(15);
        styleField(pfPass);
        form.add(pfPass);
        form.add(Box.createVerticalStrut(35));

        JButton btnLogin = createButton("Log In", PRIMARY_BLUE, Color.WHITE);
        btnLogin.addActionListener(e -> doLogin());
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(15));

        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.RED);
        lblStatus.setFont(fontSub);
        form.add(lblStatus);

        wrapper.add(form);
        panel.add(wrapper, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 1. Draw Mountain Background (Layer 1)
                if (bgImage != null) {
                    double scaleWidth = (double) getWidth() / bgImage.getWidth();
                    double scaleHeight = (double) getHeight() / bgImage.getHeight();
                    double scale = Math.max(scaleWidth, scaleHeight);
                    int w = (int) (bgImage.getWidth() * scale);
                    int h = (int) (bgImage.getHeight() * scale);
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;
                    g2.drawImage(bgImage, x, y, w, h, null);
                } else {
                    g.setColor(PRIMARY_BLUE);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                // 2. Draw IIITD Logo Overlay (Layer 2)
                if (logoImage != null) {
                    // Resize logic: Make it 200px wide, maintain aspect ratio
                    int logoWidth = 200;
                    int logoHeight = (int) ((double)logoWidth / logoImage.getWidth() * logoImage.getHeight());

                    // Position: Top Right with 30px padding
                    int x = getWidth() - logoWidth - 30;
                    int y = 30;

                    // Optional: Add a subtle white glow/shadow if logo is dark
                    // g2.setColor(new Color(255,255,255, 50));
                    // g2.fillRoundRect(x-10, y-10, logoWidth+20, logoHeight+20, 15, 15);

                    g2.drawImage(logoImage, x, y, logoWidth, logoHeight, null);
                }
            }
        };
    }

    // --- Helpers ---

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(LABEL_COLOR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleField(JTextField tf) {
        tf.setFont(fontInput);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        tf.setPreferredSize(new Dimension(320, 50));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Helvetica Neue", Font.BOLD, 15));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setPreferredSize(new Dimension(320, 50));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

   // Add import

    private void doLogin() {
        String un = tfUser.getText().trim();
        String pw = new String(pfPass.getPassword());

        if (un.isEmpty() || pw.isEmpty()) {
            ErrorHandler.showWarning(this, "Please enter both username and password.");
            return;
        }

        try {
            if (auth.verify(un, pw)) {
                User u = auth.getSessionUser();
                dispose();
                new DashboardFrame(u, service, auth).setVisible(true);
            } else {
                // Shows "Incorrect password" or "Account locked"
                lblStatus.setText(auth.getLastError());
                // Optional: Shake animation here
            }
        } catch (Exception e) {
            // Catches Database down errors
            ErrorHandler.showError(this, "Could not connect to the server.", e);
        }
    }

    // --- Custom Components ---

    private static class RoundedTextField extends JTextField {
        private final int radius;
        public RoundedTextField(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBorder(new EmptyBorder(10, 15, 10, 15));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            g2.setColor(INPUT_BORDER);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    private static class RoundedPasswordField extends JPasswordField {
        private final int radius;
        public RoundedPasswordField(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBorder(new EmptyBorder(10, 15, 10, 15));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            g2.setColor(INPUT_BORDER);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            super.paintComponent(g);
            g2.dispose();
        }
    }
}