package org.openpnp.logging;

import org.pmw.tinylog.LogEntry;

import javax.swing.*;
import java.awt.*;

public class LogEntryListCellRenderer extends JTextArea implements ListCellRenderer<LogEntry> {

    @Override
    public Component getListCellRendererComponent(JList<? extends LogEntry> list, LogEntry logEntry, int index, boolean isSelected, boolean cellHasFocus) {

        this.setText(logEntry.getRenderedLogEntry());
        this.setFont(new Font("Monospaced", Font.PLAIN, 13));
        this.setEditable(false);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}