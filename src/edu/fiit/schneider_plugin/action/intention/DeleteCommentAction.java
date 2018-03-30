package edu.fiit.schneider_plugin.action.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.IncorrectOperationException;
import edu.fiit.schneider_plugin.action.FindComments;
import edu.fiit.schneider_plugin.comment_util.Extractor;
import edu.fiit.schneider_plugin.highlighters.HighlightingClearer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DeleteCommentAction extends PsiElementBaseIntentionAction implements IntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        if (!isAvailable(project, editor, psiElement)) return;
        //removing highlighting
        int fromLine, toLine;
        fromLine = editor.getDocument().getLineNumber(psiElement.getTextOffset());
        toLine = editor.getDocument().getLineNumber(psiElement.getTextOffset() + psiElement.getTextLength());
        HighlightingClearer.clearSpecificHighlight(editor, fromLine, toLine);
        //deleting PsiElements
        List<PsiComment> list = FindComments.getHighlightedComments(psiElement);
        assert list != null;//this is decided by isAvailable function
        for (PsiComment psiComment : list)
            WriteCommandAction.runWriteCommandAction(project, psiComment::delete);

    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (!(psiElement instanceof PsiComment)) return false;//if it is no comment
        if (FindComments.getHighlightedComments(psiElement) == null) return false;//if it is not highlighted
        PsiComment comment = (PsiComment) psiElement;
        List<PsiElement> listOfTargets = Extractor.extractTargets(comment);
        for (PsiElement element : listOfTargets)
            if (!(element instanceof PsiJavaToken && element instanceof PsiWhiteSpace))
                return true;

        return false;
    }

    @NotNull
    public String getText() {
        return "Remove redundant comment";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }
}
