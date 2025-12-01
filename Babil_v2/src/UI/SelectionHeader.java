package UI;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.*;

public class SelectionHeader extends JPanel {
    // Overload: backward-compatible constructor for when no filtering is needed
    public SelectionHeader() {
        this(new String[] { "Science", "Technology", "AI" }, null);
    }

    // Main constructor with type filtering
    public SelectionHeader(String[] types, Consumer<String> onTypeSelected) {
        setLayout(new GridLayout(1, Math.max(types.length + 1, 8)));
        setPreferredSize(new Dimension(0, 50));

        // Create "All" button to show all books
        JButton allBooks = new JButton("▾ All Books");
        setupButton(allBooks, 0, new String[types.length + 1].length, () -> {
            if (onTypeSelected != null) onTypeSelected.accept(null); // null means show all
        });
        add(allBooks);

        // Create a button for each type
        for (int i = 0; i < types.length; i++) {
            final String type = types[i];
            JButton typeButton = new JButton("▾ " + type);
            setupButton(typeButton, i + 1, types.length + 1, () -> {
                if (onTypeSelected != null) onTypeSelected.accept(type);
            });
            add(typeButton);
        }
    }

    private void setupButton(JButton button, int index, int totalButtons, Runnable onClicked) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        Color separator = new Color(220, 220, 220); // light vertical border
        if (index > 0) {
            button.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, separator));
        } else {
            button.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        }

        // Wire the button click to the callback
        if (onClicked != null) {
            button.addActionListener(e -> onClicked.run());
        }
    }
}

