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
import edu.fiit.schneider_plugin.action.intention.ignore.IgnoreCommentAction;
import edu.fiit.schneider_plugin.comment_util.Extractor;
import edu.fiit.schneider_plugin.highlighters.MainHighlighter;

import java.util.List;

public class ShowTarget extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {

        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);

        List<List<PsiComment>> mergedComments = FindComments.getHighlightedComments();
        PsiElement selectedElement = null;
        int offset = editor.getCaretModel().getOffset();
        if (psiFile != null)
            selectedElement = psiFile.findElementAt(offset);

        PsiComment targetComment = IgnoreCommentAction.parentIsNotComment(selectedElement);

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
        if (!breaker) return;//comment was not found -> cursor wasnt at highlighted comment
        List<PsiComment> selectedGroup = mergedComments.get(groupCounter);
        List<PsiElement> targets = Extractor.extractTargets(selectedGroup.get(selectedGroup.size() - 1));

        MainHighlighter.getInstance().highlight(targets, "High coherence with target", 3);
    }

}
