package edu.fiit.schneider_plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiUtilBase;
import edu.fiit.schneider_plugin.comment_util.CommentExtractor;

import java.util.List;

public class FindComments extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //Open psifile by given project and editor
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        PsiManager manager = PsiManager.getInstance(project);
        Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        //get list of all comments from file
        List<PsiComment> allComments = CommentExtractor.getCommentsFromPsiFile(psiFile);

        System.out.println();

    }


}
