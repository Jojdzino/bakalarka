package edu.fiit.schneider_plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import edu.fiit.schneider_plugin.highlighters.HighlightingClearer;
import edu.fiit.schneider_plugin.startup.CodeChangeListener;

//clears target and updates table
public class ClearSingleEditor extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        HighlightingClearer.clear(editor);
        FindComments.clearHighlighterComments();
        CodeChangeListener.updateTable();
    }
}
