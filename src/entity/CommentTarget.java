package entity;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.impl.source.PsiModifierListImpl;
import com.intellij.psi.impl.source.PsiTypeElementImpl;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import edu.fiit.schneider_plugin.comment_util.RegExParser;
import edu.fiit.schneider_plugin.config.ConfigAccesser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class will be used only on comments that are targetting declaration of variable, method implementation or class declaration
 */
@SuppressWarnings({"unused", "FieldCanBeLocal", "Duplicates"})
public class CommentTarget {
    private final String mergedComment;
    private List<String> commentWordList = new ArrayList<>();
    private List<String> modifierList    = new ArrayList<>();
    private List<String> targetWordList  = new ArrayList<>();
    private String currentString;

    public CommentTarget(List<PsiComment> comments, List<PsiElement> targets, int result) {
        //Prechadzaj komentare a vytvor string z ich slov
        // prechadzaj targety a ked najdes metodu alebo triedu alebo variable tak vytvor z nich slova
        StringBuilder builder = new StringBuilder();
        for(PsiElement comment: comments){
            builder.append(comment.getText());
        }
        this.mergedComment = builder.toString();
        builder.setLength(0);
        if(result == 1)
            getClassText(targets);
        if(result == 2)
            getMethodText(targets);
        if(result == 3)
            getVariableText(targets);
    }

    private void getClassText(List<PsiElement> targets) {
        PsiElement main=null;
        PsiElement help;
        StringBuilder builder = new StringBuilder();
        String className;
        for(PsiElement actual: targets) {
            if (actual.getClass() == PsiClassImpl.class) {
                main = actual;
                break;
            }
        }
        if (main != null) {
            for (PsiElement actual : main.getChildren()) {
                if(actual.getClass()==PsiModifierListImpl.class) {
                    modifierList.addAll(Arrays.asList(actual.getText().split(" ")));//Automatic adding of class string ( i know it will be there)
                    modifierList.add("class");
                }
                if (actual.getClass() == PsiIdentifierImpl.class) {
                    if (ConfigAccesser.getElement("snake_case") == 0)
                        builder.append(arrToString(RegExParser.camelCaseSplitter(actual.getText())));
                    else builder.append(arrToString(RegExParser.snakeCaseSplitter(actual.getText())));
                }
            }
        }
        this.currentString =builder.toString();
    }


    private void getMethodText(List<PsiElement> targets) {
        //Get modifier list first PsiModifierList
        //Then get return value PsiTypeElement
        //PsiIdentifier - meno
        //Argumenty asi zatial nebudem pridavat
        PsiElement main=null;
        PsiElement help;
        StringBuilder builder = new StringBuilder();
        String methodName;

        for(PsiElement actual: targets) {
            if (actual.getClass() == PsiMethodImpl.class) {
                main = actual;
                break;
            }
        }

        assert main != null;
        PsiElement[] elArr= main.getChildren();

        for(PsiElement actual :elArr ) {
            if (actual.getClass() == PsiModifierListImpl.class)
                modifierList.addAll(Arrays.asList(actual.getText().split(" ")));
            if(actual.getClass() == PsiTypeElementImpl.class)
                modifierList.addAll(Arrays.asList(actual.getText().split("[^A-Za-z0-9]")));//Split by non alphanumeric
            if(actual.getClass() == PsiIdentifierImpl.class){
                if(ConfigAccesser.getElement("snake_case")==0)
                    builder.append(arrToString(RegExParser.camelCaseSplitter(actual.getText())));
                else builder.append(arrToString(RegExParser.snakeCaseSplitter(actual.getText())));
            }
        }
        this.currentString =builder.toString();
    }

    private void getVariableText(List<PsiElement> targets) {
        PsiElement main=targets.get(0).getParent();
        PsiElement help;
        StringBuilder builder = new StringBuilder();
        String methodName;

        PsiElement[] array= main.getChildren();

        for(int i = 0; i < array.length-1;i++) {
            PsiElement actual = array[i];
            if (actual.getClass() == PsiModifierListImpl.class)
                modifierList.addAll(Arrays.asList(actual.getText().split(" ")));
            if(actual.getClass() == PsiTypeElementImpl.class)
                modifierList.addAll(Arrays.asList(actual.getText().split("[^A-Za-z0-9]")));//Split by non alphanumeric
            if(actual.getClass() == PsiIdentifierImpl.class){
                if(ConfigAccesser.getElement("snake_case")==0)
                    builder.append(arrToString(RegExParser.camelCaseSplitter(actual.getText())));
                else builder.append(arrToString(RegExParser.snakeCaseSplitter(actual.getText())));
            }
        }
        this.currentString =builder.toString();
    }

    //I didnt like Arrays.toString, this will make string from array of strings without []
    private String arrToString(String[] strings) {
        StringBuilder b = new StringBuilder();
        b.append(" ");
        for (String x : strings)
            b.append(x).append(" ");

        return b.toString();
    }

}
