package UI;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import Models.RoundedButton;

public class HomePage extends JPanel {
    private Image backgroundImage = null;
    private String backgroundPath = null;

    /**
     * Set background image path. Path may be a URL (http/https/file) or local file path.
     * The image is loaded asynchronously and painted scaled to fill the panel.
     */
    public void setBackgroundImagePath(String path) {
        this.backgroundPath = path;
        if (path == null || path.isEmpty()) {
            this.backgroundImage = null;
            repaint();
            return;
        }
        // Try to get from ImageCache first
        try {
            Models.ImageCache.preload(java.util.Collections.singletonList(java.util.Collections.singletonMap("coverUrl", path)));
            javax.swing.ImageIcon ic = Models.ImageCache.get(path);
            if (ic != null && ic.getImage() != null) {
                this.backgroundImage = ic.getImage();
                repaint();
                return;
            }
        } catch (Exception ignored) {}

        // Otherwise load asynchronously and set image when ready
        new Thread(() -> {
            try {
                BufferedImage bi = null;
                if (path.startsWith("http://") || path.startsWith("https://")) {
                    bi = ImageIO.read(new java.net.URL(path));
                } else {
                    // attempt file URL then plain path
                    try {
                        bi = ImageIO.read(new java.net.URL("file:" + path));
                    } catch (Exception ex) {
                        bi = ImageIO.read(new java.io.File(path));
                    }
                }
                if (bi != null) {
                    this.backgroundImage = bi;
                    javax.swing.SwingUtilities.invokeLater(() -> repaint());
                }
            } catch (Exception e) {
                // ignore load failures
            }
        }, "HomePageBgLoad").start();
    }

    public String getBackgroundImagePath() {
        return this.backgroundPath;
    }

    public HomePage(Runnable onExplore, Runnable onCart) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        // Add vertical padding so content sits away from top/bottom edges
        setBorder(BorderFactory.createEmptyBorder(60, 40, 60, 40));

        // Top: Logo and title
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        top.setOpaque(false);
        ImageIcon logo = null;
        try {
            java.net.URL logoUrl = new java.net.URL("file:///C:/Users/Lenovo/Downloads/B%C4%81bil.png");
            logo = new ImageIcon(logoUrl);
            Image img = logo.getImage();
            if (img != null) {
                Image scaled = img.getScaledInstance(120, 60, Image.SCALE_SMOOTH);
                logo = new ImageIcon(scaled);
            }
        } catch (Exception e) {
            logo = new ImageIcon("Logo.svg");
        }
        JLabel logoLabel = new JLabel(logo);
        top.add(logoLabel);
        add(top, BorderLayout.NORTH);

        // Center: slogan and description
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel slogan = new JLabel("Discover great reads, anytime.");
        slogan.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(slogan);

        JTextArea description = new JTextArea("Bābil helps you explore a curated catalog of books, save favorites to your cart, and quickly view details. Use Explore to browse all books and Your Cart to view what you've added.");
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setEditable(false);
        description.setOpaque(false);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        description.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(description);

        add(center, BorderLayout.CENTER);

        // Bottom: Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttons.setOpaque(false);

        RoundedButton exploreBtn = new RoundedButton("Explore", 12);
        exploreBtn.setPreferredSize(new Dimension(140, 40));
        exploreBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exploreBtn.setBackground(new Color(140, 113, 92));
        exploreBtn.setForeground(Color.WHITE);
        exploreBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exploreBtn.addActionListener(e -> {
            if (onExplore != null) onExplore.run();
        });

        RoundedButton cartBtn = new RoundedButton("Your Cart", 12);
        cartBtn.setPreferredSize(new Dimension(140, 40));
        cartBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cartBtn.setBackground(new Color(140, 113, 92));
        cartBtn.setForeground(Color.WHITE);
        cartBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cartBtn.addActionListener(e -> {
            if (onCart != null) onCart.run();
        });

        buttons.add(exploreBtn);
        buttons.add(cartBtn);

        add(buttons, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.backgroundImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            int iw = backgroundImage.getWidth(this);
            int ih = backgroundImage.getHeight(this);
            if (iw > 0 && ih > 0) {
                // scale to cover the panel while preserving aspect ratio
                double scale = Math.max((double)w / iw, (double)h / ih);
                int dw = (int)(iw * scale);
                int dh = (int)(ih * scale);
                int dx = (w - dw) / 2;
                int dy = (h - dh) / 2;
                g2.drawImage(this.backgroundImage, dx, dy, dw, dh, this);
            } else {
                g2.drawImage(this.backgroundImage, 0, 0, w, h, this);
            }
            g2.dispose();
        }
    }
}
