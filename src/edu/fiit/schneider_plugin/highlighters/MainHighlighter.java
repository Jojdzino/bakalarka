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

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Inspired from https://github.com/xcegin/PUTVT
 */
@SuppressWarnings({"UseJBColor", "WeakerAccess", "Duplicates"})
public class MainHighlighter {

    private Map<String, Map<String, RangeHighlighter>> highlighters;
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

    public static void highlightErrorStripe(RangeHighlighter rangeHighlighter, Color color, String problem) {
        if (problem != null) {
            rangeHighlighter.setErrorStripeMarkColor(color);
            rangeHighlighter.setErrorStripeTooltip(problem);
        }
    }

    public static RangeHighlighter createRangeHighlighter(int fromLine, int toLine, WarningType warningType, Editor editor) {
        TextAttributes attributes = ElementTextAtributesCreator.createContrastTextAttributes(warningType);

        return editor.getMarkupModel().addRangeHighlighter(
                fromLine, toLine, 3333, attributes, HighlighterTargetArea.EXACT_RANGE);

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
        int fromOffset = getFromOffset(psiElements);
        int toOffset = getToOffset(psiElements);
        Editor editor = FileEditorManager.getInstance(psiElements.get(0).getProject()).getSelectedTextEditor();

        if (psiElements.get(0) instanceof PsiComment) {
            switch (errorCode) {
                case 0://quality comment
                    switch (warning) {
                        case INFO:
                            rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.INFO_BACKGROUND,
                                    fromOffset, toOffset, editor, warning);
                            highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.INFO_BACKGROUND,
                                    "Problem :\n" + problem);
                            break;

                        case WARNING:
                            rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.WARNING_BACKGROUND,
                                    fromOffset, toOffset, editor, warning);
                            highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.WARNING_BACKGROUND,
                                    "Problem :\n" + problem);
                            break;

                        case ERROR:
                            rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.ERROR_BACKGROUND,
                                    fromOffset, toOffset, editor, warning);
                            highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.ERROR_BACKGROUND,
                                    "Problem :\n" + problem);
                    }
                    break;
                case 1://comment with no target
                    rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.WARNING_BACKGROUND, fromOffset,
                            toOffset, editor, warning);
                    highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.WARNING_BACKGROUND,
                            "Problem :\n" + problem);
                    break;

                case 2://comment has no target
                    rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.ERROR_BACKGROUND, fromOffset,
                            toOffset, editor, warning);
                    highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.ERROR_BACKGROUND,
                            "Problem :\n" + problem);
                    break;

                case 3://other highlighting
                    rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.ERROR_BACKGROUND, fromOffset,
                            toOffset, editor, warning);
                    highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.ERROR_BACKGROUND,
                            "Problem :\n" + problem);
            }
        } else if (warning == WarningType.TARGET) {
            rangeHighlighter = this.highlightLines(ElementTextAtributesCreator.TARGET_BACKGROUND,
                    fromOffset, toOffset, editor, warning);
            highlightErrorStripe(rangeHighlighter, ElementTextAtributesCreator.TARGET_BACKGROUND,
                    "Target of comment");

        }
    }

    /**
     * Highlight lines with given color, from given fromLine up to toLine in given editor. Sidebar is highlighted with
     * the same color, and will show message of testName.
     *  @param color    color to highlight
     * @param fromOffset line to highlight from, indexed as in IDEA
     * @param toOffset   line to highlight to, indexed as in IDEA
     * @param editor   editor to highlight in
     */
    public RangeHighlighter highlightLines(final Color color, int fromOffset, int toOffset, Editor editor, WarningType warning) {
        Document document = editor.getDocument();
        SideHighlighter sideHighlighter = new SideHighlighter();

        if (toOffset <= document.getTextLength()) {
            TextAttributes attributes = new TextAttributes();

            RangeHighlighter highlighter = createRangeHighlighter(fromOffset, toOffset, warning, editor);

            highlight(attributes, color);
            //map has keys from and toLine
            int fromLine = editor.getDocument().getLineNumber(fromOffset);
            int toLine = editor.getDocument().getLineNumber(toOffset);
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
    public int getToOffset(List<? extends PsiElement> psiElements) {
        int toOffset;
        PsiElement lastChild;
        if (psiElements.size() == 1) {
            if (psiElements.get(0).getChildren().length != 0) {
                toOffset = psiElements.get(0).getTextOffset() + psiElements.get(0).getTextLength();
            } else {
                lastChild = getLastChild(psiElements.get(0));
                toOffset = lastChild.getTextOffset() + lastChild.getTextLength();
            }
        } else {
            //If last element has children
            PsiElement element = psiElements.get(psiElements.size() - 1);
            if (element.getChildren().length != 0) {
                lastChild = getLastChild(element);
                toOffset = lastChild.getTextOffset() + lastChild.getTextLength();
            } else
                toOffset = element.getTextOffset() + element.getTextLength();
        }
        return toOffset;
    }

    public Map<String, Map<String, RangeHighlighter>> getHighlighters() {
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
    public int getFromOffset(List<? extends PsiElement> psiElements) {
        int fromOffset;
        if (psiElements.size() == 1) {
            if (psiElements.get(0).getChildren().length != 0) {
                fromOffset = psiElements.get(0).getFirstChild().getTextOffset();
            } else {
                fromOffset = psiElements.get(0).getTextOffset();
            }
        } else {
            //If first element has children
            if (psiElements.get(0).getChildren().length != 0)
                fromOffset = psiElements.get(0).getFirstChild().getTextOffset();
            else
                fromOffset = psiElements.get(0).getTextOffset();
        }
        return fromOffset;
    }

    public List<RangeHighlighter> getHighlightersByEditor(Editor editor) {
        Map<String, RangeHighlighter> x = highlighters.get(editor.getMarkupModel().toString());
        if (x == null) return null;
        return new ArrayList<>(x.values());
    }
}
