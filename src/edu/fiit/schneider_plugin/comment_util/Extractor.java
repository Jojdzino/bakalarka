package edu.fiit.schneider_plugin.comment_util;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
import com.intellij.psi.impl.source.tree.java.PsiForStatementImpl;
import com.intellij.psi.impl.source.tree.java.PsiForeachStatementImpl;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
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
        System.out.println();
        return allComments;
    }

    @SuppressWarnings("WeakerAccess")
    public static PsiElement extractLastElement(PsiElement root){
        PsiElement actualElement = root;

        while(actualElement.getNextSibling()!=null){
            actualElement=actualElement.getNextSibling();
        }
        if(actualElement.getChildren().length!=0)
            return extractLastElement(actualElement.getLastChild());
        else return actualElement;
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

        //TODO mozno nie pre triedu, lebo to mi oznaci celu classu
        //If comment describes method implementation
        //If comment describes class declaration
        if(psiComment.getParent().getClass()== PsiMethodImpl.class ||
                psiComment.getParent().getClass() ==PsiClassImpl.class) {
            targetElements.add(psiComment.getParent());
            return targetElements;
        }

        //Sets variable to make program look for only one element - Class
        //TODO this might be unstable check for various types of java file content
        //TODO like multiple classes in one file and classes with annotations etc...
        if(psiComment.getParent().getClass() == PsiJavaFileImpl.class){
            actualMaxStatementBoundTogether=1;
        }

        while (realElementsCounter != actualMaxStatementBoundTogether) {
            if(actualElement==null)
                break;
            //There is new line, but not before there was a token or another code
            if (actualElement.getClass() == PsiWhiteSpaceImpl.class && actualElement.getText().matches(".*\n.*\n.*") &&
                    (realElementsCounter != 0 || tokenCounter != 0))
                break;

            if (actualElement.getClass() != PsiWhiteSpaceImpl.class && actualElement.getClass() != PsiJavaTokenImpl.class)
                realElementsCounter++;

            if (actualElement.getClass() == PsiJavaTokenImpl.class)
                tokenCounter++;

            targetElements.add(actualElement);

            Class actualElementClass = actualElement.getClass();
            //SPECIAL when targetting reaches for within counter it will  break - for is signifficant for code processing
            if (actualElementClass == PsiForeachStatementImpl.class || actualElementClass == PsiForStatementImpl.class)
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
            if (actual.getClass() == PsiClassImpl.class) return 1;
            if (actual.getClass() == PsiMethodImpl.class) return 2;
            if (actual.getClass() == PsiFieldImpl.class ||
                    actual.getParent().getClass() == PsiFieldImpl.class) return 3;
        }

        return -1;
    }



}
