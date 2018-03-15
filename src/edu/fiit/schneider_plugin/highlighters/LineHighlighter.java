package edu.fiit.schneider_plugin.highlighters;

import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;

import java.awt.*;
/**
 * Taken from https://github.com/xcegin/PUTVT
 */
public class LineHighlighter {
    public void highlight(RangeHighlighter lineHighlighter, TextAttributes textAttributes, Color color) {
        textAttributes.setBackgroundColor(color);
    }
}
