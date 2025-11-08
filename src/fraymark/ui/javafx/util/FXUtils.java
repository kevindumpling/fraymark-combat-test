package fraymark.ui.javafx.util;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/***
 * Utility class for JavaFX concerns, such as fonts, etc.
 */
public class FXUtils {
    public static Text styledText(String content, Color color) {
        Text t = new Text(content);
        t.setFill(color);
        return t;
    }
}
