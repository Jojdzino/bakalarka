package edu.fiit.schneider_plugin.action.intention.ignore;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.util.IncorrectOperationException;
import edu.fiit.schneider_plugin.action.ClearSingleTarget;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class IgnoreCommentAction extends PsiElementBaseIntentionAction implements IntentionAction {

    private static PsiElementFactory factory = null;

    @SuppressWarnings("SimplifiableIfStatement")
    public static PsiComment parentIsNotComment(PsiElement selectedElement) {
        if (selectedElement instanceof PsiComment) return (PsiComment) selectedElement;
        if (selectedElement.getParent() == null) return null;
        return parentIsNotComment(selectedElement.getParent());
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        ClearSingleTarget.clearSingleTarget(editor, psiElement);
        if (factory == null) factory = PsiElementFactory.SERVICE.getInstance(project);

        PsiComment newComment = factory.createCommentFromText(createIgnoredComment(psiElement.getText()), psiElement);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            psiElement.replace(newComment);
        });
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        PsiElement targetComment = parentIsNotComment(psiElement);

        if (targetComment instanceof PsiDocComment) return false; //cant ignore javadoc, too important
        if (targetComment != null && targetComment.getText().contains("__IGNORE__"))
            return false;// why ignore ignored :)
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
            return builder.append("//__IGNORE__").append(oldComment.substring(2)).toString();
        } else if (oldComment.charAt(1) == '*')
            return builder.append("/*__IGNORE__").append(oldComment.substring(2)).toString();
        return oldComment;
    }

}
