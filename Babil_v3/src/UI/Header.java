package UI;
import java.awt.*;
import javax.swing.*;
import Models.RoundedButton;
import Models.RoundedTextField;

public class Header extends JPanel {
    private JLabel cartCountLabel;
    public Header(Runnable onCartClicked) {
        this(onCartClicked, null, null);
    }

    public Header(Runnable onCartClicked, Runnable onLogoClicked) {
        this(onCartClicked, onLogoClicked, null);
    }

    public Header(Runnable onCartClicked, Runnable onLogoClicked, java.util.function.Consumer<String> onSearch) {
        System.out.println("Hello, your Header is created!");

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Logo (try web image first, fallback to local file)
        ImageIcon logo = null;
        try {
            java.net.URL logoUrl = new java.net.URL("file:///C:/Users/Lenovo/Downloads/B%C4%81bil.png");
            logo = new ImageIcon(logoUrl);
            Image li = logo.getImage();
            if (li != null) {
                Image scaled = li.getScaledInstance(48, 20, Image.SCALE_SMOOTH);
                logo = new ImageIcon(scaled);
            }
        } catch (Exception e) {
            // Fallback to bundled/local logo
            logo = new ImageIcon("Logo.svg");
        }
        JLabel logoLabel = new JLabel(logo);
        
        // Wire logo click to callback
        if (onLogoClicked != null) {
            logoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    onLogoClicked.run();
                }
            });
        }

        // Search bar: use FlowLayout centered so the search controls sit in the header center
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

            // Small logo placed to the left of the search field
            ImageIcon smallLogoIcon = new ImageIcon("Logo.svg");
            Image smallImg = smallLogoIcon.getImage();
            if (smallImg != null) {
                Image scaled = smallImg.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                smallLogoIcon = new ImageIcon(scaled);
            }
            JLabel logoSearchLabel = new JLabel(smallLogoIcon);
            logoSearchLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

            // Search field (rounded)
            RoundedTextField searchField = new RoundedTextField(20, 15);
            searchField.setPreferredSize(new Dimension(180, 35));
            searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            searchField.setText("Search products...");
            searchField.setBackground(Color.WHITE);

            // Search button (rounded brown)
            RoundedButton searchButton = new RoundedButton("Search", 12);
            searchButton.setBackground(new Color(140, 113, 92)); // Brown color 8C715C
            searchButton.setForeground(Color.WHITE);
            searchButton.setFocusPainted(false);
            searchButton.setPreferredSize(new Dimension(90, 35));
            searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // When user clicks the 'Search' button or presses Enter
            searchButton.addActionListener(e -> {
                String query = searchField.getText();
                if (onSearch != null) {
                    try {
                        onSearch.accept(query == null ? "" : query.trim());
                    } catch (Exception ex) {
                        System.err.println("Error running search callback: " + ex.getMessage());
                    }
                } else {
                    System.out.println("Searching for: " + query);
                }
            });
            searchField.addActionListener(e -> searchButton.doClick());

        // Add components to panel (FlowLayout will respect preferred sizes)
        searchPanel.add(logoSearchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Cart button
        ImageIcon cartIcon = null;
        try {
            cartIcon = new ImageIcon(new java.net.URL("file:///C:/Users/Lenovo/Downloads/Cart.png"));
            // Scale icon to 40x40
            Image cartImg = cartIcon.getImage();
            if (cartImg != null) {
                Image scaled = cartImg.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                cartIcon = new ImageIcon(scaled);
            }
        } catch (Exception e) {
            System.err.println("Failed to load cart icon: " + e.getMessage());
            cartIcon = new ImageIcon(); // Empty icon as fallback
        }
        
        JButton cartButton = new JButton(cartIcon);
        cartButton.setOpaque(false);
        cartButton.setContentAreaFilled(false);
        cartButton.setBorderPainted(false);
        cartButton.setFocusPainted(false);
        cartButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Create a small badge label to display number of items in cart
        cartCountLabel = new JLabel("0", SwingConstants.CENTER);
        cartCountLabel.setOpaque(true);
        cartCountLabel.setBackground(new Color(139, 69, 19));
        cartCountLabel.setForeground(Color.WHITE);
        cartCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cartCountLabel.setPreferredSize(new Dimension(26, 20));
        cartCountLabel.setMaximumSize(new Dimension(26, 20));
        cartCountLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        cartCountLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Wrap cart button and badge in a centered panel using BoxLayout
        JPanel cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.X_AXIS));
        cartPanel.add(Box.createHorizontalGlue()); // Push to center
        cartPanel.add(cartButton);
        cartPanel.add(Box.createRigidArea(new Dimension(6, 0)));
        cartPanel.add(cartCountLabel);
        cartPanel.add(Box.createHorizontalGlue()); // Balance on right

        // Wire the cart click to the provided callback
        if (onCartClicked != null) {
            cartButton.addActionListener(e -> {
                try {
                    onCartClicked.run();
                } catch (Exception ex) {
                    System.err.println("Error running cart callback: " + ex.getMessage());
                }
            });
        }

        

        add(logoLabel, BorderLayout.WEST);
        add(searchPanel, BorderLayout.CENTER);
        add(cartPanel, BorderLayout.EAST);
    }

    // Update the count shown in the small cart badge
    public void setCartCount(int count) {
        if (cartCountLabel != null) {
            cartCountLabel.setText(Integer.toString(count));
        }
    }
}
    