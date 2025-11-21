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

    public MainFrame(List<Map<String, String>> books, Double numBooks, String jsonPath, java.util.function.Consumer<Map<String, String>> onShowDetails) {
        this.onShowDetails = onShowDetails;
        System.out.println("Initializing MainFrame with " + numBooks + " books.");
        this.books = books;
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
                    System.out.println("Attempting to load image from: " + coverPath);
                    ImageIcon image = null;
                    try {
                        if (coverPath != null && (coverPath.startsWith("http://") || coverPath.startsWith("https://"))) {
                            image = new ImageIcon(new java.net.URL(coverPath));
                        } else {
                            image = new ImageIcon(coverPath);
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading image from " + coverPath + " : " + e.getMessage());
                    }

                    // If image is empty (file/URL not found), create a placeholder
                    if (image == null || image.getIconWidth() == -1 || image.getIconHeight() == -1) {
                        System.err.println("Warning: Image not found or invalid at " + coverPath);
                    } else {
                        System.out.println("Image loaded successfully: " + image.getIconWidth() + "x" + image.getIconHeight());
                    }
                    
                    // Pass the book map and a callback to persist changes when added to cart
                    ProductCard product = new ProductCard(
                        image, 
                        currentBook, 
                        (book) -> {
                            // When a book is added to cart, save the books list back to JSON
                            try {
                                saveBooksToJson(this.books, this.jsonPath);
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
