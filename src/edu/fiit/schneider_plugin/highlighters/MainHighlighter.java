package edu.fiit.schneider_plugin.highlighters;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.IntentionManager;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.javadoc.PsiDocCommentImpl;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.util.IncorrectOperationException;
import edu.fiit.schneider_plugin.intelij.util.EditorUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

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
    private final static Color COMMENT_WITH_NO_TARGET_HIGHLIGHT_COLOR = new Color(191, 134, 123, 255);


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
     * Highlights all elements from given list, color is chosen based on errorCode.
     * @param psiElements elements to be highlighted
     * @param problem text representation of problem
     * @param errorCode codes with problem representation : 0-coherence, 1- target extraction, 2- comment has no target,
     *                  3- target highlighting
     */
    public void highlight(List<? extends PsiElement> psiElements, String problem, int errorCode) {
        Integer fromLine, toLine;
        Annotation annotation;
        IntentionAction akcia = new IntentionAction() {
            @Nls
            @NotNull
            @Override
            public String getText() {
                return "Text";
            }

            @Nls
            @NotNull
            @Override
            public String getFamilyName() {
                return "familyName";
            }

            @Override
            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
                return false;
            }

            @Override
            public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
                System.out.println("dasdasd");
            }

            @Override
            public boolean startInWriteAction() {
                System.out.println("startInWriteAction");
                return false;
            }
        };
        PsiElement first = psiElements.get(0);
        PsiElement last = psiElements.get(psiElements.size() - 1);
        Class<? extends PsiElement> decisionClass = psiElements.get(0).getClass();
        RangeHighlighter rangeHighlighter;

        fromLine = setFromLine(psiElements);
        toLine = setToLine(psiElements);

        Editor editor =FileEditorManager.getInstance(psiElements.get(0).getProject()).getSelectedTextEditor();
        rangeHighlighter = createRangeHighlighter(fromLine, toLine, new TextAttributes(),editor);

        if (decisionClass == PsiCommentImpl.class || decisionClass == PsiDocCommentImpl.class) {
            if (errorCode == 0) {
                highlightErrorStripe(rangeHighlighter, COMMENT_HIGHLIGHT_COLOR, problem);
                this.highlightLines(COMMENT_HIGHLIGHT_COLOR, fromLine, toLine, problem, editor);
                annotation = new Annotation(first.getTextOffset(), last.getTextOffset(),
                        new HighlightSeverity("highlight", 5), "message", "tooltip");
                annotation.registerFix(akcia);
                IntentionManager.getInstance().addAction(akcia);
            }
            if (errorCode == 2) {
                highlightErrorStripe(rangeHighlighter, COMMENT_WITH_NO_TARGET_HIGHLIGHT_COLOR, problem);
                this.highlightLines(COMMENT_WITH_NO_TARGET_HIGHLIGHT_COLOR, fromLine, toLine, problem, editor);
            }
        } else {
            highlightErrorStripe(rangeHighlighter, TARGET_HIGHLIGHT_COLOR, problem);
            this.highlightLines(TARGET_HIGHLIGHT_COLOR, fromLine, toLine, problem, editor);
        }

    }

    /**
     * Highlight lines with given color, from given fromLine up to toLine in given editor. Sidebar is highlighted with
     * the same color, and will show message of testName.
     *
     * @param color    color to highlight
     * @param fromLine line to highlight from, indexed as in IDEA
     * @param toLine   line to highlight to, indexed as in IDEA
     * @param testName message to show on sidebar
     * @param editor   editor to highlight in
     */
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
            } else {
                List<RangeHighlighter> rangeHighlighterList = highlights.get(editor.getMarkupModel().toString());
                rangeHighlighterList.add(highlighter);
                highlighters.computeIfAbsent(editor.getMarkupModel().toString(), k -> new Hashtable<>());
                highlighters.get(editor.getMarkupModel().toString()).put(fromToString, highlighter);
            }
        }
    }

    /**
     * Clears given editor of all highlighting.
     *
     * @param editor editor to clear of highglighting
     */
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

    /**
     * Clears highlighting based on editor and line numbers between is highlighting to be removed.
     * @param editor editor to search for highlighting
     * @param fromLine first line of highlighting targets, indexed as in IDEA
     * @param toLine last line of highlighting targets, indexed as in IDEA
     * @return true if highlighting was removed, false otherwise
     */
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

    /**
     * Returns last child of given element, recursive
     *
     * @param element element ot get last child from
     * @return last child of element, or element if if has no children
     */
    public PsiElement getLastChild(PsiElement element){
        if(element.getChildren().length==0)return element;
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

    /**
     * Return lineNumber of last element in list
     *
     * @param psiElements list of PsiElement to check for children
     */
    public int setToLine(List<? extends PsiElement> psiElements) {
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

}
