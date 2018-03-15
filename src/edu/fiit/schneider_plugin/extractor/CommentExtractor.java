package edu.fiit.schneider_plugin.extractor;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommentExtractor {
    /**
     * Gets all comments from given psifile
     *
     * @param psiFile                 psiFile to search from
     * @param psiFileNodesCollections list of collections of comments from differenc parts of file
     */
    public static Collection<? extends PsiComment> getCommentsFromPsiFile(PsiFile psiFile,
                                                                          List<Collection<? extends PsiComment>> psiFileNodesCollections) {

        Class<? extends PsiComment> list = PsiComment.class;
        for (PsiElement element : psiFile.getChildren()) {
            psiFileNodesCollections.add(PsiTreeUtil.findChildrenOfType(element, list));
        }

        Collection<? extends PsiComment> allComments;
        allComments = flatten(psiFileNodesCollections);
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
}
