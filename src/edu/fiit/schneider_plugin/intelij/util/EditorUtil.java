package edu.fiit.schneider_plugin.intelij.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

@SuppressWarnings("unused")
public class EditorUtil {

    public static int getLineOfElement(PsiElement element) {

        PsiFile containingFile = element.getContainingFile();
        Project project = containingFile.getProject();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(containingFile);
        int textOffset = element.getTextOffset();
        if (document != null && textOffset < element.getContainingFile().getTextLength()) {
            return document.getLineNumber(textOffset);
        }
        return -1;
    }

    public static int getLineOfElementWithOffset(PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        Project project = containingFile.getProject();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(containingFile);
        int textOffset = element.getTextOffset() + element.getText().length() - 1;
        if (document != null && textOffset < element.getContainingFile().getTextLength()) {
            return document.getLineNumber(textOffset);
        }
        return -1;
    }
}
