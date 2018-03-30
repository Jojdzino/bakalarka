package edu.fiit.schneider_plugin.action.intention.ignore;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class UnIgnoreCommentAction extends PsiElementBaseIntentionAction implements IntentionAction {

    private static PsiElementFactory factory = null;

    private String createUnIgnoredComment(String oldComment) {
        return oldComment.replace("__IGNORE__", "");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        if (factory == null) factory = PsiElementFactory.SERVICE.getInstance(project);
        if (psiElement instanceof PsiWhiteSpace)
            psiElement = psiElement.getPrevSibling();
        PsiComment newComment = factory.createCommentFromText(createUnIgnoredComment(psiElement.getText()), psiElement);
        PsiElement finalPsiElement = psiElement;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            finalPsiElement.replace(newComment);
        });
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (psiElement instanceof PsiWhiteSpace && psiElement.getPrevSibling() instanceof PsiComment)
            psiElement = psiElement.getPrevSibling();
        if (psiElement instanceof PsiDocComment) return false;
        if (!psiElement.getText().contains("__IGNORE__"))
            return false;
        return true;
    }

    @NotNull
    @Override
    public String getText() {
        return "Unignore comment";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }
}
