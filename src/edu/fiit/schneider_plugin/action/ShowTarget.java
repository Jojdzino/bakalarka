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
import edu.fiit.schneider_plugin.action.ignore.IgnoreComment;
import edu.fiit.schneider_plugin.comment_util.Extractor;
import edu.fiit.schneider_plugin.comment_util.Transformer;
import edu.fiit.schneider_plugin.config.ConfigAccesser;

import java.util.List;

public class ShowTarget extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {

        if (ConfigAccesser.getElement("selected_all_elements") != 1)
            return;

        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);

        List<PsiComment> allComments = Extractor.extractCommentsFromPsiFile(psiFile);

        if (allComments.size() == 0)
            return;

        List<List<PsiComment>> mergedComments = Transformer.mergeByPosition(allComments);

        PsiElement selectedElement = null;
        int offset = editor.getCaretModel().getOffset();
        if (psiFile != null)
            selectedElement = psiFile.findElementAt(offset);

        PsiComment targetComment = IgnoreComment.parentIsNotComment(selectedElement);

        int groupCounter = 0;
        boolean breaker = false;
        for (; groupCounter < mergedComments.size(); groupCounter++) {
            for (PsiComment actual : mergedComments.get(groupCounter))
                if (targetComment != null && targetComment.isEquivalentTo(actual)) {
                    breaker = true;
                    break;
                }
            if (breaker) break;
        }
        // najdena grouppa ktora obsahuje ten komentar, najst k nej target a highlight
        // spravim to tak, ze tato vec bude fungovat len ak clovek klikol highlight all
        //TODO continue here


    }

}
