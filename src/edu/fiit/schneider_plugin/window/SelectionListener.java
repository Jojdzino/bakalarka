package edu.fiit.schneider_plugin.window;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
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

class SelectionListener implements ListSelectionListener {

    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();

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
        Editor selectedEditor = TableController.getEditorList().get(selectedIndex);
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        VirtualFile file = FileDocumentManager.getInstance().getFile(selectedEditor.getDocument());
        assert file != null;
        FileEditorManager.getInstance(project).openEditor(new OpenFileDescriptor(project, file), true);
        RangeHighlighter selectedHighlighter = TableController.getRangeHighlighterList().get(selectedIndex);
        //positioning selection
        selectedEditor.getSelectionModel().setSelection(
                selectedHighlighter.getStartOffset(),
                selectedHighlighter.getEndOffset()
        );
        //sets caret to endOffset of given highlighter and then transforms caret offset to logical position and scrolls to it
        selectedEditor.getCaretModel().moveToOffset(selectedHighlighter.getEndOffset());
        selectedEditor.getScrollingModel().scrollTo(selectedEditor.offsetToLogicalPosition(selectedEditor.getCaretModel().getOffset()),
                ScrollType.CENTER_DOWN
        );
    }
}
