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
import edu.fiit.schneider_plugin.action.intention.ClearSingleTargetAction;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class IgnoreCommentAction extends PsiElementBaseIntentionAction implements IntentionAction {

    private static PsiElementFactory factory = null;

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        new ClearSingleTargetAction().invokeWithIgnore(project, editor, psiElement);
        if (factory == null) factory = PsiElementFactory.SERVICE.getInstance(project);
        if (psiElement instanceof PsiWhiteSpace)
            psiElement = psiElement.getPrevSibling();
        if (psiElement.getParent() instanceof PsiDocComment)
            psiElement = psiElement.getParent();
        PsiComment newComment = factory.createCommentFromText(createIgnoredComment(psiElement.getText()), psiElement);

        PsiElement finalPsiElement = psiElement; // lambda expression must be final -> what...
        WriteCommandAction.runWriteCommandAction(project, () -> {
            finalPsiElement.replace(newComment);
        });
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (psiElement instanceof PsiWhiteSpace && psiElement.getPrevSibling() instanceof PsiComment)
            psiElement = psiElement.getPrevSibling();
        if (psiElement.getParent() instanceof PsiDocComment)
            psiElement = psiElement.getParent();
        //if (psiElement instanceof PsiDocComment) return false; //cant ignore javadoc, too important
        if (psiElement.getText().contains("__I__"))
            return false;
        return true;
    }

    @NotNull
    @Override
    public String getText() {
        return "Ignore comment from highlighting";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    private String createIgnoredComment(String oldComment) {
        StringBuilder builder = new StringBuilder();
        if (oldComment.charAt(0) == '/' && oldComment.charAt(1) == '/') {
            return builder.append(oldComment).append(" __I__").toString();
        } else if (oldComment.charAt(1) == '*')
            return builder.append(oldComment.substring(oldComment.length() - 2)).append(" __I__*/").toString();
        return oldComment;
    }

}
