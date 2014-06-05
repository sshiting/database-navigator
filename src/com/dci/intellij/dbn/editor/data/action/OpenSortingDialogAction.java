package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.sorting.DatasetSortingManager;
import com.dci.intellij.dbn.editor.data.sorting.DatasetSortingState;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class OpenSortingDialogAction extends AbstractDataEditorAction {

    public OpenSortingDialogAction() {
        super("Sorting...", Icons.DATA_SORTING);
    }

    public void actionPerformed(AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);

        if (datasetEditor != null) {
            DBDataset dataset = datasetEditor.getDataset();
            if (dataset != null) {
                DatasetSortingManager sortingManager = DatasetSortingManager.getInstance(datasetEditor.getProject());

                DatasetSortingState sortingState = new DatasetSortingState();

                for (DBColumn column : dataset.getColumns()) {
                    sortingState.applySorting(column, SortDirection.ASCENDING, true);
                    if (sortingState.getSortingInstructions().size() > 3) break;
                }

                sortingManager.openSortingDialog(dataset, sortingState);
            }
        }
    }

    public void update(AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);

        Presentation presentation = e.getPresentation();
        presentation.setText("Sorting...");

        boolean enabled =
                datasetEditor != null &&
                datasetEditor.getEditorTable() != null &&
                !datasetEditor.isInserting();
        presentation.setEnabled(enabled);
        presentation.setVisible(DatabaseNavigator.getInstance().isDeveloperModeEnabled());
    }
}
