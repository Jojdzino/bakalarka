package edu.fiit.schneider_plugin.comment_util;

import com.intellij.psi.PsiComment;

import java.util.LinkedList;
import java.util.List;



public class Merger {

    /**
     * Puts together comments that are close nearby, next to each other etc. One line comments with code in the same line
     * are left alone. All other comments that are " one big comment", like //comment\n//comment are put together into
     * one list.
     *
     * @param allComments comments from single file
     * @return list of together listed comments
     */
    public static List<List<PsiComment>> mergeByPosition(List<PsiComment> allComments) {

        List<List<PsiComment>> mergedCommentLists = new LinkedList<>();

        /*
        Set of rules:
        if comment is END_OF_LINE_COMMENT do:
            loop over previous siblings, and check if there is any code(semicolon is enough) before new line appears -> indication that this comment
            is in the same line with code -> this comment is single within its list

            if there is no code in previous line go to next sibling, if it is comment add add it into list
         */


        for(int iter = 0;iter<allComments.size();iter++){
            PsiComment actualComment = allComments.get(iter);

            if (Checker.checkIfCodeInLine(actualComment)) {
                List<PsiComment> newList= new LinkedList<>();
                newList.add(actualComment);
                mergedCommentLists.add(newList);
            }
            else{
                List<PsiComment> newList = new LinkedList<>();
                newList.add(actualComment);
                while(iter +1 != allComments.size() && Checker.checkIfNeighbors(actualComment,allComments.get(iter+1))){
                    newList.add(allComments.get(iter+1));
                    iter++;
                }
                mergedCommentLists.add(newList);
            }
        }

        return mergedCommentLists;
    }


}