package Models;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageCache {
    private static final ConcurrentHashMap<String, ImageIcon> cache = new ConcurrentHashMap<>();

    // Preload images for a list of books (non-blocking but will attempt to load each image)
    public static void preload(List<Map<String, String>> books) {
        if (books == null) return;
        for (Map<String, String> b : books) {
            String url = b.get("coverUrl");
            if (url == null || url.isEmpty()) continue;
            // Kick off loading in a background thread for each image to avoid blocking startup too long
            if (!cache.containsKey(url)) {
                new Thread(() -> {
                    try {
                        BufferedImage img = null;
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            img = ImageIO.read(new URL(url));
                        } else {
                            img = ImageIO.read(new URL("file:" + url));
                        }
                        if (img != null) cache.put(url, new ImageIcon(img));
                        else cache.put(url, null);
                    } catch (Exception e) {
                        // store a null marker to avoid retrying endlessly
                        cache.put(url, null);
                    }
                }, "ImagePreload-" + Math.abs(url.hashCode())).start();
            }
        }
    }

    // Get cached image (may be null if failed or not yet loaded)
    public static ImageIcon get(String url) {
        if (url == null) return null;
        return cache.get(url);
    }
}
