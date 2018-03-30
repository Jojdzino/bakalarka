package edu.fiit.schneider_plugin.action.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.IncorrectOperationException;
import edu.fiit.schneider_plugin.WarningType;
import edu.fiit.schneider_plugin.action.FindComments;
import edu.fiit.schneider_plugin.comment_util.Extractor;
import edu.fiit.schneider_plugin.highlighters.MainHighlighter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShowTargetAction extends PsiElementBaseIntentionAction implements IntentionAction {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        if (psiElement.getParent() instanceof PsiComment) psiElement = psiElement.getParent();
        List<PsiComment> commentList = FindComments.getHighlightedComments(psiElement);
        if (commentList == null)
            return;
        List<PsiElement> targets = Extractor.extractTargets(commentList.get(commentList.size() - 1));

        MainHighlighter.getInstance().highlight(targets, "Target of\n ' " + targets.toString() + " '",
                3, WarningType.INFO);

    }

    //Im gonna kil myself with those simplify if... 3 ternary in one line OMG
    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (psiElement.getParent() instanceof PsiComment) psiElement = psiElement.getParent();
        if (!(psiElement instanceof PsiComment)) return false;
        if (psiElement instanceof PsiWhiteSpace && psiElement.getPrevSibling() instanceof PsiComment)
            psiElement = psiElement.getPrevSibling();
        if (FindComments.getHighlightedComments(psiElement) == null) return false;
        return true;
    }

    @NotNull
    @Override
    public String getText() {
        return "Show target of comment";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }
}