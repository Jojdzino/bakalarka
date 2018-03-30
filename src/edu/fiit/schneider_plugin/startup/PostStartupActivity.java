package edu.fiit.schneider_plugin.startup;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import edu.fiit.schneider_plugin.CodeChangeListener;
import org.jetbrains.annotations.NotNull;

public class PostStartupActivity implements com.intellij.openapi.startup.StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);
        psiManager.addPsiTreeChangeListener(new CodeChangeListener());
    }
}