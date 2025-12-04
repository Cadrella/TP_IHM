package Models;
import javax.swing.*;

import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

public class ProductCard extends JPanel {
    // Context enum: MAINFRAME or CART
    public enum Context { MAINFRAME, CART }

    // fields for reload support
    private java.util.Map<String, String> bookData;
    private String coverUrl;
    private JLabel coverLabelRef;

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
        // store references for later reload
        this.bookData = book;
        this.coverUrl = (book == null) ? null : book.get("coverUrl");
        this.coverLabelRef = coverLabel;
        if (cover == null) {
            // start async load for thumbnail
            startAsyncThumbnailLoad(coverLabel, this.coverUrl);
        }

        // Book infos
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        JLabel authorLabel = new JLabel(author, SwingConstants.CENTER);
        JLabel priceLabel = new JLabel(price + " DA", SwingConstants.CENTER);
        JLabel ratingLabel = new JLabel(rating + "/5", SwingConstants.CENTER);

        // 'Add to cart' or 'Remove from cart' button depending on context
        JButton actionButton = new RoundedButton(context == Context.CART ? "Remove from cart" : "Add to cart", 15);
        actionButton.setBackground(new Color(140, 113, 92)); // Brown color 8C715C
        actionButton.setForeground(Color.WHITE); // White text
        actionButton.setPreferredSize(new Dimension(100, 28)); // Slightly smaller button
        actionButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Pointer cursor

        // Determine stock from book data
        String stockStrInit = book.getOrDefault("stock", "0");
        int stockInit = 0;
        try { stockInit = Integer.parseInt(stockStrInit); } catch (Exception ex) { stockInit = 0; }
        // If the book already has added_to_cart set (and we're in MAINFRAME), update button text/state
        if (context == Context.MAINFRAME) {
            if ("true".equalsIgnoreCase(book.getOrDefault("added_to_cart", "false"))) {
                actionButton.setText("Added");
                actionButton.setEnabled(false); // Disable the button
            }
            // Disable Add if out of stock
            if (stockInit <= 0) {
                actionButton.setText("Out of stock");
                actionButton.setEnabled(false);
            }
        }

        // Show details to expand the product card
        JButton showDetails = new RoundedButton("Show more", 15);
        showDetails.setBackground(new Color(140, 113, 92)); // Brown color 8C715C
        showDetails.setForeground(Color.WHITE); // White text
        showDetails.setPreferredSize(new Dimension(80, 28)); // Slightly smaller button
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

        // Stock label (centered)
        JLabel stockLabel = new JLabel();
        if (stockInit <= 0) stockLabel.setText("Out of stock");
        else if (stockInit == 1) stockLabel.setText("1 in stock");
        else stockLabel.setText(stockInit + " in stock");
        stockLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // Color: red when out of stock, green otherwise
        if (stockInit <= 0) {
            stockLabel.setForeground(Color.RED);
        } else {
            stockLabel.setForeground(new Color(0, 128, 0));
        }

        // Vertical layout for all infos (include stock)
        JPanel infoPanel = new JPanel(new GridLayout(5, 1));
        infoPanel.add(titleLabel);
        infoPanel.add(authorLabel);
        infoPanel.add(stockLabel);
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
                if (onAdd != null) onAdd.accept(book); // Update JSON
            });
        } else {
            // CART: Remove from cart, update JSON, and call onRemove callback
            actionButton.addActionListener(e -> {
                book.put("added_to_cart", "false");
                if (onAdd != null) onAdd.accept(book); // Update JSON
                if (onRemove != null) onRemove.run(); // Remove from UI
            });
        }

        // Add all to product card
        add(coverLabel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }

    // Reload thumbnail on demand (non-blocking)
    public void reloadThumbnail() {
        startAsyncThumbnailLoad(this.coverLabelRef, this.coverUrl);
    }

    private void startAsyncThumbnailLoad(JLabel coverLabel, String cp) {
        if (coverLabel == null || cp == null || cp.isEmpty()) return;
        coverLabel.setText("Loading...");
        new Thread(() -> {
            try {
                ImageIcon loaded = Models.ImageCache.get(cp);
                if (loaded == null) {
                    // try a direct blocking load via ImageIO (safer than ImageIcon(URL))
                    try {
                        java.awt.image.BufferedImage bi;
                        if (cp.startsWith("http://") || cp.startsWith("https://")) {
                            bi = javax.imageio.ImageIO.read(new java.net.URL(cp));
                        } else {
                            bi = javax.imageio.ImageIO.read(new java.net.URL("file:" + cp));
                        }
                        if (bi != null) loaded = new ImageIcon(bi);
                    } catch (Exception ex) {
                        loaded = null;
                    }
                }
                if (loaded != null && loaded.getImage() != null) {
                    Image img = loaded.getImage();
                    Image scaled = img.getScaledInstance(120, 160, Image.SCALE_SMOOTH);
                    ImageIcon thumb = new ImageIcon(scaled);
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        coverLabel.setText("");
                        coverLabel.setIcon(thumb);
                    });
                } else {
                    javax.swing.SwingUtilities.invokeLater(() -> coverLabel.setText("No image"));
                }
            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() -> coverLabel.setText("Image error"));
            }
        }, "ThumbLoad-" + Math.abs(cp.hashCode())).start();
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250, 360);
    }

}
