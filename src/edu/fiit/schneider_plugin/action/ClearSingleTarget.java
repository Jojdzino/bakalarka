package edu.fiit.schneider_plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import edu.fiit.schneider_plugin.action.ignore.IgnoreComment;
import edu.fiit.schneider_plugin.highlighters.MainHighlighter;

import java.util.List;

public class ClearSingleTarget extends AnAction {
    @SuppressWarnings("WeakerAccess")
    public final String IGNORE_MESSAGE = "Use '__IGNORE__' in comments or target comments and select action Ignore comment";

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        assert psiFile != null;
        Document document = psiDocumentManager.getDocument(psiFile);
        assert document != null;
        PsiElement selectedElement;
        int fromLine, toLine;
        int offset = editor.getCaretModel().getOffset();
        selectedElement = psiFile.findElementAt(offset);
        assert selectedElement != null;
        //bude fungovat len na komenty
        PsiElement targetComment = IgnoreComment.parentIsNotComment(selectedElement);
        if (targetComment == null) return;


        List<? extends PsiElement> groupOfComments = FindComments.removeList(targetComment);
        if (groupOfComments == null) return;

        int textOffset = groupOfComments.get(0).getTextOffset();
        fromLine = document.getLineNumber(textOffset);
        PsiElement lastElement = groupOfComments.get(groupOfComments.size() - 1);
        textOffset = lastElement.getTextOffset() + lastElement.getText().length();
        toLine = document.getLineNumber(textOffset);

        while (!MainHighlighter.getInstance().clearSpecificHighlight(editor, fromLine, toLine)) {
            fromLine++;
            toLine++;
        }
        StatusBar statusBar = WindowManager.getInstance()
                .getStatusBar(CommonDataKeys.PROJECT.getData(anActionEvent.getDataContext()));
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(IGNORE_MESSAGE, MessageType.INFO, null)
                .setFadeoutTime(10000)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()),
                        Balloon.Position.atRight);
    }
}
