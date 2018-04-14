package edu.fiit.schneider_plugin.entity;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.impl.source.PsiModifierListImpl;
import com.intellij.psi.impl.source.PsiTypeElementImpl;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl;
import edu.fiit.schneider_plugin.algoritm.LevenshteinDistance;
import edu.fiit.schneider_plugin.algoritm.StanfordLemmatizer;
import edu.fiit.schneider_plugin.comment_util.Parser;
import edu.fiit.schneider_plugin.config.ConfigAccesser;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Class will be used only on comments that are targetting declaration of variable, method implementation or class declaration
 */
@SuppressWarnings({"FieldCanBeLocal"})
public class CommentTarget {
    @SuppressWarnings("WeakerAccess")
    public final static int COHORENCE_CONSTANT = 2;
    private final String mergedComment;
    //private String trimmedComment;
    private List<String> commentWordList = new ArrayList<>();
    private List<String> modifierList    = new ArrayList<>();
    private List<String> targetWordList  = new ArrayList<>();
    private List<String> lematisedList;
    private String currentString;//This is variable holding actual string of variable name. It will become longer
                                 // after it is appended with modifierList words like private static and so on
    private float coherenceCoeficient;

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

        this.lematisedList = StanfordLemmatizer.getSingleton().lemmatize(this.mergedComment);
        clearList();
        calculateCoherence();
    }

    /**
     * Calculates difference between every word from targetWordList and lematisedList with LevenshteinDistance.
     * Sets cohorenceCoeficient to proportion of similar words to word count of target
     */
    private void calculateCoherence() {

        //creating targetWordList, remove all short words that might break levenshtein
        this.targetWordList = Arrays.asList(currentString.split(" "));
        this.targetWordList = Parser.specialCheck(this.targetWordList);
        List<String> newTargets = new LinkedList<>();
        for(String str:targetWordList)
            if(str.length()>2)newTargets.add((str.toLowerCase()));
        this.targetWordList=newTargets;

        int wordSimilarityCounter = 0;
        //n*n, but will skip short words ->no syntactical benefit to coherence, but might increment counter
        //SPECIAL short words can break levenshtein like "to" and "hi", they mean different but short to work with
        for (String currentTarget : targetWordList) {
            if(currentTarget.length()<=2)continue;
            for (String currentCommentLemma : lematisedList) {
                if(currentCommentLemma.length()<=2)continue;
                int distance = LevenshteinDistance.computeLevenshteinDistance(currentTarget.toLowerCase(), currentCommentLemma.toLowerCase());
                if ( distance<= COHORENCE_CONSTANT)
                    wordSimilarityCounter++;
            }
        }
        int lematisedLength = 0;
        for (String str : lematisedList)
            if (str.length() > 2)
                lematisedLength++;
        this.coherenceCoeficient = (float) wordSimilarityCounter / (float) lematisedLength;
    }

    private void getClassText(List<PsiElement> targets) {
        PsiElement main=null;
        StringBuilder builder = new StringBuilder();
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
                        builder.append(arrToString(Parser.camelCaseSplitter(actual.getText())));
                    else builder.append(arrToString(Parser.snakeCaseSplitter(actual.getText())));
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
        StringBuilder builder = new StringBuilder();

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
                    builder.append(arrToString(Parser.camelCaseSplitter(actual.getText())));
                else builder.append(arrToString(Parser.snakeCaseSplitter(actual.getText())));
            }
        }
        this.currentString =builder.toString();
    }

    private void getVariableText(List<PsiElement> targets) {
        PsiElement main=targets.get(0).getParent();
        StringBuilder builder = new StringBuilder();
        PsiElement[] array= main.getChildren();
        PsiElement[] newArray = null;
        for (PsiElement element : array) {
            if (element.getClass() == PsiLocalVariableImpl.class) {
                newArray = element.getChildren();
            }
        }
        if (newArray != null)
            array = newArray;
        for(int i = 0; i < array.length-1;i++) {
            PsiElement actual = array[i];
            if (actual.getClass() == PsiModifierListImpl.class)
                modifierList.addAll(Arrays.asList(actual.getText().split(" ")));
            if(actual.getClass() == PsiTypeElementImpl.class)
                modifierList.addAll(Arrays.asList(actual.getText().split("[^A-Za-z0-9]")));//Split by non alphanumeric
            if(actual.getClass() == PsiIdentifierImpl.class){
                if(ConfigAccesser.getElement("snake_case")==0)
                    builder.append(arrToString(Parser.camelCaseSplitter(actual.getText())));
                else builder.append(arrToString(Parser.snakeCaseSplitter(actual.getText())));
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

    /**
     * Clears list of string that dont contain alphanumerics - especially strings like "/" and "*"
     */
    private void clearList(){
        Iterator iterator=lematisedList.listIterator();
        while(iterator.hasNext()){
            String current = (String) iterator.next();
            Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
            if(pattern.matcher(current).find())
                iterator.remove();
        }
    }

    //------------------------GETTERS------------------------


    public float getCoherenceCoefficient() {
        return coherenceCoeficient;
    }
}
