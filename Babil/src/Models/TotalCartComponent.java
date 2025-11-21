package Models;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
 
import javax.swing.*;

public class TotalCartComponent extends JPanel {
    private List<Map<String, String>> cartBooks;
    private JLabel totalLabel;
    private JLabel totalTextLabel;
    private JPanel bookContainer;
    private JComboBox<String> paymentSelector;
    private JLabel errorLabel;

    // Main constructor with 3 args for persistence
    public TotalCartComponent(List<Map<String, String>> cartBooks, int numBooks, List<Map<String, String>> allBooks) {
        this.cartBooks = cartBooks;

        // Create a container with BorderLayout for the whole component
        JPanel totalContainer = new JPanel(new BorderLayout(8, 8));

        // Create a scrollable container for book items
        this.bookContainer = new JPanel();
        this.bookContainer.setLayout(new BoxLayout(this.bookContainer, BoxLayout.Y_AXIS));
            this.bookContainer.setBackground(new Color(250, 249, 246)); // Set background color for book container

        Double totalPrice = 0.0;
        if (cartBooks != null) {
            for (Map<String, String> book : cartBooks) {
                String priceStr = book.getOrDefault("price", "0").replace("$", "").trim();
                try {
                    // Calculate total price
                    totalPrice += Double.parseDouble(priceStr);

                    // Create book item panel with left title and right price
                    JPanel bookItem = new JPanel(new BorderLayout());
                    JLabel titleLabel = new JLabel(book.getOrDefault("title", ""));
                    titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
                    titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    JLabel priceLabel = new JLabel(book.getOrDefault("price", ""));
                    priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                    priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    bookItem.add(titleLabel, BorderLayout.WEST);
                    bookItem.add(priceLabel, BorderLayout.EAST);
                        bookItem.setMaximumSize(new Dimension(240, 26)); // Set maximum size for book item
                        bookItem.setOpaque(false); // Make book item transparent
                    this.bookContainer.add(bookItem);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid price format for book: " + book.getOrDefault("title", "Unknown") + " with price: " + priceStr);
                }
            }
        }

        setLayout(new BorderLayout());

        JScrollPane scroll = new JScrollPane(this.bookContainer);
        scroll.setPreferredSize(new Dimension(240, 260));
            Color bg = new Color(250, 249, 246); // Background color for consistency
            totalContainer.setBackground(bg);
            this.setBackground(bg);
            scroll.getViewport().setBackground(bg);
            scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder()); // Remove scroll border
            scroll.setOpaque(false); // Make scroll transparent
        totalContainer.add(scroll, BorderLayout.CENTER);

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

        // Email input
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
            // simple email validation
            boolean valid = Pattern.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", email);
            if (!valid) {
                errorLabel.setText("E-mail not valid");
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
                // For now show a confirmation dialog; integration with payment flow can be added later
                JOptionPane.showMessageDialog(this, "Purchase successful (" + paymentSelector.getSelectedItem() + ")", "Purchase", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        btnRow.setOpaque(false);
        btnRow.add(buyButton);
        controlsPanel.add(btnRow);

        totalContainer.add(controlsPanel, BorderLayout.SOUTH);

        this.add(totalContainer, BorderLayout.CENTER);
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250, 360);
    }

    // Recompute totals and refresh UI
    public void refresh(List<Map<String, String>> newCartBooks, int newNumBooks) {
        this.cartBooks = newCartBooks;
        double totalPrice = 0.0;
        // Rebuild list and recompute total
        this.bookContainer.removeAll();
        if (this.cartBooks != null) {
            for (Map<String, String> b : this.cartBooks) {
                String priceStr = b.getOrDefault("price", "0").replace("$", "").trim();
                try {
                    totalPrice += Double.parseDouble(priceStr);
                } catch (NumberFormatException ignored) {}

                // Create book item row with title at left and price at right
                JPanel bookItem = new JPanel(new BorderLayout());
                JLabel titleLabel = new JLabel(b.getOrDefault("title", ""));
                titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
                titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                JLabel priceLabel = new JLabel(b.getOrDefault("price", ""));
                priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                bookItem.add(titleLabel, BorderLayout.WEST);
                bookItem.add(priceLabel, BorderLayout.EAST);
                    bookItem.setMaximumSize(new Dimension(240, 26)); // Set maximum size for book item
                    bookItem.setOpaque(false); // Make book item transparent
                this.bookContainer.add(bookItem);
            }
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
}
