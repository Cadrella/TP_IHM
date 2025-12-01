package UI;

import java.util.List;
import java.util.Map;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import Models.ProductCard;
import Models.TotalCartComponent;

public class cartMainFrame extends JPanel {
    private List<Map<String, String>> allBooks; // Keep reference to all books for persistence
    private String jsonPath = "data/books.json";
    // Overload for backward compatibility (2 args)
    public cartMainFrame(List<Map<String, String>> books, int numBooks) {
        this(books, numBooks, null);
    }

    // Rebuild the rows from the cartBooks list so items always fill earlier rows first
    private void rebuildGrid(List<Map<String, String>> cartBooks, JPanel container, int cols, TotalCartComponent totals) {
        container.removeAll();

        int num = (cartBooks == null) ? 0 : cartBooks.size();
        int numRows = Math.max(1, (int)Math.ceil(num / (double)cols));

        for (int row = 0; row < numRows; row++) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 40));
            for (int col = 0; col < cols; col++) {
                int index = row * cols + col;
                if (index < num) {
                    final Map<String, String> book = cartBooks.get(index);
                    String coverPath = book.getOrDefault("coverUrl", "");
                    // Use preloaded image if available; avoid synchronous network IO here
                    ImageIcon image = Models.ImageCache.get(coverPath);
                    if (image != null && image.getImage() != null) {
                        Image img = image.getImage();
                        Image scaled = img.getScaledInstance(120, 160, Image.SCALE_SMOOTH);
                        image = new ImageIcon(scaled);
                    } else {
                        // Not cached yet — use placeholder; ProductCard will attempt async loading if needed
                        image = createPlaceholderImage();
                    }

                    final java.util.Map<String, Object> holder = new java.util.HashMap<>();
                    ProductCard product = new ProductCard(
                        image,
                        book,
                        (b) -> {
                            try {
                                if (this.allBooks != null) saveBooksToJson(this.allBooks, this.jsonPath);
                            } catch (Exception ex) {
                                System.err.println("Failed to save books.json: " + ex.getMessage());
                            }
                        },
                        ProductCard.Context.CART,
                        () -> {
                            // On removal: update data, then rebuild the whole grid so items shift left
                            try {
                                if (book != null) book.put("added_to_cart", "false");
                                cartBooks.remove(book);
                                if (this.allBooks != null) saveBooksToJson(this.allBooks, this.jsonPath);
                            } catch (Exception ex) {
                                System.err.println("Error updating cart state: " + ex.getMessage());
                            }
                            // Rebuild the grid and refresh totals
                            rebuildGrid(cartBooks, container, cols, totals);
                            if (totals != null) totals.refresh(cartBooks, cartBooks.size());
                        }
                    );
                    holder.put("product", product);
                    rowPanel.add(product);
                }
            }
            container.add(rowPanel);
        }

        container.revalidate();
        container.repaint();
    }

    // Main constructor with 3 args for persistence
    public cartMainFrame(List<Map<String, String>> cartBooks, int numBooks, List<Map<String, String>> allBooks) {
        this.allBooks = allBooks;
        int cols = 3; // changed: 3 products per row in cart
        int numRows = Math.max(1, (int)Math.ceil(numBooks / (double)cols));

        // Create a container with GridLayout for rows (use 0 rows so layout adapts dynamically)
        JPanel container = new JPanel(new GridLayout(0, 1, 0, 0));

        // Create totals component early so removal callbacks can update it in real time
        TotalCartComponent totals = new TotalCartComponent(cartBooks, numBooks, this.allBooks);

        // Build initial grid from cartBooks
        rebuildGrid(cartBooks, container, cols, totals);

        setLayout(new BorderLayout());

        // Left-align the container with a left margin
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        centerWrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 10));
        centerWrapper.add(container);
        add(centerWrapper, BorderLayout.CENTER);

        // Add totals panel to the right of the books grid (use previously created instance)
        JPanel rightWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        rightWrapper.add(totals);
        add(rightWrapper, BorderLayout.EAST);
    }

    private ImageIcon createPlaceholderImage() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(200, 280, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillRect(0, 0, 200, 280);
        g2d.setColor(Color.BLACK);
        g2d.drawString("No Image", 75, 140);
        g2d.dispose();
        return new ImageIcon(img);
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
        Files.write(Paths.get(path), sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
