package edu.fiit.schneider_plugin.highlighters;

import com.intellij.openapi.editor.markup.RangeHighlighter;

import java.awt.*;

/**
 * Taken from https://github.com/xcegin/PUTVT
 */
class SideHighlighter {
    void highlight(RangeHighlighter lineHighlighter, final Color color) {
        lineHighlighter.setLineMarkerRenderer((editor, graphics, rectangle) -> {

            Color origColor = graphics.getColor();
            graphics.setColor(color);
            graphics.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
            graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
            graphics.setColor(origColor);

        });
    }
}
