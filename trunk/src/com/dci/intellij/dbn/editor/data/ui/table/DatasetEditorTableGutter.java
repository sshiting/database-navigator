package com.dci.intellij.dbn.editor.data.ui.table;

import com.dci.intellij.dbn.common.ui.table.BasicTableGutter;
import com.dci.intellij.dbn.editor.data.ui.table.renderer.DatasetEditorTableGutterRenderer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class DatasetEditorTableGutter extends BasicTableGutter {
    public DatasetEditorTableGutter(DatasetEditorTable table) {
        super(table);
        setCellRenderer(DatasetEditorTableGutterRenderer.INSTANCE);
        setFixedCellWidth(48);
        addMouseListener(mouseListener);
    }

    MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                getTable().getDatasetEditor().openRecordEditor(getSelectedIndex());
            }
        }
    };

    @Override
    public DatasetEditorTable getTable() {
        return (DatasetEditorTable) super.getTable();
    }
}
