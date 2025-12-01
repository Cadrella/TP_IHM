package UI;

import java.awt.*;
import javax.swing.*;
import Models.RoundedButton;

public class HomePage extends JPanel {
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
}
