package edu.fiit.schneider_plugin.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import edu.fiit.schneider_plugin.CommentFixerFormMenu;
import org.jetbrains.annotations.NotNull;

public class Window implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        CommentFixerFormMenu menu = new CommentFixerFormMenu();
        toolWindow.getComponent().add(new CommentFixerFormMenu().getPanel1());
        //toolWindow.getComponent().add()
    }
}
