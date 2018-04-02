package edu.fiit.schneider_plugin;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiTreeChangeListener;
import com.intellij.psi.PsiWhiteSpace;
import edu.fiit.schneider_plugin.action.FindComments;
import edu.fiit.schneider_plugin.entity.Change;
import edu.fiit.schneider_plugin.highlighters.MainHighlighterMapRebuilder;
import edu.fiit.schneider_plugin.intelij.util.EditorUtil;
import org.jetbrains.annotations.NotNull;

public class CodeChangeListener implements PsiTreeChangeListener {

    @Override
    public void beforeChildReplacement(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        if (psiTreeChangeEvent.getFile() == null) return;
        if (FindComments.getHighlightedComments().size() == 0 && !psiTreeChangeEvent.getFile().getName().contains(".java"))
            return;
        if (!(psiTreeChangeEvent.getNewChild() instanceof PsiWhiteSpace) ||
                !(psiTreeChangeEvent.getOldChild() instanceof PsiWhiteSpace))
            return;
        int lineDif = countLineDifference(psiTreeChangeEvent.getOldChild(), psiTreeChangeEvent.getNewChild());
        int lineAt = EditorUtil.getLineOfElementWithOffset(psiTreeChangeEvent.getOldChild());
        if (psiTreeChangeEvent.getFile() != null && lineDif != 0) {
            if (lineDif > 0)
                MainHighlighterMapRebuilder.getInstance().rebuildMapPlus(new Change(lineAt, lineDif),
                        psiTreeChangeEvent.getParent().getProject());
            else MainHighlighterMapRebuilder.getInstance().rebuildMapMinus(new Change(lineAt, lineDif),
                    psiTreeChangeEvent.getParent().getProject());

        }
    }

    //return how many new lines were added
    private int countLineDifference(PsiElement oldChild, PsiElement newChild) {
        int oldLinesCount, newLinesCount;
        oldLinesCount = oldChild.getText().length() - oldChild.getText().replace("\n", "").length();
        newLinesCount = newChild.getText().length() - newChild.getText().replace("\n", "").length();
        return newLinesCount - oldLinesCount;
    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        if (psiTreeChangeEvent.getFile() == null) return;
        if (FindComments.getHighlightedComments().size() == 0 && !psiTreeChangeEvent.getFile().getName().contains(".java"))
            return;
        if (!(psiTreeChangeEvent.getChild() instanceof PsiWhiteSpace))
            return;
        add(psiTreeChangeEvent);//incrementing
    }

    @Override
    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
        if (psiTreeChangeEvent.getFile() == null) return;
        if (FindComments.getHighlightedComments().size() == 0 && !psiTreeChangeEvent.getFile().getName().contains(".java"))
            return;
        if (!(psiTreeChangeEvent.getChild() instanceof PsiWhiteSpace))
            return;
        remove(psiTreeChangeEvent);//decrementing
    }

    private void remove(PsiTreeChangeEvent psiTreeChangeEvent) {
        if (psiTreeChangeEvent.getChild().getText().contains("\n")) {
            int lineAt = EditorUtil.getLineOfElementWithOffset(psiTreeChangeEvent.getChild());
            int lineDif = psiTreeChangeEvent.getChild().getText().length() -
                    psiTreeChangeEvent.getChild().getText().replace("\n", "").length();
            lineDif = Math.abs(lineDif) * -1;
            MainHighlighterMapRebuilder.getInstance().rebuildMapMinus(new Change(lineAt, lineDif),
                    psiTreeChangeEvent.getParent().getProject());

        }
    }

    private void add(PsiTreeChangeEvent psiTreeChangeEvent) {
        if (psiTreeChangeEvent.getChild().getText().contains("\n")) {
            int lineAt = EditorUtil.getLineOfElementWithOffset(psiTreeChangeEvent.getChild());
            int lineDif = psiTreeChangeEvent.getChild().getText().length() -
                    psiTreeChangeEvent.getChild().getText().replace("\n", "").length();
            MainHighlighterMapRebuilder.getInstance().rebuildMapPlus(new Change(lineAt, lineDif),
                    psiTreeChangeEvent.getParent().getProject());
        }
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

    @Override
    public void beforeChildAddition(@NotNull PsiTreeChangeEvent psiTreeChangeEvent) {
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
}
