package edu.fiit.schneider_plugin.comment_util;

import com.intellij.psi.*;
import edu.fiit.schneider_plugin.entity.enums.SpecialStatementType;

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

    public static SpecialStatementType specialStatement(List<PsiElement> psiElements) {
        for (PsiElement element : psiElements) {
            if (element instanceof PsiLambdaExpression)
                return SpecialStatementType.LAMBDA;
            if (element instanceof PsiForStatement || element instanceof PsiForeachStatement)
                return SpecialStatementType.FOR;
            if (element instanceof PsiAnonymousClass)
                return SpecialStatementType.ANONYMOUS;
        }
        return null;
    }
}
