package org.openpnp.gui.support;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import javax.swing.*;

import org.pmw.tinylog.Configuration;
import org.pmw.tinylog.LogEntry;
import org.pmw.tinylog.writers.LogEntryValue;
import org.pmw.tinylog.writers.Writer;

public class JTextLogWriter implements Writer {
    private DefaultListModel<LogEntry> logEntryList;

    private int lineLimit = 1000;

    public JTextLogWriter(DefaultListModel<LogEntry>  logEntryList) {
        this.logEntryList = logEntryList;
    }

    public void setLineLimit(int lineLimit) {
        this.lineLimit = lineLimit;
        trim();
    }

    public int getLineLimit() {
        return lineLimit;
    }

    @Override
    public Set<LogEntryValue> getRequiredLogEntryValues() {
        return EnumSet.of(LogEntryValue.RENDERED_LOG_ENTRY);
    }

    @Override
    public void init(final Configuration configuration) throws IOException {}

    @Override
    public void write(final LogEntry logEntry) throws IOException {
        this.logEntryList.addElement(logEntry);
        trim();
    }

    @Override
    public void flush() throws IOException {}

    @Override
    public void close() throws IOException {}

    private void trim() {
        try {
            if (lineLimit > 0 && (logEntryList.size() > lineLimit)) {
                logEntryList.removeRange(0, logEntryList.size() - lineLimit);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
