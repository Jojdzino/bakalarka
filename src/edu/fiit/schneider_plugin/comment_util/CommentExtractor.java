package edu.fiit.schneider_plugin.comment_util;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CommentExtractor {
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

    private static Collection<? extends PsiComment> flatten(List<Collection<? extends PsiComment>> psiFileNodesCollections) {
        Collection<PsiComment> newCollection = new ArrayList<>();
        for (Collection<? extends PsiComment> collection : psiFileNodesCollections) {
            newCollection.addAll(collection);
        }
        return newCollection;
    }

    private static List<PsiComment> turnToList(Collection<? extends PsiComment> collection){
        return new LinkedList<>(collection);
    }
}
