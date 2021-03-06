package edu.fiit.schneider_plugin.window;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.markup.RangeHighlighter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
class TableController {

    private static List<Editor> editorList = null;
    private static List<RangeHighlighter> rangeHighlighterList = null;//editors that

    void clearTable(JTable table) {
        DefaultTableModel dm = (DefaultTableModel) table.getModel();
        int rowCount = dm.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            dm.removeRow(i);
        }
    }

    static List<Editor> getEditorList() {
        return editorList;
    }

    private void addIntoTable(List<Object> objectList, int rowCounter, MyTableModel model) {
        Color color = (Color) objectList.get(3);
        model.setRowColour(rowCounter, color);
        Vector<Object> arr = new Vector<>();
        arr.add(objectList.get(0));
        arr.add(objectList.get(1));
        arr.add(objectList.get(2));
        model.addRow(arr);
    }

    private Vector createColumnVector() {
        Vector<Object> columnNames = new Vector<>();
        columnNames.add("Document");
        columnNames.add("Line");
        columnNames.add("Problem");
        return columnNames;
    }

    private List<RangeHighlighter> onlyFromLayer(List<RangeHighlighter> highlighters) {
        List<RangeHighlighter> pomList = new ArrayList<>();
        for (RangeHighlighter highlighter : highlighters)
            if (highlighter.getLayer() == 3333)
                pomList.add(highlighter);
        return pomList;
    }

    private String parseEditorName(String editorName) {
        String[] arr = editorName.split("/");
        return arr[arr.length - 1].split("]")[0];
    }


    static List<RangeHighlighter> getRangeHighlighterList() {
        return rangeHighlighterList;
    }

    //1 -> editor string name, 2. -> row of error, 3. -> problem string, 4. -> color of highlighting (not in table)
    void updateTable(JTable table) {
        int rowCounter = 0;
        //int rowActual=10;
        MyTableModel model = new MyTableModel(createColumnVector(), 0);
        table.setModel(model);
        editorList = new ArrayList<>();
        rangeHighlighterList = new ArrayList<>();
        List<List<Object>> tableContent = new ArrayList<>();
        Editor[] editors = EditorFactory.getInstance().getAllEditors();

        for (Editor editor : editors) {
            List<RangeHighlighter> highlighters = getAllHighlightersFromMyLayer(editor);
            if (highlighters.size() == 0) continue;
            highlighters.sort(Comparator.comparingInt(RangeMarker::getStartOffset));
            rangeHighlighterList.addAll(highlighters);
            highlighters.sort(Comparator.comparingInt(RangeMarker::getStartOffset));
            for (RangeHighlighter highlighter : highlighters) {
                List<Object> row = new ArrayList<>();
                row.add(parseEditorName(editor.getDocument().toString()));
                row.add(1 + editor.getDocument().getLineNumber(highlighter.getStartOffset()));
                row.add(highlighter.getErrorStripeTooltip());
                row.add(highlighter.getTextAttributes().getBackgroundColor());
                tableContent.add(row);
                editorList.add(editor);
            }
        }

        for (List<Object> objectList : tableContent)
            addIntoTable(objectList, rowCounter++, model);
        table.setModel(model);
        table.getColumnModel().getColumn(1).setMaxWidth(40);
        table.getColumnModel().getColumn(2).setMinWidth(250);
    }

    private List<RangeHighlighter> getAllHighlightersFromMyLayer(Editor editor) {
        List<RangeHighlighter> highlighters = new ArrayList<>();
        highlighters.addAll(Arrays.asList(editor.getMarkupModel().getAllHighlighters()));
        highlighters.removeIf(highlighter -> highlighter.getLayer() != 3333);
        return highlighters;
    }
}
