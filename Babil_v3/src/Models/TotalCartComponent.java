package Models;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.*;

public class TotalCartComponent extends JPanel {
    private List<Map<String, String>> cartBooks;
    private List<Map<String, String>> allBooks;
    private String booksJsonPath;
    private JLabel totalLabel;
    private JLabel totalTextLabel;
    private JPanel bookContainer;
    private JScrollPane scroll;
    private JComboBox<String> paymentSelector;
    private JLabel errorLabel;
    // map book identity to quantity spinner
    private java.util.Map<Map<String, String>, JSpinner> quantitySpinners = new java.util.IdentityHashMap<>();

    // Main constructor with 4 args for persistence
    public TotalCartComponent(List<Map<String, String>> cartBooks, int numBooks, List<Map<String, String>> allBooks, String booksJsonPath) {
        this.cartBooks = cartBooks;
        this.allBooks = allBooks;
        this.booksJsonPath = booksJsonPath;

        // Create a container with BorderLayout for the whole component
        JPanel totalContainer = new JPanel(new BorderLayout(8, 8));
        // Add padding around the totals panel so contents don't touch edges
        totalContainer.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Create a scrollable container for book items
        this.bookContainer = new JPanel();
        this.bookContainer.setLayout(new BoxLayout(this.bookContainer, BoxLayout.Y_AXIS));
        this.bookContainer.setBackground(new Color(250, 249, 246)); // Set background color for book container
        // Add light padding inside the book list
        this.bookContainer.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        Double totalPrice = 0.0;
        if (cartBooks != null) {
            for (Map<String, String> book : cartBooks) {
                String priceStr = book.getOrDefault("price", "0").replace(" DA", "").trim();
                try {
                    // Calculate total price (quantity later)
                    totalPrice += Double.parseDouble(priceStr);

                    // Create book item panel with three fixed columns using GridBagLayout
                    JPanel bookItem = new JPanel(new GridBagLayout());
                    bookItem.setOpaque(false);
                    bookItem.setMaximumSize(new Dimension(420, 40));

                    JLabel titleLabel = new JLabel(book.getOrDefault("title", ""));
                    titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
                    titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    titleLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

                    // Quantity spinner default 1, max based on stock
                    int stock = 0;
                    try { stock = Integer.parseInt(book.getOrDefault("stock", "0")); } catch (NumberFormatException ignored) {}
                    SpinnerNumberModel model = new SpinnerNumberModel(1, 1, Math.max(1, stock), 1);
                    JSpinner qtySpinner = new JSpinner(model);
                    qtySpinner.setPreferredSize(new Dimension(60, 24));
                    qtySpinner.setAlignmentY(Component.CENTER_ALIGNMENT);
                    quantitySpinners.put(book, qtySpinner);

                    JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
                    centerPanel.setOpaque(false);
                    centerPanel.add(new JLabel("Qty:"));
                    centerPanel.add(qtySpinner);
                    centerPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

                    JLabel priceLabel = new JLabel(book.getOrDefault("price", ""));
                    priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                    priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    priceLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridy = 0;
                    gbc.insets = new Insets(4, 6, 4, 6);

                    gbc.gridx = 0; // Index
                    gbc.weightx = 1.0; // Stretching enabled
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.anchor = GridBagConstraints.WEST;
                    bookItem.add(titleLabel, gbc);

                    gbc.gridx = 1;
                    gbc.weightx = 0.0;
                    gbc.fill = GridBagConstraints.NONE;
                    gbc.anchor = GridBagConstraints.CENTER;
                    bookItem.add(centerPanel, gbc);

                    gbc.gridx = 2;
                    gbc.weightx = 0.0;
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.anchor = GridBagConstraints.EAST;
                    bookItem.add(priceLabel, gbc);

                    this.bookContainer.add(bookItem);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid price format for book: " + book.getOrDefault("title", "Unknown") + " with price: " + priceStr);
                }
            }
        }

        setLayout(new BorderLayout());

        // Create scroll pane and decide viewport content based on whether cart has items
        this.scroll = new JScrollPane();
        // Increase preferred size so the list fits without showing a scrollbar in usual layouts
        // Increased height to give the totals panel more vertical space
        this.scroll.setPreferredSize(new Dimension(420, 620));
        Color bg = new Color(250, 249, 246); // Background color for consistency
        totalContainer.setBackground(bg);
        this.setBackground(bg);
        this.scroll.getViewport().setBackground(bg);
        this.scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder()); // Remove scroll border
        this.scroll.setOpaque(false); // Make scroll transparent

        if (this.cartBooks == null || this.cartBooks.isEmpty()) {
            this.scroll.setViewportView(createEmptyMessagePanel());
        } else {
            this.scroll.setViewportView(this.bookContainer);
        }

        totalContainer.add(this.scroll, BorderLayout.CENTER);

        totalTextLabel = new JLabel("Total:");
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalTextLabel.setForeground(new Color(80, 50, 30));

        // Price on the right
        totalLabel = new JLabel(String.format("%.2f", totalPrice) + " DA", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(new Color(139, 69, 19));

        // Controls panel (holds total label, email input, payment selector and buy button)
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlsPanel.setBackground(new Color(250, 249, 246));

        // Total label row: text on left, price on right
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.add(totalTextLabel, BorderLayout.WEST);
        totalRow.add(totalLabel, BorderLayout.EAST);
        controlsPanel.add(totalRow);

        // Full name input
        RoundedTextField nameField = new RoundedTextField(30, 15);
        nameField.setPreferredSize(new Dimension(220, 30));
        nameField.setText("Full name");
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        nameRow.setOpaque(false);
        nameRow.add(nameField);
        controlsPanel.add(nameRow);

        // Phone input (prefill +213)
        RoundedTextField phoneField = new RoundedTextField(15, 15);
        phoneField.setPreferredSize(new Dimension(220, 30));
        phoneField.setText("+213");
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JPanel phoneRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        phoneRow.setOpaque(false);
        phoneRow.add(phoneField);
        controlsPanel.add(phoneRow);

        // Email input (optional)
        RoundedTextField emailField = new RoundedTextField(20, 15);
        emailField.setPreferredSize(new Dimension(220, 30));
        emailField.setText("Enter your Email");
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JPanel emailRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        emailRow.setOpaque(false);
        emailRow.add(emailField);
        controlsPanel.add(emailRow);

        // Payment selector
        paymentSelector = new JComboBox<>(new String[]{"CCP", "CIB"});
        paymentSelector.setSelectedIndex(0); // CCP default
        paymentSelector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentSelector.setPreferredSize(new Dimension(120, 26));
        paymentSelector.setBackground(Color.WHITE);
        JPanel paymentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        paymentRow.setOpaque(false);
        JLabel payLbl = new JLabel("Payment:");
        payLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentRow.add(payLbl);
        paymentRow.add(paymentSelector);
        controlsPanel.add(paymentRow);

        // Error label for email validation
        errorLabel = new JLabel("", SwingConstants.LEFT);
        errorLabel.setForeground(new Color(180, 30, 30));
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setVisible(false);
        JPanel errorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        errorRow.setOpaque(false);
        errorRow.add(errorLabel);
        controlsPanel.add(errorRow);

        // Buy button
        RoundedButton buyButton = new RoundedButton("Buy now", 12);
        buyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        buyButton.setBackground(new Color(140, 113, 92));
        buyButton.setForeground(Color.WHITE);
        buyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buyButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        buyButton.setPreferredSize(new Dimension(120, 36));
        buyButton.addActionListener(e -> {
            String email = (emailField.getText() == null) ? "" : emailField.getText().trim();
            String name = (nameField.getText() == null) ? "" : nameField.getText().trim();
            String phone = (phoneField.getText() == null) ? "" : phoneField.getText().trim();

            // basic validations
            if (name.isEmpty()) {
                errorLabel.setText("Full name required");
                errorLabel.setVisible(true);
                return;
            }
            if (!phone.startsWith("+213") || phone.length() < 5) {
                errorLabel.setText("Phone must start with +213");
                errorLabel.setVisible(true);
                return;
            }
            boolean validEmail = Pattern.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", email);
            if (!validEmail) {
                errorLabel.setText("E-mail not valid");
                errorLabel.setVisible(true);
                return;
            }

            // Build purchase record and update stocks
            java.util.List<java.util.Map<String, Object>> purchasedItems = new ArrayList<>();
            if (this.cartBooks != null && this.allBooks != null) {
                for (Map<String, String> book : new ArrayList<>(this.cartBooks)) {
                    JSpinner spinner = quantitySpinners.get(book);
                    int qty = 1;
                    try { qty = (spinner == null) ? 1 : ((Number)spinner.getValue()).intValue(); } catch (Exception ignored) {}

                    int stock = 0;
                    try { stock = Integer.parseInt(book.getOrDefault("stock", "0")); } catch (NumberFormatException ignored) {}
                    int decrement = Math.min(stock, qty);

                    // Apply decrement to matching book in allBooks (match by title+author+coverUrl)
                    for (Map<String, String> b : this.allBooks) {
                        if (b.getOrDefault("title", "").equals(book.getOrDefault("title", ""))
                                && b.getOrDefault("author", "").equals(book.getOrDefault("author", ""))
                                && b.getOrDefault("coverUrl", "").equals(book.getOrDefault("coverUrl", ""))) {
                            int bstock = 0;
                            try { bstock = Integer.parseInt(b.getOrDefault("stock", "0")); } catch (NumberFormatException ignored) {}
                            int newStock = Math.max(0, bstock - qty);
                            b.put("stock", Integer.toString(newStock));
                            // Remove from cart if fully purchased or keep if stock remains
                            b.put("added_to_cart", "false");
                            break;
                        }
                    }

                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("title", book.getOrDefault("title", ""));
                    item.put("quantity", qty);
                    item.put("price", book.getOrDefault("price", ""));
                    purchasedItems.add(item);
                }

                // Save updated books.json
                try {
                    saveBooksToJson(this.allBooks, this.booksJsonPath);
                } catch (IOException io) {
                    System.err.println("Failed to save books.json: " + io.getMessage());
                }

                // Save purchase record to purchases.json (include total price)
                try {
                    // Calculate total price of this purchase
                    double purchaseTotal = 0.0;
                    for (Map<String, Object> it : purchasedItems) {
                        String p = (String) it.getOrDefault("price", "0");
                        String cleaned = p.replaceAll("[^0-9,\\.\\-]", "").replace(',', '.');
                        double pv = 0.0;
                        try { pv = Double.parseDouble(cleaned); } catch (Exception ignored) {}
                        int q = 1;
                        try { q = ((Number)it.getOrDefault("quantity", 1)).intValue(); } catch (Exception ignored) {}
                        purchaseTotal += pv * q;
                    }
                    savePurchaseRecord(name, phone, email, paymentSelector.getSelectedItem().toString(), purchasedItems, purchaseTotal);
                } catch (IOException io) {
                    System.err.println("Failed to save purchases.json: " + io.getMessage());
                }

                // Clear cartBooks and refresh UI
                this.cartBooks.clear();
                this.quantitySpinners.clear();
                refresh(this.cartBooks, 0);
                // Update header badge count after purchase
                try { Utils.AppState.refreshCartCount(); } catch (Exception ignored) {}
                JOptionPane.showMessageDialog(this, "Purchase successful (" + paymentSelector.getSelectedItem() + ")", "Purchase", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        btnRow.setOpaque(false);
        btnRow.add(buyButton);
        controlsPanel.add(btnRow);

        totalContainer.add(controlsPanel, BorderLayout.SOUTH);

        this.add(totalContainer, BorderLayout.CENTER);
        // Add larger external margin so the totals panel aligns with the books grid (top + left)
        // Reduced left padding to make the panel appear closer to the content while retaining overall size
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 20));
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(440, 580);
    }

    // Create a centered panel that displays the empty-cart message
    private JPanel createEmptyMessagePanel() {
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setOpaque(false);
        JLabel msg = new JLabel("No item added to cart yet.");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(new Color(100, 100, 100));
        msg.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1.0; g.weighty = 1.0; g.anchor = GridBagConstraints.CENTER;
        emptyPanel.add(msg, g);
        return emptyPanel;
    }

    // Recompute totals and refresh UI
    public void refresh(List<Map<String, String>> newCartBooks, int newNumBooks) {
        this.cartBooks = newCartBooks;
        double totalPrice = 0.0;
        // Rebuild list and recompute total
        this.bookContainer.removeAll();
        this.quantitySpinners.clear();
        if (this.cartBooks != null && !this.cartBooks.isEmpty()) {
            for (Map<String, String> b : this.cartBooks) {
                String priceStr = b.getOrDefault("price", "0").replace("$", "").trim();
                try {
                    totalPrice += Double.parseDouble(priceStr);
                } catch (NumberFormatException ignored) {}

                JPanel bookItem = new JPanel(new GridBagLayout());
                bookItem.setOpaque(false);
                bookItem.setMaximumSize(new Dimension(420, 40));

                JLabel titleLabel = new JLabel(b.getOrDefault("title", ""));
                titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
                titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                titleLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

                int stock = 0;
                try { stock = Integer.parseInt(b.getOrDefault("stock", "0")); } catch (NumberFormatException ignored) {}
                SpinnerNumberModel model = new SpinnerNumberModel(1, 1, Math.max(1, stock), 1);
                JSpinner qtySpinner = new JSpinner(model);
                qtySpinner.setPreferredSize(new Dimension(60, 24));
                qtySpinner.setAlignmentY(Component.CENTER_ALIGNMENT);
                this.quantitySpinners.put(b, qtySpinner);

                JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
                centerPanel.setOpaque(false);
                centerPanel.add(new JLabel("Qty:"));
                centerPanel.add(qtySpinner);
                centerPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

                JLabel priceLabel = new JLabel(b.getOrDefault("price", ""));
                priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                priceLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridy = 0;
                gbc.insets = new Insets(4, 6, 4, 6);

                gbc.gridx = 0;
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.WEST;
                bookItem.add(titleLabel, gbc);

                gbc.gridx = 1;
                gbc.weightx = 0.0;
                gbc.fill = GridBagConstraints.NONE;
                gbc.anchor = GridBagConstraints.CENTER;
                bookItem.add(centerPanel, gbc);

                gbc.gridx = 2;
                gbc.weightx = 0.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.EAST;
                bookItem.add(priceLabel, gbc);

                this.bookContainer.add(bookItem);
            }
        } else {
            // show the empty message panel when no items
            if (this.scroll != null) {
                this.scroll.setViewportView(createEmptyMessagePanel());
            }
        }

        // if we have items, ensure the viewport shows the book container
        if (this.cartBooks != null && !this.cartBooks.isEmpty() && this.scroll != null) {
            this.scroll.setViewportView(this.bookContainer);
        }

        if (this.totalLabel != null) {
                this.totalLabel.setText(String.format("%.2f", totalPrice) + " DA"); // Update total label format
        }
        this.bookContainer.revalidate();
        this.bookContainer.repaint();
        this.bookContainer.setBackground(new Color(250, 249, 246));
        revalidate();
        repaint();
    }

    private void saveBooksToJson(List<Map<String, String>> books, String path) throws IOException {
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

    private void savePurchaseRecord(String fullName, String phone, String email, String paymentMethod, List<Map<String, Object>> items, double totalPrice) throws IOException {
        String purchasesPath = "data/purchases.json";
        StringBuilder obj = new StringBuilder();
        obj.append("  {\n");
        obj.append("    \"created_at\": \"").append(Instant.now().toString()).append("\",\n");
        obj.append("    \"name\": \"").append(escapeJson(fullName)).append("\",\n");
        obj.append("    \"phone\": \"").append(escapeJson(phone)).append("\",\n");
        obj.append("    \"email\": \"").append(escapeJson(email)).append("\",\n");
        obj.append("    \"payment\": \"").append(escapeJson(paymentMethod)).append("\",\n");
        // include total price as a formatted string with DA
        obj.append("    \"total_price\": \"").append(String.format("%.2f DA", totalPrice)).append("\",\n");
        obj.append("    \"items\": [\n");
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> it = items.get(i);
            obj.append("      {\"title\": \"").append(escapeJson((String)it.getOrDefault("title", ""))).append("\", \"quantity\": ").append(it.getOrDefault("quantity", 1)).append(", \"price\": \"").append(escapeJson((String)it.getOrDefault("price", ""))).append("\"}");
            if (i < items.size() - 1) obj.append(",");
            obj.append("\n");
        }
        obj.append("    ]\n");
        obj.append("  }");

        java.nio.file.Path p = Paths.get(purchasesPath);
        if (!Files.exists(p)) {
            // create new array with single object
            String content = "[\n" + obj.toString() + "\n]\n";
            Files.createDirectories(p.getParent());
            Files.write(p, content.getBytes(StandardCharsets.UTF_8));
            return;
        }
        String existing = new String(Files.readAllBytes(p), StandardCharsets.UTF_8).trim();
        if (existing.isEmpty() || existing.equals("[]")) {
            String content = "[\n" + obj.toString() + "\n]\n";
            Files.write(p, content.getBytes(StandardCharsets.UTF_8));
            return;
        }
        // Insert before last ]
        int lastIdx = existing.lastIndexOf(']');
        String prefix = existing.substring(0, lastIdx).trim();
        // If prefix ends with { ... } then append comma
        String newContent = prefix;
        if (!prefix.endsWith("[")) newContent += ",\n";
        newContent += obj.toString() + "\n]";
        Files.write(p, newContent.getBytes(StandardCharsets.UTF_8));
    }
}
