package Models;
import javax.swing.*;

import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

public class ProductCard extends JPanel {
    // Context enum: MAINFRAME or CART
    public enum Context { MAINFRAME, CART }

    public ProductCard(ImageIcon cover, Map<String,String> book, Consumer<Map<String,String>> onAdd) {
        this(cover, book, onAdd, Context.MAINFRAME, null, null);
    }

    public ProductCard(ImageIcon cover, Map<String,String> book, Consumer<Map<String,String>> onAdd, Context context, Runnable onRemove) {
        this(cover, book, onAdd, context, onRemove, null);
    }

    public ProductCard(ImageIcon cover, Map<String,String> book, Consumer<Map<String,String>> onAdd, Context context, Runnable onRemove, Runnable onShowDetails) {

        String title = book.getOrDefault("title", "");
        String author = book.getOrDefault("author", "");
        double price = 0.0;
        double rating = 0.0;
        try { price = Double.parseDouble(book.getOrDefault("price", "0")); } catch (Exception e) {}
        try { rating = Double.parseDouble(book.getOrDefault("rating", "0")); } catch (Exception e) {}

        System.out.println("Cover: " + cover);
        // Set the product card layout
        setLayout(new BorderLayout());

        // Border of product card
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Book cover
        JLabel coverLabel = new JLabel(cover);
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220))); // Light horizontal border bottom

        // Book infos
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        JLabel authorLabel = new JLabel(author, SwingConstants.CENTER);
        JLabel priceLabel = new JLabel(price + " DA", SwingConstants.CENTER);
        JLabel ratingLabel = new JLabel(rating + "/5", SwingConstants.CENTER);

        // 'Add to cart' or 'Remove from cart' button depending on context
        JButton actionButton = new RoundedButton(context == Context.CART ? "Remove from cart" : "Add to cart", 15);
        actionButton.setBackground(new Color(140, 113, 92)); // Brown color 8C715C
        actionButton.setForeground(Color.WHITE); // White text
        actionButton.setFocusPainted(false);
        actionButton.setPreferredSize(new Dimension(1501, 30)); // Narrow button
        actionButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Pointer cursor

        // If the book already has added_to_cart set (and we're in MAINFRAME), update button text/state
        if (context == Context.MAINFRAME && "true".equalsIgnoreCase(book.getOrDefault("added_to_cart", "false"))) {
            actionButton.setText("Added");
            actionButton.setEnabled(false);
        }

        // Show details to expand the product card
        JButton showDetails = new RoundedButton(context == Context.CART ? "Show more" : "Show more", 15);
        showDetails.setBackground(new Color(140, 113, 92)); // Brown color 8C715C
        showDetails.setForeground(Color.WHITE); // White text
        showDetails.setFocusPainted(false);
        showDetails.setPreferredSize(new Dimension(150, 30)); // Narrow button
        showDetails.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Pointer cursor

        showDetails.addActionListener(e -> {
            if (onShowDetails != null) {
                onShowDetails.run();
            }
        });

        // Horizontal layout for price and rating
        JPanel priceAndRatingPanel = new JPanel(new GridLayout(1, 2));
        priceAndRatingPanel.add(priceLabel);
        priceAndRatingPanel.add(ratingLabel);

        // Vertical layout for all infos
        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        infoPanel.add(titleLabel);
        infoPanel.add(authorLabel);
        infoPanel.add(priceAndRatingPanel);
        
        // Wrap buttons in a panel to center
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(actionButton);
        buttonPanel.add(showDetails);
        infoPanel.add(buttonPanel);

        // Add action listener based on context
        if (context == Context.MAINFRAME) {
            // MAINFRAME: Add to cart and call onAdd callback
            actionButton.addActionListener(e -> {
                book.put("added_to_cart", "true");
                actionButton.setText("Added");
                actionButton.setEnabled(false);
                if (onAdd != null) onAdd.accept(book);
            });
        } else {
            // CART: Remove from cart, update JSON, and call onRemove callback
            actionButton.addActionListener(e -> {
                book.put("added_to_cart", "false");
                if (onAdd != null) onAdd.accept(book); // Persist the change to JSON
                if (onRemove != null) onRemove.run(); // Remove from UI
            });
        }

        // Add all to product card
        add(coverLabel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250, 360);
    }

}
