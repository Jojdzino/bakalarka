package edu.fiit.schneider_plugin.startup;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class PostStartupActivity implements com.intellij.openapi.startup.StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);
        psiManager.addPsiTreeChangeListener(new CodeChangeListener());
        if (!PropertiesComponent.getInstance(project).isValueSet("snake_case"))
            PropertiesComponent.getInstance(project).setValue("snake_case", 0, -1);
        if (!PropertiesComponent.getInstance(project).isValueSet("max_statement_bound_together"))
            PropertiesComponent.getInstance(project).setValue("max_statement_bound_together", 2, 0);
    }
}