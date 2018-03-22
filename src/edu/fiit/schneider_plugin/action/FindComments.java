package edu.fiit.schneider_plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import edu.fiit.schneider_plugin.comment_util.Extractor;
import edu.fiit.schneider_plugin.comment_util.Transformer;
import edu.fiit.schneider_plugin.entity.CommentTarget;
import edu.fiit.schneider_plugin.highlighters.MainHighlighter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FindComments extends AnAction {

    //Need a hashmap to let me find with a comment group of psicomments. If this group of comments is in hashmap
    // i can find
    private static HashMap<PsiComment, List<PsiComment>> highlightedGroupsOfComments = new HashMap<>();

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        //Open psifile by given project and editor
        //PsiManager manager = PsiManager.getInstance(project); //maybe needed later, code stays to help me not forget
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);


        List<PsiComment> allComments = Extractor.extractCommentsFromPsiFile(psiFile);   //get list of all comments from file
        if(allComments.size()==0)
            return;

        List<List<PsiComment>> mergedComments= Transformer.mergeByPosition(allComments);

        List<List<PsiElement>> commentTargets = new ArrayList<>();// SPECIAL might contain array and linked lists, because one method insert at index 1 - better linked list
        for(List<PsiComment> actualList : mergedComments){
            commentTargets.add(Extractor.extractTargets(actualList.get(actualList.size()-1)));
        }

        //---------------------EXTRACTION COMPLETED------------------------ -> SPECIFICATION OF COMMENTS

        int counter = 0;
        List<List<PsiComment>> qualityComments  = new ArrayList<>();//Used to store comments that target method, class or variable decl.
        List<List<PsiComment>> quantityComments = new ArrayList<>();//Used to store all other comments
        List<List<PsiElement>> qualityTargets   = new ArrayList<>();
        List<List<PsiElement>> quantityTargets  = new ArrayList<>();
        List<Integer> resultSpecificationList   = new ArrayList<>();// Contains result for each list of comments
        List<CommentTarget> pair                = new ArrayList<>();

        for(List<PsiElement> list:commentTargets){
            int result = Extractor.targetSpecifier(list);
            if(result >=1 && result <=3){
                resultSpecificationList.add(result);
                qualityComments.add(mergedComments.get(counter));
                qualityTargets.add(list);
                pair.add(new CommentTarget(mergedComments.get(counter),list,result));
            }
            else {
                quantityComments.add(mergedComments.get(counter));
                quantityTargets.add(list);
            }
            counter++;
        }
        System.out.println();

        //zafarbenie podla koherencie
        for(int i = 0;i<pair.size();i++){
            if(i<=0)continue;
            if(pair.get(i).getCoherenceCoeficient()>0.5){
                //3 types of comments
                MainHighlighter.getInstance().highlight(qualityTargets.get(i),"High coherence with target");
                MainHighlighter.getInstance().highlight(qualityComments.get(i),"High coherence with comment");
            }
        }

        //zafarbenie ak je komentar neproporcionalne dlhy ku svojemu targetu to este porozmyslat ako napr ked ma comment 2 az 7 slov a odkazuje sa na viac ako 5 statementov
    }

    public static HashMap<PsiComment, List<PsiComment>> getHighlightedGroupsOfComments() {
        return highlightedGroupsOfComments;
    }
}
