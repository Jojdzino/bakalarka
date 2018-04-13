package edu.fiit.schneider_plugin.window;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("UseJBColor")
class MyTableModel extends DefaultTableModel {

    private List<Color> rowColours = new ArrayList<>();

    MyTableModel(Vector columnVector, int rowActual) {
        this.columnIdentifiers = columnVector;
        this.setRowCount(rowActual);
    }


    void setRowColour(int row, Color color) {
        rowColours.add(color);
        fireTableRowsUpdated(row, row);
    }

    Color getRowColour(int row) {
        return rowColours.get(row);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}