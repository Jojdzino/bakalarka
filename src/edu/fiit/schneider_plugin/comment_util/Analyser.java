package edu.fiit.schneider_plugin.comment_util;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiWhiteSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Analyser {
    public static int countWords(List<PsiComment> psiComments) {
        List<String> commentStringCorrected = new ArrayList<>();
        for (PsiComment comment : psiComments) {
            List<String> commentString = Arrays.asList(comment.getText().split(" "));
            for (String str : commentString) {
                if (str.length() > 2 && !str.matches("/")) {
                    commentStringCorrected.add(str);
                }
            }
        }
        return commentStringCorrected.size();
    }

    public static int countStatements(List<PsiElement> psiElements) {
        int counter = 0;
        for (PsiElement element : psiElements) {
            if (!(element instanceof PsiWhiteSpace) && !(element instanceof PsiJavaToken))
                counter++;
        }
        return counter;
    }
}
