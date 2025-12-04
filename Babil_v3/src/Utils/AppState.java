package Utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

/**
 * Small application state helper to allow UI components to refresh the header badge.
 */
public class AppState {
    // A reference to the header component (optional)
    public static UI.Header headerRef = null;

    // Read data/books.json and count entries with added_to_cart == "true"
    public static void refreshCartCount() {
        int count = 0;
        try {
            String raw = new String(Files.readAllBytes(Paths.get("data/books.json")), StandardCharsets.UTF_8);
            Pattern objPattern = Pattern.compile("\\{[^}]*\\}", Pattern.DOTALL);
            Matcher m = objPattern.matcher(raw);
            while (m.find()) {
                String obj = m.group();
                Pattern p = Pattern.compile("\"added_to_cart\"\s*:\s*\"([^\"]*)\"");
                Matcher mm = p.matcher(obj);
                if (mm.find()) {
                    String val = mm.group(1);
                    if ("true".equalsIgnoreCase(val)) count++;
                }
            }
        } catch (Exception e) {
            // ignore — file may not exist yet
        }

        final int finalCount = count;
        if (headerRef != null) {
            SwingUtilities.invokeLater(() -> headerRef.setCartCount(finalCount));
        }
    }
}
