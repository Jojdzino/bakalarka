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
import edu.fiit.schneider_plugin.comment_util.Analyser;
import edu.fiit.schneider_plugin.comment_util.Checker;
import edu.fiit.schneider_plugin.comment_util.Extractor;
import edu.fiit.schneider_plugin.comment_util.Transformer;
import edu.fiit.schneider_plugin.config.ConfigAccesser;
import edu.fiit.schneider_plugin.entity.CommentTarget;
import edu.fiit.schneider_plugin.entity.enums.SpecialStatementType;
import edu.fiit.schneider_plugin.entity.enums.WarningType;
import edu.fiit.schneider_plugin.highlighters.MainHighlighter;
import edu.fiit.schneider_plugin.startup.CodeChangeListener;

import java.util.ArrayList;
import java.util.List;

public class FindComments extends AnAction {

    private static List<List<PsiComment>> highlightedComments = new ArrayList<>();
    @SuppressWarnings("FieldCanBeLocal")
    private final int WORD_COUNT_COEFFICIENT = 5;

    public static List<PsiComment> removeList(PsiElement element) {
        int i, j;
        boolean abort = false;
        for (i = 0; i < highlightedComments.size(); i++) {
            for (j = 0; j < highlightedComments.get(i).size(); j++)
                if (highlightedComments.get(i).get(j) == element) {
                    abort = true;
                    break;
                }
            if (abort) break;
        }
        if (!abort) return null;
        return highlightedComments.remove(i);
    }

    public static List<PsiComment> getHighlightedComments(PsiElement psiElement) {
        for (List<PsiComment> list : highlightedComments) {
            for (PsiComment comment : list) {
                if (comment.isEquivalentTo(psiElement))
                    return list;
            }
        }
        return null;
    }

    static void clearHighlighterComments() {
        highlightedComments.clear();
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //clears highlights from this editor and clears table to be filled with actual highlights later
        new ClearSingleEditor().actionPerformed(anActionEvent);
        CodeChangeListener.updateTable();

        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);


        List<PsiComment> allComments = Extractor.extractCommentsFromPsiFile(psiFile);
        if (allComments.size() == 0)
            return;

        List<List<PsiComment>> mergedComments = Transformer.mergeByPosition(allComments);

        List<List<PsiElement>> commentTargets = new ArrayList<>();
        for (List<PsiComment> actualList : mergedComments) {
            commentTargets.add(Extractor.extractTargets(actualList.get(actualList.size() - 1)));
        }

        //---------------------EXTRACTION COMPLETED------------------------ -> SPECIFICATION OF COMMENTS INTO GROUPs

        int counter = 0;
        List<List<PsiComment>> qualityComments = new ArrayList<>();//Used to store comments that target method, class or variable decl.
        List<List<PsiComment>> quantityComments = new ArrayList<>();//Used to store all other comments
        List<List<PsiElement>> quantityTargets = new ArrayList<>();
        List<CommentTarget> pair = new ArrayList<>();

        for (List<PsiElement> list : commentTargets) {
            int result = Extractor.targetSpecifier(list);
            if (result >= 1 && result <= 3) {
                qualityComments.add(mergedComments.get(counter));
                pair.add(new CommentTarget(mergedComments.get(counter), list, result));
            } else {
                quantityComments.add(mergedComments.get(counter));
                quantityTargets.add(list);
            }
            counter++;
        }

        //---------------------SPECIFICATION COMPLETED------------------------ -> HIGHLIGHTING OF COMMENTS
        //1. qualitative comment <-> target pairs with coherence highlighting
        for (int i = 0; i < pair.size(); i++) {
            if (pair.get(i).getCoherenceCoefficient() > 0.5) {
                MainHighlighter.highlight(qualityComments.get(i),
                        "High coherence with comment", 0, WarningType.ERROR);
                highlightedComments.add(qualityComments.get(i));
            }
            if (pair.get(i).getCoherenceCoefficient() > 0.3 && pair.get(i).getCoherenceCoefficient() <= 0.5) {
                MainHighlighter.highlight(qualityComments.get(i),
                        "Medium coherence with comment", 0, WarningType.WARNING);
                highlightedComments.add(qualityComments.get(i));
            }
        }

        //2. quantitative comments <-> comments that target many statements
        int statementsBoundTogether = ConfigAccesser.getElement("max_statement_bound_together");
        int commentWordCount;
        int statementCount;
        SpecialStatementType specialStatement;// statements like for, lambda, anonymous class that put comment at special position/place
        for (int i = 0; i < quantityComments.size(); i++) {
            if (Checker.checkIfCommentedOutCode(quantityComments.get(i))) {
                MainHighlighter.highlight(quantityComments.get(i),
                        "Commented out code", 3, WarningType.ERROR);
                highlightedComments.add(quantityComments.get(i));
            }
            //highlighting of comment target without statements
            else if (Checker.checkIfNoTarget(quantityTargets.get(i))) {
                MainHighlighter.highlight(quantityComments.get(i),
                        "Comment has no target", 2, WarningType.ERROR);

                highlightedComments.add(quantityComments.get(i));
            } else {
                commentWordCount = Analyser.countWords(quantityComments.get(i));
                statementCount = Analyser.countStatements(quantityTargets.get(i));
                specialStatement = Analyser.specialStatement(quantityTargets.get(i));
                if ((commentWordCount <= WORD_COUNT_COEFFICIENT && statementCount >= statementsBoundTogether) ||
                        specialStatement != null) {
                    MainHighlighter.highlight(quantityComments.get(i),
                            "Comment describes complex block, should be extracted",
                            1, WarningType.WARNING);

                    highlightedComments.add(quantityComments.get(i));
                }
            }
        }
        CodeChangeListener.updateTable();
    }
}
