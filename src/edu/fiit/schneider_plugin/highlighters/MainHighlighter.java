package edu.fiit.schneider_plugin.highlighters;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;

import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
/**
 * Taken from https://github.com/xcegin/PUTVT
 */
public class MainHighlighter {

    private Hashtable<String, List<RangeHighlighter>> highlights;
    private static LineHighlighter instance = null;
    private final static int HIGHLIGHTING_INDEX = 50;

    public static LineHighlighter getInstance() {
        if (instance == null) {
            instance = new LineHighlighter();
        }
        return instance;
    }

    private MainHighlighter() {
        highlights = new Hashtable<>();
    }

    public void highlightLines(final Color color, int fromLine, int toLine, String testName, Editor editor) {
        fromLine+=1;// indexovanie od 0
        Document document = editor.getDocument();
        SideHighlighter sideHighlighter = new SideHighlighter();

        LineHighlighter lineHighlighter = new LineHighlighter();
        ErrorStripeMarkHighlighter stripeHighlighter = new ErrorStripeMarkHighlighter();
        if (toLine <= document.getLineCount()) {
            TextAttributes attributes = new TextAttributes();

            RangeHighlighter highlighter = createRangeHighlighter(fromLine, toLine, attributes, editor);

            highlight(highlighter, attributes, color);

            stripeHighlighter.highlight(highlighter, attributes, color, testName);

            sideHighlighter.highlight(highlighter, color);
            if (highlights.get(editor.getMarkupModel().toString())==null){
                List<RangeHighlighter> rangeHighlighterList = new ArrayList<>();
                rangeHighlighterList.add(highlighter);
                highlights.put(editor.getMarkupModel().toString(), rangeHighlighterList);
            }
            else{
                List<RangeHighlighter> rangeHighlighterList = highlights.get(editor.getMarkupModel().toString());
                rangeHighlighterList.add(highlighter);
            }
        }
    }

    private static void highlight(@SuppressWarnings("unused") RangeHighlighter lineHighlighter, TextAttributes textAttributes, Color color) {
        textAttributes.setBackgroundColor(color);
    }

    public static void highlight(RangeHighlighter lineHighlighter,@SuppressWarnings("unused") TextAttributes textAttributes, Color color, String testName) {
        if (testName != null) {
            lineHighlighter.setErrorStripeMarkColor(color);
            lineHighlighter.setErrorStripeTooltip("The main.highlighter.errors on this line are:\n" + testName);
        }
    }
    private RangeHighlighter createRangeHighlighter(int fromLine, int toLine, TextAttributes attributes, Editor editor) {
        Document document = editor.getDocument();

        int lineStartOffset = document.getLineStartOffset(Math.max(0, fromLine - 1));
        int lineEndOffset = document.getLineEndOffset(Math.max(0, toLine - 1));

        return editor.getMarkupModel().addRangeHighlighter(
                lineStartOffset, lineEndOffset, 3333, attributes, HighlighterTargetArea.EXACT_RANGE
        );
    }

    public void clear(Editor editor) {
        MarkupModel model = editor.getMarkupModel();

        List<RangeHighlighter> rangeHighlighterList = highlights.get(model.toString());
        if (rangeHighlighterList != null && rangeHighlighterList.size() != 0) {
            for (RangeHighlighter rangeHighlighter : rangeHighlighterList) {
                model.removeHighlighter(rangeHighlighter);
            }
        }
        highlights.remove(model.toString());
        highlights.clear();
    }
}
