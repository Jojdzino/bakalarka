package edu.fiit.schneider_plugin.highlighters;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;

@SuppressWarnings("Duplicates")
public class HighlightingClearer {
    /**
     * Clears given editor of all highlighting.
     *
     * @param editor editor to clear of highglighting
     */
    public static void clear(Editor editor) {
        MarkupModel model = editor.getMarkupModel();
        model.removeAllHighlighters();
    }

    public static void clearSpecificHighlight(Editor editor, int highlightedIndex) {
        MarkupModel model = editor.getMarkupModel();
        RangeHighlighter[] highlighters = model.getAllHighlighters();
        RangeHighlighter specificHighlighter = getHighlighter(highlighters, highlightedIndex);
        if (specificHighlighter != null)
            model.removeHighlighter(specificHighlighter);
    }

    /**
     * Returns highlighter at specified index of selected editor.
     *
     * @param highlighters     highlighters frmo editor
     * @param highlightedIndex index that is highlighted
     * @return highlighter that highlights specified index
     */
    private static RangeHighlighter getHighlighter(RangeHighlighter[] highlighters, int highlightedIndex) {
        int from, to;
        for (RangeHighlighter highlighter : highlighters) {
            from = highlighter.getStartOffset();
            to = highlighter.getStartOffset();
            if (from >= highlightedIndex && highlightedIndex <= to)
                return highlighter;
        }
        return null;
    }
}
