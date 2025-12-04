package Models;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ProductCardExpanded extends JPanel {
    public ProductCardExpanded(Map<String,String> book) {
        //System.out.println(book);

        // Outer panel centers the inner card
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        // Inner card: actual product content (keeps previous GridLayout)
        JPanel card = new JPanel(new GridLayout(1, 2));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(640, 360));

        // Book cover (non-blocking): try cache first, otherwise show placeholder and load in background
        String coverPath = book.get("coverUrl");
        ImageIcon cached = Models.ImageCache.get(coverPath);
        JLabel coverLabel = new JLabel();
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        if (cached != null && cached.getImage() != null) {
            Image img = cached.getImage();
            Image scaled = img.getScaledInstance(240, 320, Image.SCALE_SMOOTH);
            coverLabel.setIcon(new ImageIcon(scaled));
        } else if (coverPath != null && !coverPath.isEmpty()) {
            coverLabel.setText("Loading image...");
            final String cp = coverPath;
            new Thread(() -> {
                try {
                    java.awt.image.BufferedImage bi = null;
                    if (cp.startsWith("http://") || cp.startsWith("https://")) {
                        bi = javax.imageio.ImageIO.read(new java.net.URL(cp));
                    } else {
                        bi = javax.imageio.ImageIO.read(new java.net.URL("file:" + cp));
                    }
                    if (bi != null) {
                        Image scaled = bi.getScaledInstance(240, 320, Image.SCALE_SMOOTH);
                        ImageIcon scaledIcon = new ImageIcon(scaled);
                        // cache the original URL (preload will put a marker)
                        Models.ImageCache.preload(java.util.Collections.singletonList(java.util.Collections.singletonMap("coverUrl", cp)));
                        SwingUtilities.invokeLater(() -> {
                            coverLabel.setText("");
                            coverLabel.setIcon(scaledIcon);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> coverLabel.setText("Image unavailable"));
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> coverLabel.setText("Image unavailable"));
                    System.err.println("Error loading image from " + cp + " : " + e.getMessage());
                }
            }, "LoadCover-" + Math.abs(coverPath.hashCode())).start();
        } else {
            coverLabel.setText("No image");
        }

        // Book infos (left-aligned)
        JLabel titleLabel = new JLabel("Title: " + book.getOrDefault("title", ""), SwingConstants.LEFT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel authorLabel = new JLabel("Author: " +  book.getOrDefault("author", ""), SwingConstants.LEFT);
        authorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        authorLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel typeLabel = new JLabel("Type: " + book.getOrDefault("type", ""), SwingConstants.LEFT);
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel languageLabel = new JLabel("Language: " + book.getOrDefault("language", ""), SwingConstants.LEFT);
        languageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        languageLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Summary as a wrapping text area so long summaries display nicely
        JTextArea summaryLabel = new JTextArea("Summary: " + book.getOrDefault("summary", ""));
        summaryLabel.setLineWrap(true);
        summaryLabel.setWrapStyleWord(true);
        summaryLabel.setEditable(false);
        summaryLabel.setOpaque(false);
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        summaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Horizontal info
        JPanel horizontalInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        horizontalInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel ratingLabel = new JLabel("Rating: " + book.getOrDefault("rating", "") + "/5", SwingConstants.LEFT);
        ratingLabel.setHorizontalAlignment(SwingConstants.LEFT);
        ratingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel pagesLabel = new JLabel("Pages: " + book.getOrDefault("pages", ""), SwingConstants.LEFT);
        pagesLabel.setHorizontalAlignment(SwingConstants.LEFT);
        pagesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel publisherLabel = new JLabel("Publisher: " + book.getOrDefault("publisher", ""), SwingConstants.LEFT);
        publisherLabel.setHorizontalAlignment(SwingConstants.LEFT);
        publisherLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        horizontalInfo.add(ratingLabel);
        horizontalInfo.add(Box.createHorizontalStrut(12));
        horizontalInfo.add(pagesLabel);
        horizontalInfo.add(Box.createHorizontalStrut(12));
        horizontalInfo.add(publisherLabel);

        JLabel priceLabel = new JLabel("Price: " + book.getOrDefault("price", "") + " DA", SwingConstants.LEFT);
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        priceLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Stock display
        String stockStr = book.getOrDefault("stock", "0");
        JLabel stockDisplay = new JLabel("Stock: " + stockStr, SwingConstants.LEFT);
        stockDisplay.setAlignmentX(Component.LEFT_ALIGNMENT);
        stockDisplay.setHorizontalAlignment(SwingConstants.LEFT);
        // Color: red when out of stock, green otherwise
        try {
            int s = Integer.parseInt(stockStr);
            if (s <= 0) stockDisplay.setForeground(Color.RED);
            else stockDisplay.setForeground(new Color(0, 128, 0));
        } catch (Exception ex) {
            stockDisplay.setForeground(Color.RED);
        }

        // Vertical layout for all info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(8)); // Empty component for spacing
        infoPanel.add(authorLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(typeLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(languageLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(summaryLabel);
        infoPanel.add(Box.createVerticalStrut(12));
        infoPanel.add(horizontalInfo);
        infoPanel.add(Box.createVerticalStrut(12));
        infoPanel.add(stockDisplay);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(priceLabel);

        // Add all to inner card (cover on the left, info on the right)
        coverLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        infoPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        card.add(coverLabel);
        card.add(infoPanel);

        // Add the inner card to this panel and center it
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(card, gbc);
    }
}
