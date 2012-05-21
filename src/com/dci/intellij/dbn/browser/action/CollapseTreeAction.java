package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class CollapseTreeAction extends AnAction {
    public CollapseTreeAction() {
        super("Collapse all", null, Icons.COMMON_COLLAPSE_ALL);
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        browserManager.getActiveBrowserTree().collapseAll();
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Collapse all");
    }
}