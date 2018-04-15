package edu.fiit.schneider_plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import edu.fiit.schneider_plugin.highlighters.HighlightingClearer;
import edu.fiit.schneider_plugin.startup.CodeChangeListener;

import java.util.Arrays;
import java.util.List;

public class ClearAllEditors extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        List<Editor> allEditors = Arrays.asList(EditorFactory.getInstance().getAllEditors());
        for (Editor editor : allEditors)
            HighlightingClearer.clear(editor);

        FindComments.clearHighlighterComments();
        CodeChangeListener.updateTable();
    }
}
