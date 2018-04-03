package edu.fiit.schneider_plugin.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import edu.fiit.schneider_plugin.config.ConfigAccesser;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.swing.*;
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
    private JPanel editor;
    private JPanel config;
    private int length = 30;
    private static Project project;
    private int statementsBoundTogether = 5;
    private TableColumn editorColumn = new TableColumn();
    private TableColumn errorColumn = new TableColumn();
    private TableColumn rowColumn = new TableColumn();
    private TableController tableController = new TableController();

    static {
        project = ProjectManager.getInstance().getOpenProjects()[0];
    }

    {
        config.setMaximumSize(new Dimension(390, 170));
    }

    //sets current variable of comment length to user input and saves it into file
    CommentFixerFormMenu() {
        addConfigListeners();
        initJTable();
        tabbedPane1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableController.clearTable(editorToWarningTypeEntryTable);
                tableController.updateTable(editorToWarningTypeEntryTable);
            }
        });
    }


    private void initJTable() {
        editorToWarningTypeEntryTable.setDefaultRenderer(Object.class, new MyTableCellRender());
        //editorToWarningTypeEntryTable.setModel(new MyTableModel());
        editorColumn.setHeaderValue("Editor");
        editorToWarningTypeEntryTable.addColumn(editorColumn);
        rowColumn.setHeaderValue("Row");
        editorToWarningTypeEntryTable.addColumn(rowColumn);
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

    private void addConfigListeners() {
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
    }

    JPanel getPanel1() {
        return panel;
    }

}
