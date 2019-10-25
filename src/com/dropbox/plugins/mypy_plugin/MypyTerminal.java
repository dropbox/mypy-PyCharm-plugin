package com.dropbox.plugins.mypy_plugin;

import com.dropbox.plugins.mypy_plugin.model.MypyError;
import com.dropbox.plugins.mypy_plugin.model.MypyResult;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import icons.MypyIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.max;

public final class MypyTerminal {
    private JPanel mypyToolWindowContent;
    private JBList<MypyError> errorsList;
    private JTextField mypyStatus;
    private JButton mypyRun;
    @SuppressWarnings("unused")
    private JScrollPane scroll;
    private int rightIndex;
    private final Project project;
    private ListCellRenderer<? super MypyError> defaultRenderer;
    private ListCellRenderer<MypyError> mypyRenderer;
    private MypyRunner runner;
    private ArrayList<String> errorFiles;
    private Set<String> collapsed;
    private HashMap<String, ArrayList<MypyError>> errorMap;

    final static int GRAY = 11579568;
    final static int DARK_GRAY = 7368816;
    private final static int LIGHT_GREEN = 13500365;
    private final static int LIGHT_RED = 16764365;
    final static int BLACK = 0;
    final static int WHITE = 16777215;
    final public static String ERROR_MARK = ": error:";
    final public static String NOTE_MARK = ": note:";
    final static String ERROR_RE = ".+" + ERROR_MARK + ".+";
    final static String NOTE_RE = ".+" + NOTE_MARK + ".+";

    public MypyTerminal (Project project) {
        this.project = project;
    }

    public MypyRunner getRunner() {
        return runner;
    }

    public JBList<MypyError> getErrorsList() {
        return errorsList;
    }

    public void toggleExpand(MypyError error) {
        String file = error.getFile();
        if (collapsed.contains(file)) {
            collapsed.remove(file);
        } else {
            collapsed.add(file);
        }
    }

    void initUI(ToolWindow toolWindow) {
        errorsList.getEmptyText().setText("");
        errorsList.setListData(new MypyError[] {});
        runner = new MypyRunner(errorsList, project);
        rightIndex = 0;

        // List popup menu.

        JBPopupMenu popup = new JBPopupMenu();
        JBMenuItem gotoItem = new JBMenuItem("Go to error");
        gotoItem.addActionListener(e -> {
            int tot = MypyTerminal.this.errorsList.getModel().getSize();
            int right = MypyTerminal.this.rightIndex;
            int index = MypyTerminal.this.errorsList.getSelectedIndex();
            if ((right >= 0) & (right < tot)) {
                MypyTerminal.this.errorsList.setSelectedIndex(right);
                // If it was already selected, we need to trigger this manually.
                if (right == index) {
                    MypyTerminal.this.openError(index);
                }
            }
        });
        gotoItem.setIcon(AllIcons.Debugger.Actions.Force_run_to_cursor);
        gotoItem.setDisabledIcon(AllIcons.Debugger.Actions.Force_run_to_cursor);
        popup.add(gotoItem);
        JBMenuItem copyItem = new JBMenuItem("Copy error text");
        copyItem.addActionListener(e -> {
            MypyError error = MypyTerminal.this.errorsList.getModel().getElementAt(
                    MypyTerminal.this.rightIndex);
            StringSelection selection = new StringSelection(error.getRaw());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        });
        copyItem.setIcon(AllIcons.Actions.Copy);
        copyItem.setDisabledIcon(AllIcons.Actions.Copy);
        popup.add(copyItem);
        JBMenuItem copyAllItem = new JBMenuItem("Copy all errors");
        copyAllItem.addActionListener(e -> {
            ArrayList<String> allErrors = new ArrayList<>();
            int size = MypyTerminal.this.errorsList.getModel().getSize();
            if (size == 0) {
                return;
            }
            for (int i = 0; i < size; i++) {
                MypyError err = MypyTerminal.this.errorsList.getModel().getElementAt(i);
                if (err.getLevel() == MypyError.HEADER) {
                    continue;
                }
                allErrors.add(err.getRaw());
            }
            String error = String.join("\n", allErrors);
            StringSelection selection = new StringSelection(error);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        });
        popup.add(copyAllItem);
        JBMenuItem expandItem = new JBMenuItem("Expand");
        expandItem.addActionListener(e -> {
            MypyError error = MypyTerminal.this.errorsList.getModel().getElementAt(
                    MypyTerminal.this.rightIndex);
            if (error.getLevel() == MypyError.HEADER) {
                MypyTerminal.this.toggleExpand(error);
                MypyTerminal.this.renderList();
                MypyTerminal.this.errorsList.setSelectedIndex(MypyTerminal.this.rightIndex);
            }
        });
        popup.add(expandItem);
        JBMenuItem helpItem = new JBMenuItem("Help");
        helpItem.addActionListener(e -> MypyHelp.show(project));
        popup.add(helpItem);
        JSeparator sep = new JSeparator();
        popup.add(sep);
        JBMenuItem configItem = new JBMenuItem("Configure plugin...");
        configItem.addActionListener(e -> {
            MypyConfigDialog dialog = new MypyConfigDialog(project);
            dialog.show();
        });
        popup.add(configItem);

        // List selection listener.

        errorsList.addListSelectionListener(e -> {
            int index = MypyTerminal.this.errorsList.getSelectedIndex();
            Rectangle rect = MypyTerminal.this.errorsList.getCellBounds(index, index);
            if (rect != null) {
                MypyTerminal.this.errorsList.scrollRectToVisible(rect);
            }
            MypyTerminal.this.openError(index);
        });

        // List mouse listener.

        errorsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = MypyTerminal.this.errorsList.locationToIndex(e.getPoint());
                if ((e.getButton() == MouseEvent.BUTTON2) |
                        (e.getButton() == MouseEvent.BUTTON3) | (e.isControlDown())) {
                    boolean active = !(MypyTerminal.this.runner.isRunning());
                    boolean isError = false;
                    boolean isExpanded = false;
                    boolean isHeader = false;
                    if (index >= 0) {
                        MypyError error = MypyTerminal.this.errorsList.getModel().getElementAt(index);
                        isError = error.isError();
                        isExpanded = !error.isCollapsed();
                        isHeader = error.getLevel() == MypyError.HEADER;
                    }
                    gotoItem.setEnabled(active & isError);
                    gotoItem.updateUI();
                    copyItem.setEnabled(active & !isHeader & (index >= 0));
                    copyItem.updateUI();
                    copyAllItem.setEnabled(active);
                    copyAllItem.updateUI();
                    expandItem.setEnabled(active & isHeader);
                    expandItem.setText(isExpanded ? "Collapse" : "Expand");
                    expandItem.updateUI();
                    configItem.updateUI();
                    helpItem.updateUI();
                    MypyTerminal.this.rightIndex = index;
                    popup.updateUI();
                    popup.show(e.getComponent(), e.getX(), e.getY());
                    return;
                }
                if (e.isAltDown()) {
                    String error = MypyTerminal.this.errorsList.getModel()
                            .getElementAt(index).getMessage();
                    Pattern http = Pattern.compile("http://\\S+");  // TODO: Use better regex.
                    Matcher matcher = http.matcher(error);
                    if (matcher.find()) {
                        String link = error.substring(matcher.start(0), matcher.end(0));
                        try {
                            Desktop.getDesktop().browse(new URL(link).toURI());
                        } catch (URISyntaxException | IOException exc) {
                            Messages.showMessageDialog(project, exc.getMessage(),
                                    "Plugin Exception:", Messages.getErrorIcon());
                        }
                    }
                    return;
                }
                if (e.getClickCount() >= 1) {
                    if (index >= 0) {
                        MypyError error = MypyTerminal.this.errorsList.getModel().getElementAt(index);
                        boolean expandable = (error.getLevel() == MypyError.HEADER);
                        if (expandable) {
                            MypyTerminal.this.toggleExpand(error);
                            MypyTerminal.this.renderList();
                            MypyTerminal.this.errorsList.setSelectedIndex(index);
                        } else {
                            int old = MypyTerminal.this.errorsList.getSelectedIndex();
                            if (old == index) {
                                // manually trigger if selection didn't change
                                MypyTerminal.this.openError(index);
                            } else {
                                MypyTerminal.this.errorsList.setSelectedIndex(index);
                            }
                        }
                    }

                }
            }
        });

        // Final strokes.

        mypyRun.addActionListener(e -> MypyTerminal.this.runMypyDaemonUIWrapper());
        mypyRun.setIcon(MypyIcons.MYPY_SMALL);
        mypyRenderer = new MypyCellRenderer();
        defaultRenderer = errorsList.getCellRenderer();
        
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mypyToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public void runMypyDaemonUIWrapper() {
        runMypyDaemonUIWrapper(null, null);
    }

    public void runMypyDaemonUIWrapper(@Nullable String command, @Nullable VirtualFile vf) {

        setWaiting();
        FileDocumentManager.getInstance().saveAllDocuments();
        // Invoke mypy daemon runner script in a sub-thread,
        // it looks like UI is blocked on it otherwise.
        Executors.newSingleThreadExecutor().execute(() -> {
            Thread.currentThread().setName("MypyRunnerThread");
            MypyResult result = MypyTerminal.this.runner.runMypyDaemon(command, vf);
            if (result == null) return;
            // Access UI is prohibited from non-dispatch thread.
            ApplicationManager.getApplication().invokeLater(() -> {
                MypyTerminal.this.setReady(result);
                ToolWindow tw = ToolWindowManager.getInstance(project).getToolWindow(
                        MypyToolWindowFactory.MYPY_PLUGIN_ID);
                if (!tw.isVisible()) {
                    String suffix = result.getErrCount() != 1 ? "s" : "";
                    NotificationType n_type =
                            result.getRetCode() != 0 ? NotificationType.WARNING : NotificationType.INFORMATION;
                    Notification completed = new Notification("Indexing", "Mypy Daemon",
                            String.format("Type checking completed: %d error%s found",
                                    result.getErrCount(), suffix),
                            n_type);
                    Notifications.Bus.notify(completed);
                }
                if (result.getErrCount() == 0 & result.getNoteCount() == 0) {
                    return;
                }
                if (result.getRetCode() != 0) {
                    MypyTerminal.this.makeErrorMap(result);
                    MypyTerminal.this.generateMarkers(result);
                    MypyTerminal.this.collapsed = new HashSet<>();
                    MypyTerminal.this.renderList();
                    MypyTerminal.this.errorsList.setSelectedIndex(0);
                }
            });
        });
    }

    private void setWaiting() {
        errorsList.setForeground(new JBColor(new Color(GRAY), new Color(DARK_GRAY)));
        errorsList.setCellRenderer(defaultRenderer);
        mypyStatus.setText("Running...");
        mypyStatus.setForeground(new JBColor(new Color(BLACK), new Color(GRAY)));
        mypyStatus.setBackground(new JBColor(new Color(WHITE), new Color(BLACK)));
        errorsList.setListData(new MypyError[] {});
        errorsList.setPaintBusy(true);
        mypyRun.setText("Wait...");
        mypyRun.setEnabled(false);
    }

    private void setReady(MypyResult result) {
        mypyRun.setText("Run");
        mypyRun.setEnabled(true);
        errorsList.setPaintBusy(false);
        if (result == null) { // IO exception happened
            mypyStatus.setText("Internal problem...");
            return;
        }
        if (result.getRetCode() == 0) {
            mypyStatus.setText("PASSED");
            mypyStatus.setForeground(new JBColor(new Color(BLACK), new Color(100, 255, 100)));
            mypyStatus.setBackground(new JBColor(new Color(LIGHT_GREEN), new Color(BLACK)));
        } else {
            String suffix = result.getErrCount() != 1 ? "s" : "";
            mypyStatus.setText(String.format("FAILED: %d error%s", result.getErrCount(), suffix));
            mypyStatus.setForeground(new JBColor(new Color(BLACK), new Color(255, 100, 100)));
            mypyStatus.setBackground(new JBColor(new Color(LIGHT_RED), new Color(BLACK)));
            if (result.getErrCount() == 0 & result.getNoteCount() == 0) {
                // keep debug output
                return;
            }
        }
        // clear debug output
        errorsList.setListData(new MypyError[] {});
        errorsList.setForeground(new JBColor(new Color(BLACK), new Color(GRAY)));
        errorsList.setCellRenderer(mypyRenderer);
    }

    private void makeErrorMap(MypyResult result) {
        HashMap<String, ArrayList<MypyError>> map = new HashMap<>();
        ArrayList<MypyError> errors = result.getErrors();
        ArrayList<String> files = new ArrayList<>();
        for (MypyError next: errors) {
            String file = next.getFile();
            if (!map.containsKey(file)) {
                map.put(file, new ArrayList<>());
            }
            map.get(file).add(next);
            if (!files.contains(file)) {
                files.add(file);
            }
        }
        errorMap = map;
        errorFiles = files;
    }

    private void generateMarkers(MypyResult result) {
        for (MypyError error: result.getErrors()) {
            if (error.isError()) {
                String directory = project.getBaseDir().getPath();
                String file = error.getFile();
                int line = max(error.getLine() - 1, 0);
                VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(directory + File.separator + file);
                if (vf != null) {
                    Document document = FileDocumentManager.getInstance().getCachedDocument(vf);
                    if (document != null) {
                        error.marker = document.createRangeMarker(document.getLineStartOffset(line),
                                document.getLineEndOffset(line));
                    }
                }
            }
        }
    }

    public void renderList() {
        ArrayList<MypyError> lines = new ArrayList<>();
        for (String file: errorFiles) {
            boolean toggle = collapsed.contains(file);
            int errs = 0;
            for (MypyError error: errorMap.get(file)) {
                if (error.getLevel() == MypyError.ERROR) {
                    errs++;
                }
            }
            MypyError title = new MypyError(file, MypyError.HEADER, errs);
            if (toggle) {
                title.toggle();
            }
            lines.add(title);
            if (!collapsed.contains(file)) {
                lines.addAll(errorMap.get(file));
            }
        }
        MypyError[] data = new MypyError[lines.size()];
        data = lines.toArray(data);
        errorsList.setListData(data);
    }

    private void openError(int index) {
        if ((index >= errorsList.getModel().getSize()) | (index < 0)) {
            return;
        }
        if (runner.isRunning()) {
            return;
        }
        MypyError error = errorsList.getModel().getElementAt(index);
        String directory = project.getBaseDir().getPath();
        if (error.isError()) {
            String file = error.getFile();
            int line = max(error.getLine() - 1, 0);
            int column = max(error.getColumn() - 1, 0);
            VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(directory + File.separator + file);
            // May be null if an error is shown in a file beyond repository
            // (e.g. typeshed or a deleted file because of a bug).
            if (vf != null) {
                FileEditor[] editors = FileEditorManagerEx.getInstanceEx(project).openFile(vf, true);
                if (editors[0] instanceof TextEditor) {
                    Editor editor = ((TextEditor) editors[0]).getEditor();
                    if (error.marker == null) {
                        // Try re-creating markers, likely the file was not in cache after the type check.
                        // TODO: do this on document opening for all documents?
                        Document document = FileDocumentManager.getInstance().getCachedDocument(vf);
                        if (document != null) {
                            for (MypyError e: errorMap.get(error.getFile())) {
                                int errorLine = max(e.getLine() - 1, 0);
                                e.marker = document.createRangeMarker(document.getLineStartOffset(errorLine),
                                        document.getLineEndOffset(errorLine));
                            }
                        }
                    }
                    if (error.marker != null && error.marker.isValid()) {
                        editor.getCaretModel().getPrimaryCaret().moveToOffset(error.marker.getStartOffset());
                    }
                    else {
                        LogicalPosition pos = new LogicalPosition(line, column);
                        editor.getCaretModel().getPrimaryCaret().moveToLogicalPosition(pos);
                    }
                    editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                    if (error.marker != null && error.marker.isValid()) {
                        editor.getSelectionModel().selectLineAtCaret();
                    }
                }
            }
        }
    }
}
