package edu.fiit.schneider_plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import edu.fiit.schneider_plugin.comment_util.Extractor;
import edu.fiit.schneider_plugin.comment_util.Merger;

import java.util.ArrayList;
import java.util.List;

public class FindComments extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //Open psifile by given project and editor
        //PsiManager manager = PsiManager.getInstance(project); //maybe needed later, code stays to help me not forget
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);

        List<PsiComment> allComments = Extractor.getCommentsFromPsiFile(psiFile);   //get list of all comments from file

        List<List<PsiComment>> mergedComments= Merger.mergeByPosition(allComments);
        System.out.println();
        List<List<PsiElement>> commentTargets = new ArrayList<>();
        for(List<PsiComment> actualList : mergedComments){
            commentTargets.add(Extractor.extractTargets(actualList.get(actualList.size()-1)));
        }

    }


}
