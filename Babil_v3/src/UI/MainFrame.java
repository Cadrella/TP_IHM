package UI;
import javax.swing.*;

import Models.ProductCard;
import Models.ProductCardExpanded;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JPanel {
    private List<Map<String, String>> books;
    private String jsonPath = null;
    private java.util.function.Consumer<Map<String, String>> onShowDetails = null;

    public MainFrame(List<Map<String, String>> books, Double numBooks, String jsonPath) {
        this(books, numBooks, jsonPath, null);
    }

    // Helper to recursively collect ProductCard instances from a container
    private void collectProductCards(Container parent, java.util.List<ProductCard> out) {
        for (Component c : parent.getComponents()) {
            if (c instanceof ProductCard) out.add((ProductCard)c);
            if (c instanceof Container) collectProductCards((Container)c, out);
        }
    }

    public MainFrame(List<Map<String, String>> books, Double numBooks, String jsonPath, java.util.function.Consumer<Map<String, String>> onShowDetails) {
        this.onShowDetails = onShowDetails;
        System.out.println("Initializing MainFrame with " + numBooks + " books.");
        this.books = books;
        // Ensure images for these books are being preloaded into the shared ImageCache
        try {
            Models.ImageCache.preload(this.books);
        } catch (Exception ex) {
            System.err.println("Image preload failed: " + ex.getMessage());
        }
        this.jsonPath = jsonPath;
        int numRows = (int)Math.ceil(numBooks / 4.0);
        System.out.println(numRows);
        System.out.println(numBooks);
        System.out.println(numBooks / 4);
        
        // Create a container with GridLayout for rows
        JPanel container = new JPanel(new GridLayout(numRows, 1, 0, 0));
        
        // Add rows, each with 4 columns using FlowLayout
        for (int row = 0; row < numRows; row++) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 40));
            for (int col = 0; col < 4; col++) {
                int index = row * 4 + col;
                if (index < numBooks) {
                    Map<String, String> currentBook = books.get(index);
                    String coverPath = currentBook.get("coverUrl");
                    // Use preloaded image if available; do not perform blocking network IO on the EDT
                    ImageIcon image = Models.ImageCache.get(coverPath);
                    if (image != null && image.getImage() != null) {
                        // scale thumbnail
                        Image img = image.getImage();
                        Image scaled = img.getScaledInstance(120, 160, Image.SCALE_SMOOTH);
                        image = new ImageIcon(scaled);
                    } else {
                        // image not yet available (will be loaded in background by ImageCache.preload)
                        image = null;
                    }
                    
                    // Pass the book map and a callback to persist changes when added to cart
                    ProductCard product = new ProductCard(
                        image, 
                        currentBook, 
                        (book) -> {
                            // When a book is added to cart, save the books list back to JSON
                            try {
                                saveBooksToJson(this.books, this.jsonPath);
                                // refresh header badge count
                                try { Utils.AppState.refreshCartCount(); } catch (Exception ignored) {}
                            } catch (Exception ex) {
                                System.err.println("Failed to save books.json: " + ex.getMessage());
                            }
                        },
                        ProductCard.Context.MAINFRAME,
                        null,
                        () -> {
                            // When show details is clicked, trigger the callback
                            if (onShowDetails != null) {
                                onShowDetails.accept(currentBook);
                            }
                        }
                    );

                    rowPanel.add(product);
                }
            }
            container.add(rowPanel);
        }
        
        setLayout(new BorderLayout());
        
        // Create a wrapper panel to center the container
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.add(container);
        
        add(centerWrapper, BorderLayout.CENTER);
        
        // Schedule a one-time reload of thumbnails shortly after construction
        javax.swing.Timer thumbTimer = new javax.swing.Timer(600, ev -> {
            // Find all ProductCard components and ask them to reload thumbnails
            java.util.List<ProductCard> cards = new java.util.ArrayList<>();
            collectProductCards(centerWrapper, cards);
            for (ProductCard pc : cards) {
                try { pc.reloadThumbnail(); } catch (Exception ex) {}
            }
        });
        thumbTimer.setRepeats(false);
        thumbTimer.start();
        // If books list is empty
        if(books.isEmpty()) {
            return;
        }
    }

    private void saveBooksToJson(List<Map<String, String>> books, String path) throws java.io.IOException {
        if (path == null || path.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < books.size(); i++) {
            Map<String, String> b = books.get(i);
            sb.append("  {\n");
            sb.append("    \"coverUrl\": \"").append(escapeJson(b.getOrDefault("coverUrl", ""))).append("\",\n");
            sb.append("    \"title\": \"").append(escapeJson(b.getOrDefault("title", ""))).append("\",\n");
            sb.append("    \"author\": \"").append(escapeJson(b.getOrDefault("author", ""))).append("\",\n");
            sb.append("    \"price\": \"").append(escapeJson(b.getOrDefault("price", ""))).append("\",\n");
            sb.append("    \"rating\": \"").append(escapeJson(b.getOrDefault("rating", ""))).append("\",\n");
            sb.append("    \"type\": \"").append(escapeJson(b.getOrDefault("type", ""))).append("\",\n");
            sb.append("    \"stock\": \"").append(escapeJson(b.getOrDefault("stock", "0"))).append("\",\n");
            sb.append("    \"added_to_cart\": \"").append(escapeJson(b.getOrDefault("added_to_cart", "false"))).append("\"\n");
            sb.append("  }");
            if (i < books.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        java.nio.file.Files.write(java.nio.file.Paths.get(path), sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private Map<String, String> getBookById(String id) {
        for (Map<String, String> b : books) {
            if (id.equals(b.get("id"))) return b;
        }
        return null;
    }

    private void showExpandedCard(String id) {
        Map<String, String> book = getBookById(id);
        if (book == null) return;

        removeAll();
        setLayout(new BorderLayout());

        ProductCardExpanded expanded = new ProductCardExpanded(book);
        add(expanded, BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}
