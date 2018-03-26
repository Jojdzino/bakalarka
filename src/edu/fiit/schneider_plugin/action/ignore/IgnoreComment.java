package edu.fiit.schneider_plugin.action.ignore;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.javadoc.PsiDocCommentImpl;
import com.intellij.psi.util.PsiUtilBase;
import edu.fiit.schneider_plugin.action.ClearSingleTarget;

public class IgnoreComment extends AnAction {

    private static PsiElementFactory factory=null;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiElement selectedElement = null;

        int offset = editor.getCaretModel().getOffset();
        if (psiFile != null)
             selectedElement = psiFile.findElementAt(offset);

        PsiElement targetComment=parentIsNotComment(selectedElement);
        if(targetComment==null)return;
        if(targetComment.getClass() == PsiDocCommentImpl.class) return; //cant ignore javadoc, too important
        if(targetComment.getText().contains("__IGNORE__"))return;       // why ignore ignored :)


        //clearing highlighting must be called before mofification
        new ClearSingleTarget().callAnActionFromIgnored(anActionEvent);

        if(factory == null)factory = PsiElementFactory.SERVICE.getInstance(project);

        PsiComment newComment = factory.createCommentFromText(createIgnoredComment(targetComment.getText()), targetComment);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            targetComment.replace(newComment);
        });
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public static PsiComment parentIsNotComment(PsiElement selectedElement) {
        if(selectedElement instanceof PsiComment)return (PsiComment) selectedElement;
        if(selectedElement.getParent() == null) return null;
        return parentIsNotComment(selectedElement.getParent());
    }

    private String createIgnoredComment(String oldComment){
        StringBuilder builder = new StringBuilder();
        if(oldComment.charAt(0)=='/' && oldComment.charAt(1)=='/'){
            return builder.append("//__IGNORE__").append(oldComment.substring(2)).toString();
        }
        else if(oldComment.charAt(1)=='*')
            return builder.append("/*__IGNORE__").append(oldComment.substring(2)).toString();
        return oldComment;
    }
}
