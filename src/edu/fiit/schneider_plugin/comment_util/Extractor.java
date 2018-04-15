package edu.fiit.schneider_plugin.comment_util;


import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import edu.fiit.schneider_plugin.config.ConfigAccesser;

import java.util.*;


public class Extractor {

    private static short maxStatementsBoundTogether=(short) ConfigAccesser.getElement("max_statement_bound_together");
    /**
     * Gets all comments from given psifile
     *
     * @param psiFile psiFile to search from
     */
    public static List<PsiComment> extractCommentsFromPsiFile(PsiFile psiFile) {

        List<Collection<? extends PsiComment>> psiFileNodesCollections = new ArrayList<>();
        Class<? extends PsiComment> list = PsiComment.class;
        psiFileNodesCollections.add(PsiTreeUtil.findChildrenOfType(psiFile.getFirstChild().getParent(),list));

        List<PsiComment> allComments;
        allComments = Transformer.turnToList(Transformer.flatten(psiFileNodesCollections));
        return allComments;
    }

    /**
     * Extracts target for list of comments.
     * @param psiComment last from list of comments to start looking from
     * @return list of PsiElements that are target of given PsiComment
     */
    public static List<PsiElement> extractTargets(PsiComment psiComment) {
        if (Checker.checkIfCodeInLine(psiComment))
            return Extractor.extractTargetFromBeforeCommentByParent(psiComment);

        int actualMaxStatementBoundTogether=maxStatementsBoundTogether;
        int realElementsCounter = 0; // User doesnt count for elements like 'SEMICOLON' and 'WHITESPACE'
        int tokenCounter = 0;
        PsiElement actualElement = psiComment.getNextSibling();
        List<PsiElement> targetElements = new ArrayList<>();


        //Comment that describes class or method is always before its declaration, in PSI its always child of class or method
        if (psiComment.getParent() instanceof PsiMethod || psiComment.getParent() instanceof PsiClass) {
            targetElements.add(psiComment.getParent());
            return targetElements;
        }

        //Sets variable to make program look for only one element - Class
        if (psiComment.getParent() instanceof PsiFile)
            actualMaxStatementBoundTogether=1;


        while (realElementsCounter != actualMaxStatementBoundTogether) {
            if(actualElement==null)
                break;

            //There is new line, but not before there was a token or another code
            if (actualElement instanceof PsiWhiteSpace && actualElement.getText().matches(".*\n.*\n.*") &&
                    (realElementsCounter != 0 || tokenCounter != 0))
                break;

            if (actualElement instanceof PsiWhiteSpace && !(actualElement instanceof PsiJavaToken))
                realElementsCounter++;

            if (actualElement instanceof PsiJavaToken)
                tokenCounter++;

            targetElements.add(actualElement);

            //When type of for is in targets -> break because for always does some action that ends with some result
            if (actualElement instanceof PsiForeachStatement || actualElement instanceof PsiForStatement ||
                    actualElement instanceof PsiWhileStatement)
                break;

            actualElement = actualElement.getNextSibling();
        }

        return targetElements;
    }

    // I can return all prev elements till null, because i have confirmed that there is code in the same line
    private static List<PsiElement> extractTargetFromBeforeCommentByParent(PsiComment psiComment){
        List<PsiElement> codeElements = new LinkedList<>();
        PsiElement[] array = psiComment.getParent().getChildren();
        codeElements.addAll(Arrays.asList(array).subList(0, array.length - 1));
        return codeElements;
     }

    // Treats to special cases like for() \n;//comment
    private static List<PsiElement> extractSpecialTarget(List<PsiElement> codeElements, PsiComment psiComment) {
        PsiElement parentsParent = psiComment.getParent().getParent();
        List<PsiElement> returnList = new ArrayList<>();

        //extractien except last one, because it contains psiComment and elements that are already in codeElements
        returnList.addAll(Arrays.asList(parentsParent.getChildren()).subList(0, parentsParent.getChildren().length - 2));

        returnList.addAll(codeElements);

        return returnList;
    }

    /**
     * Identifies target based on list of targets
     * @return -1 if target is quantity, 1 for class, 2 for method, 3 for variable
     */
    public static int targetSpecifier(List<PsiElement> targets) {

        for (PsiElement actual : targets) {
            if (actual instanceof PsiClass) return 1;
            if (actual instanceof PsiMethod) return 2;
            if (actual instanceof PsiField ||
                    actual.getParent() instanceof PsiField) return 3;
            if (actual instanceof PsiLocalVariable)
                return 3;
        }

        return -1;
    }



}
