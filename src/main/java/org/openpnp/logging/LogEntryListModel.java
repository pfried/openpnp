package org.openpnp.logging;

import org.pmw.tinylog.Configuration;
import org.pmw.tinylog.LogEntry;
import org.pmw.tinylog.writers.LogEntryValue;
import org.pmw.tinylog.writers.Writer;

import javax.swing.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A LogEntry List Model which contains LogEntries from tinylog
 */
public class LogEntryListModel extends AbstractListModel<LogEntry> implements Writer {

    private List<LogEntry> originalLogEntries = new ArrayList<>();
    private List<LogEntry> filteredLogEntries = new ArrayList<>(originalLogEntries);
    private HashSet<Predicate<LogEntry>> filters = new HashSet<>();

    private int lineLimit = 1000;

    public void setLineLimit(int lineLimit) {
        this.lineLimit = lineLimit;
        trim();
    }

    public int getLineLimit() {
        return lineLimit;
    }

    public List<LogEntry> getOriginalLogEntries() {
        return originalLogEntries;
    }

    public List<LogEntry> getFilteredLogEntries() {
        return filteredLogEntries;
    }

    @Override
    public int getSize() {
        return filteredLogEntries.size();
    }

    @Override
    public LogEntry getElementAt(int index) {
        return filteredLogEntries.get(index);
    }

    public void addFilter(Predicate<LogEntry> filter) {
        this.filters.add(filter);
        filter();
    }

    public void removeFilter(Predicate<LogEntry> filter) {
        this.filters.remove(filter);
        filter();
    }

    @Override
    public Set<LogEntryValue> getRequiredLogEntryValues() {
        return EnumSet.of(LogEntryValue.RENDERED_LOG_ENTRY);
    }

    @Override
    public void init(Configuration configuration) throws Exception {

    }

    @Override
    public void write(LogEntry logEntry) throws Exception {
        originalLogEntries.add(logEntry);
        trim();
    }

    public void clear() {
        this.originalLogEntries.clear();
        filter();
    }

    public void filter() {
        // Reduce all filters to a single one and apply it to our logEntries
        filteredLogEntries = originalLogEntries.stream().filter(
            filters.stream().reduce(Predicate::and).orElse(t->true)
        ).collect(Collectors.toList());

        SwingUtilities.invokeLater(() -> fireContentsChanged(this, 0,0));
    }

    private void trim() {
        if (lineLimit > 0 && (originalLogEntries.size() > lineLimit)) {
            originalLogEntries.subList(0, originalLogEntries.size() - lineLimit).clear();
        }
        filter();
    }

    @Override
    public void flush() throws Exception {

    }

    @Override
    public void close() throws Exception {

    }
}
