package edu.fiit.schneider_plugin.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiComment;
import edu.fiit.schneider_plugin.action.FindComments;
import edu.fiit.schneider_plugin.config.ConfigAccesser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class CommentFixerFormMenu {

    private JTextField textField;
    private JButton commentLengthButton;
    private JPanel panel;
    private JTextField textField1;
    private JButton statementBoundTogetherButton;
    private JCheckBox snake_caseCheckBox;
    private JTabbedPane tabbedPane1;
    private JTable editorToWarningTypeEntryTable;
    private int length = 30;
    private static Project project;
    private int statementsBoundTogether = 5;
    private TableColumn editorColumn = new TableColumn();
    private TableColumn errorColumn = new TableColumn();


    //Initializes project to get current project
    static {
        project = ProjectManager.getInstance().getOpenProjects()[0];
    }

    {
        panel.setMaximumSize(new Dimension(390, 170));
    }

    //sets current variable of comment length to user input and saves it into file
    CommentFixerFormMenu() {
        commentLengthButton.addActionListener(e -> {
            checkIfConfigMissing();
            int userInputCommentValue = Integer.parseInt(textField.getText());
            if (userInputCommentValue < 3 || userInputCommentValue > 100) {
                length = 30;
            } else
                length = userInputCommentValue;
            try {
                ConfigAccesser.setElement(length, "comment_length");
            } catch (IOException | JDOMException e1) {
                e1.printStackTrace();
            }
        });
        statementBoundTogetherButton.addActionListener(e -> {
            checkIfConfigMissing();
            int userInputStatementBoundTogether = Integer.parseInt(textField1.getText());
            if (userInputStatementBoundTogether < 1 || userInputStatementBoundTogether > 10) {
                statementsBoundTogether = 10;
            } else statementsBoundTogether = userInputStatementBoundTogether;
            try {
                ConfigAccesser.setElement(statementsBoundTogether, "max_statement_bound_together");
            } catch (IOException | JDOMException e1) {
                e1.printStackTrace();
            }
        });
        snake_caseCheckBox.addItemListener(e -> {
            try {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    ConfigAccesser.setElement(1, "snake_case");
                } else {
                    ConfigAccesser.setElement(0, "snake_case");
                }
            } catch (JDOMException | IOException el) {
                el.printStackTrace();
            }
        });
        initJTable();
        tabbedPane1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                List<List<PsiComment>> groupsOfComments = FindComments.getHighlightedComments();//update table
                DefaultTableModel dm = (DefaultTableModel) editorToWarningTypeEntryTable.getModel();
                int rowCount = dm.getRowCount();
                for (int i = rowCount - 1; i >= 0; i--) {
                    dm.removeRow(i);
                }

            }
        });
    }

    private void initJTable() {
        editorColumn.setHeaderValue("Editor");
        editorToWarningTypeEntryTable.addColumn(editorColumn);
        errorColumn.setHeaderValue("Error type");
        editorToWarningTypeEntryTable.addColumn(errorColumn);
        editorToWarningTypeEntryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }
        });
    }


    // Checks if file is missing, if yes thats ok, if not reads it and sets values
    private void checkIfConfigMissing() {
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File(project.getBaseDir().getPath() + "/.idea/commentFixerConfig.xml");
        if (!xmlFile.exists())
            return;
        try {
            Document document = builder.build(xmlFile);
            Element rootNode = document.getRootElement();
            List<Element> nodeList = rootNode.getChildren("CONFIGURATION");

            for (Element aNodeList : nodeList) {

                length = Integer.parseInt(aNodeList.getChildText("comment_length"));
                //add other values to read
                statementsBoundTogether = Integer.parseInt(aNodeList.getChildText("max_statement_bound_together"));
            }

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }


    JPanel getPanel1() {
        return panel;
    }

}
