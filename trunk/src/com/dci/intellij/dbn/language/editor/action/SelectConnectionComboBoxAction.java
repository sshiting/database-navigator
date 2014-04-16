package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.vfs.SQLConsoleFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SelectConnectionComboBoxAction extends DBNComboBoxAction {
    private static final String NAME = "DB Connections";

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        Project project = ActionUtil.getProject(component);
        return new SelectConnectionActionGroup(project);
    }

    public synchronized void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        String text = NAME;
        Icon icon = null;

        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler activeConnection = connectionMappingManager.getActiveConnection(virtualFile);
            if (activeConnection != null) {
                text = NamingUtil.enhanceUnderscoresForDisplay(activeConnection.getQualifiedName());
                icon = activeConnection.getIcon();
            }

            boolean isConsole = virtualFile instanceof SQLConsoleFile;
            presentation.setVisible(!isConsole);
        }

        presentation.setText(text);
        presentation.setIcon(icon);
    }
 }
