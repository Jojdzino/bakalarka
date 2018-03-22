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
 * Taken from https://github.com/xcegin/PUTVT
 */
@SuppressWarnings({"UseJBColor", "WeakerAccess"})
public class MainHighlighter {

    private Hashtable<String, List<RangeHighlighter>> highlights;
    private static MainHighlighter instance = null;
    private final static int HIGHLIGHTING_INDEX = 50;
    private final static Color COMMENT_COLOR = new Color(94, 148, 89, 255);
    private final static Color TARGET_COLOR  = new Color(93, 89,150, 132);


    public static MainHighlighter getInstance() {
        if (instance == null) {
            instance = new MainHighlighter();
        }
        return instance;
    }

    private MainHighlighter() {
        highlights = new Hashtable<>();
    }

    /**
     * Highlights all elements from given list. Comments are highlighted with greenish color, and targets with blueish.
     * @param psiElements elements to be highlighted
     * @param problem text representation of problem
     */
    public void highlight(List<? extends PsiElement> psiElements, String problem) {
        //Jeden element moze mat deti, ale nemusi. Cize ak ma deti, zoberiem prve decko a posledne a highlightnem
        //od ich riakov. Ak nema deti highlightnem riadky od zaciatku tohto elementu po jeho koniec. Zistim si jeho
        //offset na zaciatku a aky je to riadok, a zistim si jeho offset + jeho dlzka a aky je to riadok
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
        fromLine++;
        toLine++;
        //TODO change testname to normal error string like cohorence is too big etc... DONE
        if (decisionClass == PsiCommentImpl.class || decisionClass == PsiDocCommentImpl.class) {//psidoccommentbase, nieco ako istypeof skusit
            highlight(rangeHighlighter, new TextAttributes(), COMMENT_COLOR, problem);
            this.highlightLines(COMMENT_COLOR,fromLine,toLine,"highlightLanes",editor);
        }
        else
            highlight(rangeHighlighter, new TextAttributes(), TARGET_COLOR, problem);
            this.highlightLines(TARGET_COLOR,fromLine,toLine,"highlightLanes",editor);

    }

    public void highlightLines(final Color color, int fromLine, int toLine, String testName, Editor editor) {
        //fromLine+=1;// indexovanie od 0
        Document document = editor.getDocument();
        SideHighlighter sideHighlighter = new SideHighlighter();

        LineHighlighter lineHighlighter = new LineHighlighter();
        ErrorStripeMarkHighlighter stripeHighlighter = new ErrorStripeMarkHighlighter();
        if (toLine <= document.getLineCount()) {
            TextAttributes attributes = new TextAttributes();

            RangeHighlighter highlighter = createRangeHighlighter(fromLine, toLine, attributes, editor);

            highlight(highlighter, attributes, color);// might add special attributes color based on type of color of highlighting

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
            lineHighlighter.setErrorStripeTooltip("Problem on this line is:\n" + testName);
        }
    }
    public static RangeHighlighter createRangeHighlighter(int fromLine, int toLine, TextAttributes attributes, Editor editor) {
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

    private PsiElement getLastChild(PsiElement element){
        if(element.getChildren().length==0)return element;
        else return getLastChild(element.getLastChild());
    }
}
