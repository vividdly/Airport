// ThemeManager.java
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    // FIX: volatile ensures the write to `instance` is visible across threads.
    private static volatile ThemeManager instance;
    private final List<ThemeListener> listeners = new ArrayList<>();

    public interface ThemeListener {
        void onThemeChanged();
    }

    private ThemeManager() {}

    // FIX: double-checked locking for a thread-safe singleton.
    public static ThemeManager getInstance() {
        if (instance == null) {
            synchronized (ThemeManager.class) {
                if (instance == null) instance = new ThemeManager();
            }
        }
        return instance;
    }

    public Color getBackground() {
        return new Color(250, 252, 255);
    }

    public Color getPanelBackground() {
        return Color.WHITE;
    }

    public Color getAccentColor() {
        return new Color(0, 119, 200);
    }

    public Color getTextColor() {
        return new Color(10, 10, 10);
    }

    public void addListener(ThemeListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners() {
        for (ThemeListener listener : listeners) {
            listener.onThemeChanged();
        }
    }

    public void removeListener(ThemeListener listener) {
        listeners.remove(listener);
    }


}