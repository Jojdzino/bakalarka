package edu.fiit.schneider_plugin.startup;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.impl.ToolWindowManagerImpl;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiTreeChangeListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.*;

public class CodeChangeListener implements PsiTreeChangeListener {


    public static void updateTable() {

        //remove highlighters that are too short
        Editor selectedEditor = FileEditorManager.getInstance(ProjectManager.getInstance().getOpenProjects()[0]).getSelectedTextEditor();
        assert selectedEditor != null;
        List<RangeHighlighter> arr = getFromLayer(Arrays.asList(selectedEditor.getMarkupModel().getAllHighlighters()));
        arr.sort(Comparator.comparingInt(RangeMarker::getStartOffset));

        Iterator<RangeHighlighter> iterator = arr.iterator();
        while (iterator.hasNext()) {
            RangeHighlighter actual = iterator.next();
            if (actual.getEndOffset() - actual.getStartOffset() <= 0) {
                selectedEditor.getMarkupModel().removeHighlighter(actual);
                iterator.remove();
            }
        }
        //update table
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        if (ToolWindowManagerImpl.getInstance(project).
                getToolWindow("Tabbed pane") != null)
            ToolWindowManagerImpl.
                    getInstance(project).
                    getToolWindow("Tabbed pane").
                    getComponent().
                    getComponents()[1].
                    getComponentAt(0, 0).
                    getListeners(ChangeListener.class)[0].
                    stateChanged(new ChangeEvent(selectedEditor));//mindblowing way to call controller method
    }

    private static List<RangeHighlighter> getFromLayer(List<RangeHighlighter> rangeHighlighters) {
        List<RangeHighlighter> newList = new ArrayList<>();
        for (RangeHighlighter x : rangeHighlighters)
            if (x.getLayer() == 3333)
                newList.add(x);
        return newList;
    }

    @Override
    public void beforeChildAddition(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
    }

    @Override
    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        updateTable();
    }

    @Override
    public void beforeChildReplacement(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {

    }

    @Override
    public void beforeChildMovement(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {

    }

    @Override
    public void beforeChildrenChange(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {

    }

    @Override
    public void beforePropertyChange(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {

    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {

    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {

    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {

    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {

    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {

    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {

    }
}
