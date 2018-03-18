package edu.fiit.schneider_plugin.comment_util;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.*;

public class Extractor {
    /**
     * Gets all comments from given psifile
     *
     * @param psiFile psiFile to search from
     */
    public static List<PsiComment> getCommentsFromPsiFile(PsiFile psiFile) {

        List<Collection<? extends PsiComment>> psiFileNodesCollections = new ArrayList<>();
        List<PsiComment> elementList = new LinkedList<>();
        Class<? extends PsiComment> list = PsiComment.class;
        psiFileNodesCollections.add(PsiTreeUtil.findChildrenOfType(psiFile.getFirstChild().getParent(),list));

        List<PsiComment> allComments;
        allComments = turnToList(flatten(psiFileNodesCollections));
        System.out.println();
        return allComments;
    }


    /**
     * Transforms list of collections into one collection
     * @param psiFileNodesCollections
     * @return one collection
     */
    private static Collection<? extends PsiComment> flatten(List<Collection<? extends PsiComment>> psiFileNodesCollections) {
        Collection<PsiComment> newCollection = new ArrayList<>();
        for (Collection<? extends PsiComment> collection : psiFileNodesCollections) {
            newCollection.addAll(collection);
        }
        return newCollection;
    }

    /**
     * Transforms collection into more usable list of comment
     * @param collection
     * @return list of PsiComments
     */
    private static List<PsiComment> turnToList(Collection<? extends PsiComment> collection){
        return new LinkedList<>(collection);
    }

    public static List<PsiElement> getTarget(PsiComment startingComment){
        return null;
    }

    public static List<PsiElement> getSingleLineTarget(PsiComment startingComment){
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public static PsiElement getLastElement(PsiElement root){
        PsiElement actualElement = root;

        while(actualElement.getNextSibling()!=null){
            actualElement=actualElement.getNextSibling();
        }
        if(actualElement.getChildren().length!=0)
            return getLastElement(actualElement.getLastChild());
        else return actualElement;
    }

    /**
     * Extracts target for list of comments.
     * @param psiComment last from list of comments to start looking from
     * @return list of PsiElements that are target of given PsiComment
     */
    public static List<PsiElement> extractTargets(PsiComment psiComment) {

        if(Checker.checkIfCodeInLine(psiComment))
            return Extractor.extractTargetFromBeforeComment(psiComment);

        return null;
    }

    // I can return all prev elements till null, because i have confirmed that there is code in the same line
    private static List<PsiElement> extractTargetFromBeforeComment(PsiComment psiComment) {
        List<PsiElement> codeElements = new LinkedList<>();
        PsiElement actual = psiComment.getPrevSibling();

        while(actual!=null){
            codeElements.add(0,actual);
            actual=actual.getPrevSibling();
        }
        if(!containsCode(codeElements))
            return getSpecialTarget(codeElements,psiComment);
        return codeElements;
    }

    // Treats to special cases like for() \n;//comment
    private static List<PsiElement> getSpecialTarget(List<PsiElement> codeElements, PsiComment psiComment) {
        PsiElement parentsParent = psiComment.getParent().getParent();
        List<PsiElement> returnList = new ArrayList<>();

        //extractien except last one, because it contains psiComment and elements that are already in codeElements
        returnList.addAll(Arrays.asList(parentsParent.getChildren()).subList(0, parentsParent.getChildren().length - 2));

        returnList.addAll(codeElements);

        return returnList;
    }

    private static boolean containsCode(List<PsiElement> codeElements) {
        for (PsiElement element : codeElements) {
            Class c = element.getClass();
            //noinspection StatementWithEmptyBody
            if (c == PsiJavaTokenImpl.class || c== PsiWhiteSpaceImpl.class) {
            }
            else return true;
        }
        return false;
    }


}
