package edu.fiit.schneider_plugin.config;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

public class ConfigAccesser {
    private static Project project;
    static {
        project = ProjectManager.getInstance().getOpenProjects()[0];
    }

    //Sets persistent data to value
    public static void setElement(int value, String id) {
        PropertiesComponent.getInstance(project).setValue(id, value, -1);
    }

    //Returns value of selected element or -1 if element is missing
    public static int getElement(String id) {
        if (PropertiesComponent.getInstance(project).getValue(id) != null)
            //noinspection ConstantConditions
            return Integer.parseInt(PropertiesComponent.getInstance(project).getValue(id));
        else return -1;
    }
}
