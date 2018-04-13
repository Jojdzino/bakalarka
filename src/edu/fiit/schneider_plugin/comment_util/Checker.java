package edu.fiit.schneider_plugin.comment_util;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.PsiCommentImpl;
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl;

import java.util.List;

public class Checker {

    /**
     * Checks if second parameter is neighbor of root without any code between them
     *
     * @param root       - comment to search from
     * @param psiComment - possible neighbor or root
     * @return true, if psiComment is neighbor or root without any code between them, else false
     */
    static boolean checkIfNeighbors(PsiComment root, PsiComment psiComment) {
        if (root == null || psiComment == null) return false;

        PsiElement actualElement = root.getNextSibling();

        while (actualElement != null) {
            if (actualElement.getClass() == PsiCommentImpl.class && actualElement == psiComment)
                return true;
            if (actualElement.getClass() != PsiCommentImpl.class && actualElement.getClass() != PsiWhiteSpaceImpl.class)
                return false;
            actualElement = actualElement.getNextSibling();
        }
        return false;
    }

    /**
     * Checks if there is source code in the same line + some special cases where comments are in new line but
     * should be considered as comments for previous line
     *
     * @param root - source element to look if there is comment before
     * @return true if code was found in the same line, false otherwise
     */
    @SuppressWarnings({"ConstantConditions", "RedundantIfStatement"})
    static boolean checkIfCodeInLine(PsiComment root) {

        PsiElement prevSibling = root.getPrevSibling();
        boolean semicolonInSameLine = false;
        boolean newLine = false;
        if (prevSibling == null)
            return false;

        while (true) {

            // Special cases - if there is a declaration of variable ; is not closest prevSibling
            // ( declaration of variable but ; is in new line)
            if (prevSibling.getClass() == PsiLocalVariableImpl.class)
                return true;

            //Normal cases
            if (prevSibling.getClass() == PsiWhiteSpaceImpl.class && prevSibling.getText().contains("\n"))
                newLine = true;

            if (prevSibling.getClass() == PsiJavaTokenImpl.class && prevSibling.toString().endsWith("SEMICOLON"))
                semicolonInSameLine = true;

            prevSibling = prevSibling.getPrevSibling();
            if (newLine && !semicolonInSameLine)
                return false;
            if (!newLine && semicolonInSameLine)
                return true;
            if (prevSibling == null)
                break;
        }

        if (newLine && !semicolonInSameLine)
            return false;
        if (!newLine && semicolonInSameLine)
            return true;

        return false;
    }

    public static boolean checkIfNoTarget(List<PsiElement> elements) {
        for (PsiElement element : elements)
            if (!(element instanceof PsiWhiteSpace) && !(element instanceof PsiJavaToken))
                return false;

        return true;
    }

    //regex is not necessary, because it only checks if comment contains ';'->easier with contains
    public static boolean checkIfCommentedOutCode(List<PsiComment> psiComments) {

        for (PsiComment comment : psiComments) {
            if (comment.getText().contains(";"))
                return true;
        }
        return false;
    }
}
