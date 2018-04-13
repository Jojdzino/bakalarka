package edu.fiit.schneider_plugin;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import edu.fiit.schneider_plugin.config.ConfigAccesser;
import edu.fiit.schneider_plugin.window.MyTableCellRenderer;
import edu.fiit.schneider_plugin.window.SelectionListener;
import edu.fiit.schneider_plugin.window.TableController;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CommentFixerFormMenu {

    private JPanel panel;
    private JTextField textField1;
    private JButton statementBoundTogetherButton;
    private JCheckBox snakeCaseCheckBox;
    private JTabbedPane tabbedPane1;
    private JTable editorToWarningTypeEntryTable;
    private JPanel editor;
    private JPanel config;
    private JButton refreshTableButton;
    private int statementsBoundTogether = 5;
    private TableColumn editorColumn = new TableColumn();
    private TableColumn errorColumn = new TableColumn();
    private TableColumn rowColumn = new TableColumn();
    private TableController tableController = new TableController();

    static {
    }

    {
        config.setMaximumSize(new Dimension(390, 170));
    }

    //sets current variable of comment length to user input and saves it into file
    public CommentFixerFormMenu() {
        addConfigListeners();
        initJTable();
        tabbedPane1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshTable();
            }
        });
    }


    private void refreshTable() {
        if (editorToWarningTypeEntryTable.getRowCount() != highlighterCount()) {
            tableController.clearTable(editorToWarningTypeEntryTable);
            tableController.updateTable(editorToWarningTypeEntryTable);
        }
    }

    private int highlighterCount() {
        int count = 0;
        for (Editor editor : EditorFactory.getInstance().getAllEditors())
            for (RangeHighlighter highlighter : editor.getMarkupModel().getAllHighlighters())
                if (highlighter.getLayer() == 3333)
                    count++;
        return count;
    }


    private void initJTable() {
        editorToWarningTypeEntryTable.setDefaultRenderer(Object.class, new MyTableCellRenderer());
        //editorToWarningTypeEntryTable.setModel(new MyTableModel());
        editorColumn.setHeaderValue("Editor");
        editorToWarningTypeEntryTable.addColumn(editorColumn);
        rowColumn.setHeaderValue("Row");
        editorToWarningTypeEntryTable.addColumn(rowColumn);
        errorColumn.setHeaderValue("Error type");
        editorToWarningTypeEntryTable.addColumn(errorColumn);
        editorToWarningTypeEntryTable.getSelectionModel().addListSelectionListener(
                new SelectionListener());
    }

    private void addConfigListeners() {
        statementBoundTogetherButton.addActionListener(e -> {
            int userInputStatementBoundTogether = Integer.parseInt(textField1.getText());
            if (userInputStatementBoundTogether < 1 || userInputStatementBoundTogether > 10) {
                statementsBoundTogether = 10;
            } else statementsBoundTogether = userInputStatementBoundTogether;
            ConfigAccesser.setElement(statementsBoundTogether, "max_statement_bound_together");
        });
        snakeCaseCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ConfigAccesser.setElement(1, "snake_case");
            } else {
                ConfigAccesser.setElement(0, "snake_case");
            }
        });
        refreshTableButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshTable();
            }
        });
    }

    public JPanel getPanel1() {
        return panel;
    }
}
