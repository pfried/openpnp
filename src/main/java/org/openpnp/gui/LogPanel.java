package org.openpnp.gui;

import org.openpnp.logging.LogEntryListCellRenderer;
import org.openpnp.logging.LogEntryListModel;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.LogEntry;

class LogPanel extends JPanel {

    private Preferences prefs = Preferences.userNodeForPackage(LogPanel.class);

    private static final String PREF_LOG_LEVEL = "LogPanel.logLevel";
    private static final String PREF_LOG_LEVEL_DEF = Level.INFO.toString();

    private static final String PREF_LOG_LINE_LIMIT = "LogPanel.lineLimit";
    private static final int PREF_LOG_LINE_LIMIT_DEF = 1000;

    private static final String PREF_LOG_AUTO_SCROLL = "LogPanel.autoScroll";
    private static final boolean PREF_LOG_AUTO_SCROLL_DEF = true;
    private boolean autoScroll = false;

    private JTextField searchTextField;

    private LogEntryListModel logEntries = new LogEntryListModel();

    private Predicate<LogEntry> logLevelFilter = logEntry -> logEntry.getLevel().compareTo(Level.INFO) <= 0;

    HashMap<String, Integer> lineLimits = new HashMap<String, Integer>() {
        {
            put("100", 100);
            put("1000", 1000);
            put("10000", 10000);
            put("Unlimited", -1);
        }
    };

    public LogPanel() {
        setLayout(new BorderLayout(0, 0));

        JPanel toolBarAndSearch = new JPanel();
        add(toolBarAndSearch, BorderLayout.NORTH);
        toolBarAndSearch.setLayout(new BorderLayout(0, 0));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBarAndSearch.add(toolBar);

        JPanel panel_1 = new JPanel();
        toolBarAndSearch.add(panel_1, BorderLayout.EAST);

        JLabel lblSearch = new JLabel("Search");
        panel_1.add(lblSearch);

        searchTextField = new JTextField();
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void insertUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        panel_1.add(searchTextField);
        searchTextField.setColumns(15);

        logEntries.addFilter(logLevelFilter);

        JButton btnLineLimit = new JButton("Line Limit");
        toolBar.add(btnLineLimit);

        JButton btnLogLevel = new JButton("Log Level");
        toolBar.add(btnLogLevel);
        
        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> logEntries.clear());

        toolBar.add(btnClear);

        JToggleButton btnScroll = new JToggleButton("Auto Scrolling");
        autoScroll = prefs.getBoolean(PREF_LOG_AUTO_SCROLL, PREF_LOG_AUTO_SCROLL_DEF);
        btnScroll.setSelected(autoScroll);

        btnScroll.addActionListener(e -> {
            autoScroll = ((JToggleButton) e.getSource()).isSelected();
            prefs.putBoolean(PREF_LOG_AUTO_SCROLL, autoScroll);
        });

        toolBar.add(btnScroll);

        JList<LogEntry> logEntryJList = new JList<>(logEntries);
        logEntryJList.setCellRenderer(new LogEntryListCellRenderer());
        logEntries.setLineLimit(prefs.getInt(PREF_LOG_LINE_LIMIT, PREF_LOG_LINE_LIMIT_DEF));

        logEntryJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                JList logEntryList = (JList) mouseEvent.getSource();
                int index = logEntryList.locationToIndex(mouseEvent.getPoint());
                if (index >= 0) {
                    LogEntry entry  = (LogEntry) logEntryList.getModel().getElementAt(index);
                }
            }
        });

        KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);

        // On Copy Command (Ctrl + C) copy the actual text content of the LogEntries
        logEntryJList.registerKeyboardAction(actionEvent -> {
            ArrayList<LogEntry> logList = (ArrayList<LogEntry>) logEntryJList.getSelectedValuesList();
            StringBuilder sb = new StringBuilder();
            logList.forEach(logEntry -> sb.append(logEntry.getRenderedLogEntry()));
            StringSelection selection = new StringSelection(sb.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        }, keystroke, JComponent.WHEN_FOCUSED);

        JScrollPane scrollPane = new JScrollPane(logEntryJList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // Scroll down if enabled
        logEntries.addListDataListener(new ListDataListener() {

            private void scrollDown() {
                if(autoScroll) {
                    logEntryJList.ensureIndexIsVisible(logEntries.getSize() - 1);
                }
            }
            @Override
            public void intervalAdded(ListDataEvent e) {
                scrollDown();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                scrollDown();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                scrollDown();
            }
        });
        
        // This weird check is here because I mistakenly reused the same config key when
        // switching from slf to tinylog. This meant that some users had an int based
        // value in the key rather than the string. This caused initialization failures.
        Level level = null;
        try {
            level = Level.valueOf(prefs.get(PREF_LOG_LEVEL, PREF_LOG_LEVEL_DEF));
        }
        catch (Exception ignored) {
        }
        if (level == null ) {
            level = Level.INFO;
        }

        Configurator
            .currentConfig()
            .level(level)
            .activate();
        Configurator
            .currentConfig()
            .addWriter(logEntries)
            .activate();

        JPopupMenu lineLimitPopupMenu = createLineLimitMenu();
        btnLineLimit.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lineLimitPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        JPopupMenu logLevelPopupMenu = createLogLevelMenu();
        btnLogLevel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                logLevelPopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    private JPopupMenu createLineLimitMenu() {
        ButtonGroup buttonGroup = new ButtonGroup();

        JPopupMenu menu = new JPopupMenu();

        lineLimits.forEach((label, limit) -> {
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(label);
            menuItem.setActionCommand(limit.toString());
            menuItem.addActionListener(setLineLimitAction);
            if (limit == prefs.getInt(PREF_LOG_LINE_LIMIT, PREF_LOG_LINE_LIMIT_DEF)) {
                menuItem.setSelected(true);
            }
            buttonGroup.add(menuItem);
            menu.add(menuItem);
        });

        return menu;
    }

    private JPopupMenu createLogLevelMenu() {
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButtonMenuItem menuItem;

        JPopupMenu menu = new JPopupMenu();

        for (Level level : Level.values()) {
            menuItem = new JRadioButtonMenuItem(level.toString());
            if (level.toString().equals(prefs.get(PREF_LOG_LEVEL, PREF_LOG_LEVEL_DEF))) {
                menuItem.setSelected(true);
            }
            menuItem.addActionListener(setThresholdAction);
            buttonGroup.add(menuItem);
            menu.add(menuItem);
        }

        return menu;
    }

    private Action setThresholdAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String s = e.getActionCommand();
            prefs.put(PREF_LOG_LEVEL, s);
            Level level = Level.valueOf(s);

            Configurator
                .currentConfig()
                .level(level)
                .activate();
        }
    };

    private Action setLineLimitAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int lineLimit = Integer.parseInt(e.getActionCommand());
            logEntries.setLineLimit(lineLimit);
            prefs.putInt(PREF_LOG_LINE_LIMIT, lineLimit);
        }
    };

}
