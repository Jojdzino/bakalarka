package edu.fiit.schneider_plugin.highlighters;

import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import edu.fiit.schneider_plugin.entity.WarningType;

import java.awt.*;


@SuppressWarnings({"UseJBColor", "WeakerAccess"})
class ElementTextAtributesCreator {

    static final Color INFO_BACKGROUND = new Color(147, 206, 154);
    static final Color INFO_EFFECT = new Color(5, 122, 33);

    static final Color WARNING_BACKGROUND = new Color(206, 182, 100);
    static final Color WARNING_EFFECT = new Color(206, 166, 0);

    static final Color ERROR_BACKGROUND = new Color(255, 158, 151);
    static final Color ERROR_EFFECT = new Color(255, 0, 0);

    static final Color FOREGROUND = new Color(1, 0, 0);

    static TextAttributes createContrastTextAttributes(WarningType warningType) {
        Color foreground, background, effect;
        int font = TextAttributes.USE_INHERITED_MARKER.getFontType();//basic font
        EffectType type = EffectType.BOXED;
        switch (warningType) {
            case INFO:
                background = INFO_BACKGROUND;
                foreground = FOREGROUND;
                effect = INFO_EFFECT;
                return new TextAttributes(foreground, background, effect, type, font);
            case WARNING:
                background = WARNING_BACKGROUND;
                foreground = FOREGROUND;
                effect = WARNING_EFFECT;
                return new TextAttributes(foreground, background, effect, type, font);
            case ERROR:
                background = ERROR_BACKGROUND;
                foreground = FOREGROUND;
                effect = ERROR_EFFECT;
                return new TextAttributes(foreground, background, effect, type, font);
        }
        return null;
    }
}
