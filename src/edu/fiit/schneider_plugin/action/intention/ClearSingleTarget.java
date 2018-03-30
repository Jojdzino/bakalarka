package edu.fiit.schneider_plugin.action.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.IncorrectOperationException;
import edu.fiit.schneider_plugin.action.FindComments;
import edu.fiit.schneider_plugin.highlighters.HighlightingClearer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("Duplicates")
public class ClearSingleTarget extends PsiElementBaseIntentionAction implements IntentionAction {

    private static final String IGNORE_MESSAGE = "Use '__IGNORE__' in comments or target comments and select action Ignore comment";

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        //If psielement is white space target previous sibling -> look at function isAvailable
        if (psiElement instanceof PsiWhiteSpace)
            psiElement = psiElement.getPrevSibling();
        else if (psiElement.getParent() instanceof PsiComment)
            psiElement = psiElement.getParent();

        int fromLine, toLine;
        Document document = editor.getDocument();

        List<? extends PsiElement> groupOfComments = FindComments.removeList(psiElement);
        if (groupOfComments == null) return;

        int textOffset = groupOfComments.get(0).getTextOffset();
        fromLine = document.getLineNumber(textOffset);
        PsiElement lastElement = groupOfComments.get(groupOfComments.size() - 1);
        textOffset = lastElement.getTextOffset() + lastElement.getText().length();
        toLine = document.getLineNumber(textOffset);

        HighlightingClearer.clearSpecificHighlight(editor, fromLine, toLine);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        //Programmer might mainly click on white space after comment, because it is significantly bigger than comment himself...
        //His intentions are probably to click on comment
        if (psiElement.getParent() instanceof PsiComment) psiElement = psiElement.getParent();
        else if (psiElement instanceof PsiWhiteSpace && psiElement.getPrevSibling() instanceof PsiComment)
            return FindComments.getHighlightedComments(psiElement.getPrevSibling()) != null;
        return FindComments.getHighlightedComments(psiElement) != null;
    }

    @NotNull
    @Override
    public String getText() {
        return "Clear highlighting";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    public void invokeWithIgnore(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        invoke(project, editor, psiElement);
        //DataContext context = DataManager.getInstance().getDataContext();
        StatusBar statusBar = WindowManager.getInstance()
                .getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(IGNORE_MESSAGE, MessageType.INFO, null)
                .setFadeoutTime(10000)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                        Balloon.Position.atRight);
    }

}
