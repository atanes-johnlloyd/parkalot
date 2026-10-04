package view.Admin;

import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ActionButtonRenderer extends JPanel implements TableCellRenderer {

    private JButton updateButton;
    private JButton deleteButton;

    public ActionButtonRenderer() {

        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");

        updateButton.setPreferredSize(new java.awt.Dimension(80, 25));
        deleteButton.setPreferredSize(new java.awt.Dimension(80, 25));

        updateButton.setFocusable(false);
        deleteButton.setFocusable(false);

        add(updateButton);
        add(deleteButton);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {

        String status = table.getValueAt(row, 5).toString();
        // Fix: show delete for everyone except already deleted accounts
        deleteButton.setVisible(!status.equalsIgnoreCase("deleted"));
        return this;
    }
}