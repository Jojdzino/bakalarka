package edu.fiit.schneider_plugin.startup;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import edu.fiit.schneider_plugin.CodeChangeListener;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class PostStartupActivity implements com.intellij.openapi.startup.StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);
        psiManager.addPsiTreeChangeListener(new CodeChangeListener());

        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File(project.getBaseDir().getPath() + "/.idea/commentFixerConfig.xml");
        if (xmlFile.exists())
            return;
        try {
            Document document = builder.build(xmlFile);
            Element rootNode = document.setRootElement(new Element("CONFIGURATION")).getRootElement();
            rootNode.addContent(new Element("max_statement_bound_together").setText("2"));
            rootNode.addContent(new Element("snake_case").setText("0"));

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

    }
}