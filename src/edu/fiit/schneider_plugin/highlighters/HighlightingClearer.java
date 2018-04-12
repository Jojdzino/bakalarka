package edu.fiit.schneider_plugin.highlighters;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;

import java.util.Map;

public class HighlightingClearer {
    /**
     * Clears given editor of all highlighting.
     *
     * @param editor editor to clear of highglighting
     */
    public static void clear(Editor editor) {
        MarkupModel model = editor.getMarkupModel();
        model.removeAllHighlighters();
        //MainHighlighter.getInstance().getHighlights().remove(model.toString());
        if (MainHighlighter.getInstance().getHighlighters().containsKey(model.toString()))
            MainHighlighter.getInstance().getHighlighters().remove(model.toString()).clear();
    }

    /**
     * Clears highlighting in selected editor based on given lines. Removes highlighting between those lines, including them.
     *
     * @param editor   editor to search for highlighting
     * @param fromLine first line of highlighting targets, indexed as in IDEA, included
     * @param toLine   last line of highlighting targets, indexed as in IDEA, included
     */
    public static void clearSpecificHighlight(Editor editor, int fromLine, int toLine) {
        MarkupModel model = editor.getMarkupModel();
        Map<String, Map<String, RangeHighlighter>> highlighters =
                MainHighlighter.getInstance().getHighlighters();
        //Hashtable<String, List<RangeHighlighter>> highlights = MainHighlighter.getInstance().getHighlights();

        String specificKey = String.valueOf(fromLine) + " " + String.valueOf(toLine);
        RangeHighlighter targetOfRemoval;
        if (highlighters.containsKey(model.toString()))
            targetOfRemoval = highlighters.get(model.toString()).get(specificKey);
        else return;

        model.removeHighlighter(targetOfRemoval);
        highlighters.get(model.toString()).remove(specificKey);//removing from map by key
    }
}
