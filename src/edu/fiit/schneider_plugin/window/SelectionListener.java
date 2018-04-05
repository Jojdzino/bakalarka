package edu.fiit.schneider_plugin.window;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("FieldCanBeLocal")
class SelectionListener implements ListSelectionListener {

    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();

        int firstIndex = e.getFirstIndex();
        int lastIndex = e.getLastIndex();
        boolean isAdjusting = e.getValueIsAdjusting();
        int selectedIndex = 0;
        if (!lsm.isSelectionEmpty()) {
            // Find out which indexes are selected.
            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if (lsm.isSelectedIndex(i)) {
                    selectedIndex = i;
                }
            }
        }
        if (isAdjusting) return;
        Editor editorToBeSelected = TableController.getEditorList().get(selectedIndex);
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        VirtualFile file = FileDocumentManager.getInstance().getFile(editorToBeSelected.getDocument());
        //String s = FileEditorProviderManager.getInstance().getProviders(project, file).toString();
        //FileEditorManager.getInstance(project).setSelectedEditor(file, s);
        FileEditorManager.getInstance(project).openEditor(new OpenFileDescriptor(project, file), true);
        RangeHighlighter selectedHighlighter = TableController.getRangeHighlighterList().get(selectedIndex);
        //positioning selection
        LogicalPosition posStart, posEnd;
        posStart = FileEditorManager.getInstance(project).getSelectedTextEditor().
                offsetToLogicalPosition(selectedHighlighter.getStartOffset());
        posEnd = FileEditorManager.getInstance(project).getSelectedTextEditor().
                offsetToLogicalPosition(selectedHighlighter.getEndOffset());
        FileEditorManager.getInstance(project).getSelectedTextEditor().getSelectionModel().setBlockSelection(
                posStart,
                posEnd
        );
        FileEditorManager.getInstance(project).getSelectedTextEditor().getScrollingModel().scrollVertically(selectedHighlighter.getStartOffset());
    }
}
