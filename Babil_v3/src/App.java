import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import UI.*;
import Models.*;

public class App {
    public static void main(String[] args) throws Exception {

        JFrame frame = new JFrame("Product card example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);

        // Load books from JSON file (data/books.json) first to extract types
        final java.util.List<java.util.Map<String, String>>[] listHolder = new java.util.List[]{ new ArrayList<>() };
        try {
            listHolder[0] = loadBooksFromJson("data/books.json");
        } catch (Exception e) {
            System.err.println("Failed to load data/books.json: " + e.getMessage());
            listHolder[0] = new ArrayList<>();
        }

        // Extract unique types from books
        java.util.Set<String> typesSet = new java.util.LinkedHashSet<>();
        for (Map<String, String> book : listHolder[0]) {
            String type = book.getOrDefault("type", "Other");
            if (!type.isEmpty()) typesSet.add(type);
        }
        String[] types = typesSet.toArray(new String[0]);

        // Use a holder to track the current scroll pane so callbacks can update it
        final java.util.Map<String, Object> scrollHolder = new java.util.HashMap<>();

        // Helper to create show details callback
        java.util.function.Consumer<Map<String, String>> createShowDetailsCallback = (book) -> {
            // Create ProductCardExpanded for the selected book
            ProductCardExpanded expandedCard = new ProductCardExpanded(book);
            JScrollPane expandedScroll = new JScrollPane(expandedCard);
            expandedScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            expandedScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            // Customize scrollbar
            JScrollBar vBar = expandedScroll.getVerticalScrollBar();
            vBar.setOpaque(false);
            vBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = new java.awt.Color(180, 180, 180, 150);
                    this.trackColor = new java.awt.Color(240, 240, 240, 100);
                }
                @Override
                protected javax.swing.JButton createDecreaseButton(int orientation) {
                    return createZeroButton();
                }
                @Override
                protected javax.swing.JButton createIncreaseButton(int orientation) {
                    return createZeroButton();
                }
                private javax.swing.JButton createZeroButton() {
                        javax.swing.JButton button = new javax.swing.JButton();
                        button.setPreferredSize(new java.awt.Dimension(0, 0));
                        button.setMinimumSize(new java.awt.Dimension(0, 0));
                        button.setMaximumSize(new java.awt.Dimension(0, 0));
                        return button;
                    }
                });
                expandedScroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220), 1));
                // Swap center panel
                JScrollPane oldScrollPane = (JScrollPane) scrollHolder.get("scrollPane");
                frame.getContentPane().remove(oldScrollPane);
                frame.add(expandedScroll, BorderLayout.CENTER);
                scrollHolder.put("scrollPane", expandedScroll);
                frame.revalidate();
                frame.repaint();
            };

            // Create SelectionHeader with type filtering
            SelectionHeader selectionHeader = new SelectionHeader(types, (selectedType) -> {
                // Filter books by type (null means show all)
                java.util.List<java.util.Map<String, String>> filteredBooks = new java.util.ArrayList<>();
                if (selectedType == null) {
                    filteredBooks.addAll(listHolder[0]);
                } else {
                    for (Map<String, String> b : listHolder[0]) {
                        if (selectedType.equalsIgnoreCase(b.getOrDefault("type", ""))) {
                            filteredBooks.add(b);
                        }
                    }
                }
                // Recreate MainFrame with filtered books
                MainFrame filteredGrid = new MainFrame(filteredBooks, (double) filteredBooks.size(), "data/books.json", createShowDetailsCallback);
                JScrollPane filteredScroll = new JScrollPane(filteredGrid);
                filteredScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                filteredScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                // Customize scrollbar
                JScrollBar vBar = filteredScroll.getVerticalScrollBar();
                vBar.setOpaque(false);
                vBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
                    @Override
                    protected void configureScrollBarColors() {
                        this.thumbColor = new java.awt.Color(180, 180, 180, 150);
                        this.trackColor = new java.awt.Color(240, 240, 240, 100);
                    }
                    @Override
                    protected javax.swing.JButton createDecreaseButton(int orientation) {
                        return createZeroButton();
                    }
                    @Override
                    protected javax.swing.JButton createIncreaseButton(int orientation) {
                        return createZeroButton();
                    }
                    private javax.swing.JButton createZeroButton() {
                        javax.swing.JButton button = new javax.swing.JButton();
                        button.setPreferredSize(new java.awt.Dimension(0, 0));
                        button.setMinimumSize(new java.awt.Dimension(0, 0));
                        button.setMaximumSize(new java.awt.Dimension(0, 0));
                        return button;
                    }
                });
                filteredScroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220), 1));
                // Swap center panel
                JScrollPane oldScrollPane = (JScrollPane) scrollHolder.get("scrollPane");
                frame.getContentPane().remove(oldScrollPane);
                frame.add(filteredScroll, BorderLayout.CENTER);
                scrollHolder.put("scrollPane", filteredScroll);
                frame.revalidate();
                frame.repaint();
            });

            // Prepare navigation callbacks: show main grid, show cart, show home
            Runnable showMain = () -> {
                MainFrame newMainGrid = new MainFrame(listHolder[0], (double) listHolder[0].size(), "data/books.json", createShowDetailsCallback);
                JScrollPane newScrollPane = new JScrollPane(newMainGrid);
                newScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                newScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                // Customize scrollbar appearance
                JScrollBar vBar = newScrollPane.getVerticalScrollBar();
                vBar.setOpaque(false);
                vBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
                    @Override
                    protected void configureScrollBarColors() {
                        this.thumbColor = new java.awt.Color(180, 180, 180, 150);
                        this.trackColor = new java.awt.Color(240, 240, 240, 100);
                    }
                    @Override
                    protected javax.swing.JButton createDecreaseButton(int orientation) {
                        return createZeroButton();
                    }
                    @Override
                    protected javax.swing.JButton createIncreaseButton(int orientation) {
                        return createZeroButton();
                    }
                    private javax.swing.JButton createZeroButton() {
                        javax.swing.JButton button = new javax.swing.JButton();
                        button.setPreferredSize(new java.awt.Dimension(0, 0));
                        button.setMinimumSize(new java.awt.Dimension(0, 0));
                        button.setMaximumSize(new java.awt.Dimension(0, 0));
                        return button;
                    }
                });
                newScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220), 1));
                // Swap center
                JScrollPane oldScrollPane = (JScrollPane) scrollHolder.get("scrollPane");
                if (oldScrollPane != null) frame.getContentPane().remove(oldScrollPane);
                frame.add(newScrollPane, BorderLayout.CENTER);
                scrollHolder.put("scrollPane", newScrollPane);
                frame.revalidate();
                frame.repaint();
            };

            Runnable showCart = () -> {
                java.util.List<java.util.Map<String, String>> cartBooks = new java.util.ArrayList<>();
                for (Map<String, String> b : listHolder[0]) {
                    if ("true".equalsIgnoreCase(b.getOrDefault("added_to_cart", "false"))) {
                        cartBooks.add(b);
                    }
                }
                cartMainFrame cartPanel = new cartMainFrame(cartBooks, cartBooks.size(), listHolder[0]);
                JScrollPane cartScroll = new JScrollPane(cartPanel);
                cartScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                cartScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                // Customize scrollbar appearance to match MainFrame
                JScrollBar cartVBar = cartScroll.getVerticalScrollBar();
                cartVBar.setOpaque(false);
                cartVBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
                    @Override
                    protected void configureScrollBarColors() {
                        this.thumbColor = new java.awt.Color(180, 180, 180, 150);
                        this.trackColor = new java.awt.Color(240, 240, 240, 100);
                    }
                    @Override
                    protected javax.swing.JButton createDecreaseButton(int orientation) {
                        return createZeroButton();
                    }
                    @Override
                    protected javax.swing.JButton createIncreaseButton(int orientation) {
                        return createZeroButton();
                    }
                    private javax.swing.JButton createZeroButton() {
                        javax.swing.JButton button = new javax.swing.JButton();
                        button.setPreferredSize(new java.awt.Dimension(0, 0));
                        button.setMinimumSize(new java.awt.Dimension(0, 0));
                        button.setMaximumSize(new java.awt.Dimension(0, 0));
                        return button;
                    }
                });
                cartScroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220), 1));
                JScrollPane oldScrollPane = (JScrollPane) scrollHolder.get("scrollPane");
                if (oldScrollPane != null) frame.getContentPane().remove(oldScrollPane);
                frame.add(cartScroll, BorderLayout.CENTER);
                scrollHolder.put("scrollPane", cartScroll);
                frame.revalidate();
                frame.repaint();
            };

            Runnable showHome = () -> {
                HomePage homePanel = new HomePage(showMain, showCart);
                // set a default background image (user can replace `data/home_bg.jpg` with their own image)
                try { homePanel.setBackgroundImagePath("https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=1200&q=80"); } catch (Exception ignored) {}
                JScrollPane homeScroll = new JScrollPane(homePanel);
                homeScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                homeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                JScrollPane oldScrollPane = (JScrollPane) scrollHolder.get("scrollPane");
                if (oldScrollPane != null) frame.getContentPane().remove(oldScrollPane);
                frame.add(homeScroll, BorderLayout.CENTER);
                scrollHolder.put("scrollPane", homeScroll);
                frame.revalidate();
                frame.repaint();
            };

            // Create header with cart callback and logo callback (logo now shows Home page)
            // Create header and wire search: header will call the provided search consumer
            java.util.function.Consumer<String> searchConsumer = (query) -> {
                String q = (query == null) ? "" : query.trim().toLowerCase();
                java.util.List<java.util.Map<String, String>> filtered = new java.util.ArrayList<>();
                if (q.isEmpty()) {
                    filtered.addAll(listHolder[0]);
                } else {
                    for (Map<String, String> b : listHolder[0]) {
                        String keywords = b.getOrDefault("keywords", "").toLowerCase();
                        String title = b.getOrDefault("title", "").toLowerCase();
                        String author = b.getOrDefault("author", "").toLowerCase();
                        String type = b.getOrDefault("type", "").toLowerCase();
                        if (keywords.contains(q) || title.contains(q) || author.contains(q) || type.contains(q)) {
                            filtered.add(b);
                        }
                    }
                }
                // Build and display filtered MainFrame
                MainFrame resultsGrid = new MainFrame(filtered, (double) filtered.size(), "data/books.json", createShowDetailsCallback);
                JScrollPane resultsScroll = new JScrollPane(resultsGrid);
                resultsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                resultsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                JScrollPane old = (JScrollPane) scrollHolder.get("scrollPane");
                if (old != null) frame.getContentPane().remove(old);
                frame.add(resultsScroll, BorderLayout.CENTER);
                scrollHolder.put("scrollPane", resultsScroll);
                frame.revalidate();
                frame.repaint();
            };

            Header header = new Header(
                () -> { showCart.run(); },
                () -> { showHome.run(); },
                searchConsumer
            );

            // Wire header into AppState so other components can refresh the cart badge
            Utils.AppState.headerRef = header;
            Utils.AppState.refreshCartCount();

            // Put header and selectionHeader into a top panel
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(header, BorderLayout.NORTH);
            topPanel.add(selectionHeader, BorderLayout.SOUTH);
            frame.add(topPanel, BorderLayout.NORTH);

            // Show Home page initially
            HomePage homePanel = new HomePage(showMain, showCart);
            // set a default online background image (replace with any URL you prefer)
            try { homePanel.setBackgroundImagePath("https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=1200&q=80"); } catch (Exception ignored) {}
            JScrollPane homeScroll = new JScrollPane(homePanel);
            homeScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            homeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollHolder.put("scrollPane", homeScroll);
            frame.add(homeScroll, BorderLayout.CENTER);
            frame.setVisible(true);
        }

        private static List<Map<String, String>> loadBooksFromJson(String path) throws IOException {
            String raw = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            List<Map<String, String>> result = new ArrayList<>();

            // Find object blocks
            Pattern objPattern = Pattern.compile("\\{[^}]*\\}", Pattern.DOTALL);
            Matcher m = objPattern.matcher(raw);
            while (m.find()) {
                String obj = m.group();
                Map<String, String> map = new HashMap<>();
                map.put("coverUrl", extractString(obj, "coverUrl"));
                map.put("title", extractString(obj, "title"));
                map.put("author", extractString(obj, "author"));
                map.put("price", extractString(obj, "price"));
                map.put("rating", extractString(obj, "rating"));
                map.put("language", extractString(obj, "language"));
                map.put("summary", extractString(obj, "summary"));
                map.put("pages", extractString(obj, "pages"));
                map.put("publisher", extractString(obj, "publisher"));
                // optional type field
                String type = extractString(obj, "type");
                if (type == null || type.isEmpty()) type = "Other";
                map.put("type", type);
                // optional added_to_cart flag
                String added = extractString(obj, "added_to_cart");
                if (added == null || added.isEmpty()) added = "false";
                map.put("added_to_cart", added);
                // optional stock field
                String stock = extractString(obj, "stock");
                if (stock == null || stock.isEmpty()) stock = "0";
                map.put("stock", stock);
                // optional keywords field for search
                String keywords = extractString(obj, "keywords");
                if (keywords == null) keywords = "";
                map.put("keywords", keywords);
                result.add(map);
            }
            return result;
        }

        private static String extractString(String obj, String key) {
            Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
            Matcher m = p.matcher(obj);
            if (m.find()) return m.group(1);
            return "";
        }
    }
