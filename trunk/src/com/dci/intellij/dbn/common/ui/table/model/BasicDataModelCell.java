package com.dci.intellij.dbn.common.ui.table.model;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.editor.data.options.DataEditorQualifiedEditorSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.intellij.openapi.project.Project;

public class BasicDataModelCell implements DataModelCell{
    protected BasicDataModelRow row;
    protected ColumnInfo columnInfo;
    protected Object userValue;
    private String formattedUserValue;
    protected int index;

    public BasicDataModelCell(Object userValue, BasicDataModelRow row, ColumnInfo columnInfo) {
        this.userValue = userValue;
        this.row = row;
        index = columnInfo.getColumnIndex();
        this.columnInfo = columnInfo;
    }

    public Project getProject() {
        return row.getModel().getProject();
    }

    public TextContentType getContentType() {
        DataModelState state = row.getModel().getState();
        String contentTypeName = state.getTextContentTypeName(getColumnInfo().getName());
        DataEditorQualifiedEditorSettings qualifiedEditorSettings = DataEditorSettings.getInstance(getProject()).getQualifiedEditorSettings();
        return qualifiedEditorSettings.getContentType(contentTypeName);
    }

    public void setContentType(TextContentType contentType) {
        DataModelState state = row.getModel().getState();
        state.setTextContentType(getColumnInfo().getName(), contentType.getName());
    }

    public BasicDataModelRow getRow() {
        return row;
    }

    public void setUserValue(Object userValue) {
        this.userValue = userValue;
        this.formattedUserValue = null;
    }

    public void updateUserValue(Object userValue, boolean bulk) {
        setUserValue(userValue);
    }

    public Object getUserValue() {
        return userValue;
    }

    @Override
    public String getFormattedUserValue() {
        if (formattedUserValue == null && userValue != null) {
            RegionalSettings regionalSettings = RegionalSettings.getInstance(getProject());
            Formatter formatter = regionalSettings.getFormatter();
            formattedUserValue = formatter.formatObject(userValue);
        }
        return formattedUserValue;
    }

    public String getName() {
        return columnInfo.getName();
    }

    public ColumnInfo getColumnInfo() {
        return columnInfo;
    }

    public int getIndex() {
        return index;
    }

    public String toString() {
        return userValue == null ? null : userValue.toString();
    }

/*
    public boolean equals(Object obj) {
        DataModelCell remoteCell = (DataModelCell) obj;
        return CommonUtil.safeEqual(getUserValue(), remoteCell.getUserValue());
    }
*/

    @Override
    public boolean equals(Object obj) {
        if (!isDisposed() && obj instanceof BasicDataModelCell) {
            BasicDataModelCell cell = (BasicDataModelCell) obj;
            return cell.getIndex() == getIndex() &&
                    cell.getRow().getIndex() == getRow().getIndex() &&
                    cell.getRow().getModel() == getRow().getModel();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return index + row.getIndex() + row.getModel().hashCode();
    }

    public void dispose() {
        row = null;
        columnInfo = null;
        userValue = null;
        formattedUserValue = null;
    }

    public boolean isDisposed() {
        return columnInfo == null;
    }
}
