package Models;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ProductCardExpanded extends JPanel {
    public ProductCardExpanded(Map<String,String> book) {
        System.out.println(book);

        // Outer panel centers the inner card
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        // Inner card: actual product content (keeps previous GridLayout)
        JPanel card = new JPanel(new GridLayout(1, 2));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(640, 360));

        // Book cover
        String coverPath = book.get("coverUrl");
        System.out.println("Attempting to load image from: " + coverPath);
        ImageIcon cover = null;
        try {
            if (coverPath != null && (coverPath.startsWith("http://") || coverPath.startsWith("https://"))) {
                cover = new ImageIcon(new java.net.URL(coverPath));
            } else {
                cover = new ImageIcon(coverPath);
            }
        } catch (Exception e) {
            System.err.println("Error loading image from " + coverPath + " : " + e.getMessage());
        }
        // Scale cover image to a smaller, consistent size for the expanded card
        if (cover != null && cover.getImage() != null) {
            Image img = cover.getImage();
            Image scaled = img.getScaledInstance(240, 320, Image.SCALE_SMOOTH);
            cover = new ImageIcon(scaled);
        }
        JLabel coverLabel = new JLabel(cover);
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

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

        // Vertical layout for all info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(8));
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
