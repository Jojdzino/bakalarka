package edu.fiit.schneider_plugin.highlighters;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import edu.fiit.schneider_plugin.entity.WarningType;
import edu.fiit.schneider_plugin.intelij.util.EditorUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Highly inspired from https://github.com/xcegin/PUTVT
 */
@SuppressWarnings({"UseJBColor", "WeakerAccess", "Duplicates"})
public class MainHighlighter {

    private HashMap<String, TreeMap<String, RangeHighlighter>> highlighters;
    private static MainHighlighter instance = null;

    public static MainHighlighter getInstance() {
        if (instance == null) {
            instance = new MainHighlighter();
        }
        return instance;
    }

    private MainHighlighter() {
        highlighters = new HashMap<>();
    }

    private static void highlight(TextAttributes textAttributes, Color color) {
        textAttributes.setBackgroundColor(color);
    }

    public static void highlightErrorStripe(RangeHighlighter rangeHighlighter, Color color, String testName) {
        if (testName != null) {
            rangeHighlighter.setErrorStripeMarkColor(color);
            rangeHighlighter.setErrorStripeTooltip("Problem :\n" + testName);
        }
    }

    public static RangeHighlighter createRangeHighlighter(int fromLine, int toLine, WarningType warningType, Editor editor) {
        Document document = editor.getDocument();

        int lineStartOffset = document.getLineStartOffset(Math.max(0, fromLine));
        int lineEndOffset = document.getLineEndOffset(Math.max(0, toLine));
        TextAttributes attributes;
        attributes = ElementTextAtributesCreator.createContrastTextAttributes(warningType);

        return editor.getMarkupModel().addRangeHighlighter(
                lineStartOffset, lineEndOffset, 3333, attributes, HighlighterTargetArea.EXACT_RANGE);

    }

    /**
     * Highlights all elements from given list, color is chosen based on errorCode. Targets are automatically assigned
     * with default color, in their case errorCode number does not matter.
     * @param psiElements elements to be highlighted
     * @param problem text representation of problem
     * @param errorCode codes with problem representation : 0-coherence, 1- target extraction, 2- comment has no target,
     *                  3- other highlighting for comments
     */
    public void highlight(List<? extends PsiElement> psiElements, String problem, int errorCode, WarningType warning) {
        RangeHighlighter rangeHighlighter;
        int fromLine = setFromLine(psiElements);
        int toLine = getToLine(psiElements);
        Editor editor = FileEditorManager.getInstance(psiElements.get(0).getProject()).getSelectedTextEditor();

        if (psiElements.get(0) instanceof PsiComment) {
            switch (errorCode) {
                case 0://quality comment
                    switch (warning) {
                        case INFO:
                            rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.INFO_BACKGROUND,
                                    fromLine, toLine, problem, editor, warning);
                            highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.INFO_BACKGROUND, problem);
                            break;

                        case WARNING:
                            rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.WARNING_BACKGROUND,
                                    fromLine, toLine, problem, editor, warning);
                            highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.WARNING_BACKGROUND, problem);
                            break;

                        case ERROR:
                            rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.ERROR_BACKGROUND,
                                    fromLine, toLine, problem, editor, warning);
                            highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.ERROR_BACKGROUND, problem);
                    }
                    break;
                case 1://comment with no target
                    rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.WARNING_BACKGROUND, fromLine,
                            toLine, problem, editor, warning);
                    highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.WARNING_BACKGROUND, problem);
                    break;

                case 2://comment has no target
                    rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.ERROR_BACKGROUND, fromLine,
                            toLine, problem, editor, warning);
                    highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.ERROR_BACKGROUND, problem);
                    break;

                case 3://other highlighting
                    rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.ERROR_BACKGROUND, fromLine,
                            toLine, problem, editor, warning);
                    highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.ERROR_BACKGROUND, problem);
            }
        } else if (warning == WarningType.TARGET) {
            rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.TARGET_BACKGROUND,
                    fromLine, toLine, problem, editor, warning);
            highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.TARGET_BACKGROUND, problem);

        }
    }

    /**
     * Highlight lines with given color, from given fromLine up to toLine in given editor. Sidebar is highlighted with
     * the same color, and will show message of testName.
     *  @param color    color to highlight
     * @param fromLine line to highlight from, indexed as in IDEA
     * @param toLine   line to highlight to, indexed as in IDEA
     * @param testName message to show on sidebar
     * @param editor   editor to highlight in
     */
    public RangeHighlighter highlightLines(final Color color, int fromLine, int toLine, String testName, Editor editor, WarningType warning) {
        Document document = editor.getDocument();
        SideHighlighter sideHighlighter = new SideHighlighter();

        if (toLine <= document.getLineCount()) {
            TextAttributes attributes = new TextAttributes();

            RangeHighlighter highlighter = createRangeHighlighter(fromLine, toLine, warning, editor);

            highlight(attributes, color);

            String fromToString = String.valueOf(fromLine) + " " + String.valueOf(toLine);
            sideHighlighter.highlight(highlighter, color);

            //setting specific highlighter to specific lines
            createTreeMap(editor);
            highlighters.get(editor.getMarkupModel().toString()).put(fromToString, highlighter);
            return highlighter;
        }
        return null;
    }

    private void createTreeMap(Editor editor) {
        if (!highlighters.containsKey(editor.getMarkupModel().toString()))
            highlighters.put(editor.getMarkupModel().toString(), new TreeMap<>((first, second) -> {
                int first1 = Integer.parseInt(first.split(" ")[0]);
                int first2 = Integer.parseInt(first.split(" ")[0]);
                int second1 = Integer.parseInt(second.split(" ")[0]);
                int second2 = Integer.parseInt(second.split(" ")[1]);
                if (first1 == first2 && second1 == second2) {
                    return Integer.compare(first1, second1);
                }
                int dif1 = Math.abs(first1 - second1);
                int dif2 = Math.abs(first2 - second2);
                if (dif1 == dif2)
                    return Integer.compare(dif1, dif2);
                return first.hashCode() - second.hashCode();
            }));
    }

    /**
     * Return lineNumber of last element in list
     *
     * @param psiElements list of PsiElement to check for children
     */
    public int getToLine(List<? extends PsiElement> psiElements) {
        int toLine;
        if (psiElements.size() == 1) {
            if (psiElements.get(0).getChildren().length != 0) {
                toLine = EditorUtil.getLineOfElement(getLastChild(psiElements.get(0)));//comment
            } else {
                toLine = EditorUtil.getLineOfElementWithOffset(psiElements.get(0));
            }
        } else {
            //If last element has children
            PsiElement element = psiElements.get(psiElements.size() - 1);
            if (element.getChildren().length != 0)
                toLine = EditorUtil.getLineOfElement(getLastChild(element));
            else toLine = EditorUtil.getLineOfElement(element);
        }
        return toLine;
    }

    public HashMap<String, TreeMap<String, RangeHighlighter>> getHighlighters() {
        return highlighters;
    }

    /**
     * Returns last child of given element, recursive
     *
     * @param element element ot get last child from
     * @return last child of element, or element if if has no children
     */
    public PsiElement getLastChild(PsiElement element) {
        if (element.getChildren().length == 0) return element;
        else return getLastChild(element.getLastChild());
    }

    /**
     * Return lineNumber of first element in list
     *
     * @param psiElements list of PsiElement to check for children
     */
    public int setFromLine(List<? extends PsiElement> psiElements) {
        int fromLine;
        if (psiElements.size() == 1) {
            if (psiElements.get(0).getChildren().length != 0) {
                fromLine = EditorUtil.getLineOfElement(psiElements.get(0).getFirstChild());
            } else {
                fromLine = EditorUtil.getLineOfElement(psiElements.get(0));
            }
        } else {
            //If first element has children
            if (psiElements.get(0).getChildren().length != 0)
                fromLine = EditorUtil.getLineOfElement(psiElements.get(0).getFirstChild());
            else fromLine = EditorUtil.getLineOfElement(psiElements.get(0));
        }
        return fromLine;
    }

    public List<RangeHighlighter> getHighlightersByEditor(Editor editor) {
        TreeMap<String, RangeHighlighter> x = highlighters.get(editor.getMarkupModel().toString());
        if (x == null) return null;
        return new ArrayList<>(x.values());
    }
}
