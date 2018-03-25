package edu.fiit.schneider_plugin.highlighters;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.javadoc.PsiDocCommentImpl;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import edu.fiit.schneider_plugin.intelij.util.EditorUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
/**
 * Highly inspired from https://github.com/xcegin/PUTVT
 */
@SuppressWarnings({"UseJBColor", "WeakerAccess"})
public class MainHighlighter {

    private Hashtable<String, List<RangeHighlighter>> highlights;
    private Hashtable<String, Hashtable<String, RangeHighlighter>> highlighters;
    private static MainHighlighter instance = null;
    private final static Color COMMENT_HIGHLIGHT_COLOR = new Color(191, 191, 191, 255);
    private final static Color TARGET_HIGHLIGHT_COLOR = new Color(83, 143, 166, 132);
//    private final static Color COMMENT_TEXT_COLOR = new Color(254, 255, 34);
//    private final static TextAttributes COMMENT_ATTRIBUTES =
//            new TextAttributes()


    public static MainHighlighter getInstance() {
        if (instance == null) {
            instance = new MainHighlighter();
        }
        return instance;
    }

    private MainHighlighter() {
        highlights = new Hashtable<>();
        highlighters = new Hashtable<>();
    }

    private static void highlight(TextAttributes textAttributes, Color color) {
        textAttributes.setBackgroundColor(color);
    }

    public static void highlightErrorStripe(RangeHighlighter lineHighlighter, Color color, String testName) {
        if (testName != null) {
            lineHighlighter.setErrorStripeMarkColor(color);
            lineHighlighter.setErrorStripeTooltip("Problem on this line is:\n" + testName);
        }
    }

    public static RangeHighlighter createRangeHighlighter(int fromLine, int toLine, TextAttributes attributes, Editor editor) {
        Document document = editor.getDocument();

        int lineStartOffset = document.getLineStartOffset(Math.max(0, fromLine));
        int lineEndOffset = document.getLineEndOffset(Math.max(0, toLine));

        return editor.getMarkupModel().addRangeHighlighter(
                lineStartOffset, lineEndOffset, 3333, attributes, HighlighterTargetArea.EXACT_RANGE
        );
    }

    /**
     * Highlights all elements from given list. Comments are highlighted with greenish color, and targets with blueish.
     * @param psiElements elements to be highlighted
     * @param problem text representation of problem
     */
    public void highlight(List<? extends PsiElement> psiElements, String problem) {
        int fromLine, toLine;
        Class<? extends PsiElement> decisionClass = psiElements.get(0).getClass();
        RangeHighlighter rangeHighlighter;

        if (psiElements.size() == 1) {
            if (psiElements.get(0).getChildren().length != 0) {
                fromLine= EditorUtil.getLineOfElement(psiElements.get(0).getFirstChild());
                toLine  = EditorUtil.getLineOfElement(getLastChild(psiElements.get(0)));//comment
            }
            else{
                fromLine=EditorUtil.getLineOfElement(psiElements.get(0));
                toLine  = EditorUtil.getLineOfElementWithOffset(psiElements.get(0));
            }
        }
        else{
            //If first element has children
            if(psiElements.get(0).getChildren().length!=0)
                 fromLine = EditorUtil.getLineOfElement(psiElements.get(0).getFirstChild());
            else fromLine = EditorUtil.getLineOfElement(psiElements.get(0));
            //If last element has children
            PsiElement element = psiElements.get(psiElements.size()-1);
            if(element.getChildren().length!=0)
                toLine = EditorUtil.getLineOfElement(getLastChild(element));
            else toLine = EditorUtil.getLineOfElement(element);
        }
        Editor editor =FileEditorManager.getInstance(psiElements.get(0).getProject()).getSelectedTextEditor();
        rangeHighlighter = createRangeHighlighter(fromLine, toLine, new TextAttributes(),editor);

        //TODO change testname to normal error string like cohorence is too big etc... DONE
        if (decisionClass == PsiCommentImpl.class || decisionClass == PsiDocCommentImpl.class) {//psidoccommentbase, nieco ako istypeof skusit
            highlightErrorStripe(rangeHighlighter, COMMENT_HIGHLIGHT_COLOR, problem);
            this.highlightLines(COMMENT_HIGHLIGHT_COLOR, fromLine, toLine, problem, editor);
        } else {
            highlightErrorStripe(rangeHighlighter, TARGET_HIGHLIGHT_COLOR, problem);
            this.highlightLines(TARGET_HIGHLIGHT_COLOR, fromLine, toLine, problem, editor);
        }
    }

    public void highlightLines(final Color color, int fromLine, int toLine, String testName, Editor editor) {
        Document document = editor.getDocument();
        SideHighlighter sideHighlighter = new SideHighlighter();

        ErrorStripeMarkHighlighter stripeHighlighter = new ErrorStripeMarkHighlighter();
        if (toLine <= document.getLineCount()) {
            TextAttributes attributes = new TextAttributes();

            RangeHighlighter highlighter = createRangeHighlighter(fromLine, toLine, attributes, editor);

            highlight(attributes, color);// might add special attributes color based on type of color of highlighting

            stripeHighlighter.highlight(highlighter, color, testName);
            String fromToString = String.valueOf(fromLine) + " " + String.valueOf(toLine);
            sideHighlighter.highlight(highlighter, color);
            if (highlights.get(editor.getMarkupModel().toString())==null){
                List<RangeHighlighter> rangeHighlighterList = new ArrayList<>();
                rangeHighlighterList.add(highlighter);
                highlights.put(editor.getMarkupModel().toString(), rangeHighlighterList);

                //setting specific highlighter to specific lines
                highlighters.computeIfAbsent(editor.getMarkupModel().toString(), k -> new Hashtable<>());
                highlighters.get(editor.getMarkupModel().toString()).put(fromToString, highlighter);
            }
            else{
                List<RangeHighlighter> rangeHighlighterList = highlights.get(editor.getMarkupModel().toString());
                rangeHighlighterList.add(highlighter);
                highlighters.computeIfAbsent(editor.getMarkupModel().toString(), k -> new Hashtable<>());
                highlighters.get(editor.getMarkupModel().toString()).put(fromToString, highlighter);
            }
        }
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
    }

    public boolean clearSpecificHighlight(Editor editor, int fromLine, int toLine) {
        MarkupModel model = editor.getMarkupModel();
        String specificKey = String.valueOf(fromLine) + " " + String.valueOf(toLine);
        RangeHighlighter targetOfRemoval;
        if (highlighters.containsKey(model.toString()))
            targetOfRemoval = highlighters.get(model.toString()).get(specificKey);
        else return false;

        highlighters.get(model.toString()).remove(specificKey);

        List<RangeHighlighter> rangeHighlighterList = highlights.get(model.toString());
        if (rangeHighlighterList != null && rangeHighlighterList.size() != 0) {
            for (RangeHighlighter rangeHighlighter : rangeHighlighterList) {
                if (rangeHighlighter == targetOfRemoval)
                    model.removeHighlighter(rangeHighlighter);
            }
        }
        highlights.get(model.toString()).remove(targetOfRemoval);
        return true;
    }

    private PsiElement getLastChild(PsiElement element){
        if(element.getChildren().length==0)return element;
        else return getLastChild(element.getLastChild());
    }
}
