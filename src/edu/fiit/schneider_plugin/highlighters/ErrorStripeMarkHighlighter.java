package edu.fiit.schneider_plugin.highlighters;

import com.intellij.openapi.editor.markup.RangeHighlighter;

import java.awt.*;
/**
 * Taken from https://github.com/xcegin/PUTVT
 */
class ErrorStripeMarkHighlighter {
    void highlight(RangeHighlighter lineHighlighter, Color color, String testName) {
        if (testName != null) {
            lineHighlighter.setErrorStripeMarkColor(color);
            lineHighlighter.setErrorStripeTooltip("The main.highlighter.errors on this line are:\n" + testName);
        }
    }

}
