package edu.fiit.schneider_plugin.window;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Updates background based on color from given value. Value given is list of two values. First is string to display.
 * Second is color of which background should be.
 */
@SuppressWarnings("UseJBColor")
class MyTableCellRender extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        MyTableModel model = (MyTableModel) table.getModel();
        this.setOpaque(true);
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (model.getRowColour(row) != null)
            c.setBackground(model.getRowColour(row));
        c.setForeground(Color.black);
        return c;
    }
}
