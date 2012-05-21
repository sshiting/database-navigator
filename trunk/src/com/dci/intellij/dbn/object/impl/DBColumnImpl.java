package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeElement;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoaderRegistry;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationList;
import com.dci.intellij.dbn.object.common.list.ObjectListProvider;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;
import com.dci.intellij.dbn.object.properties.DBDataTypePresentableProperty;
import com.dci.intellij.dbn.object.properties.DBObjectPresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.properties.SimplePresentableProperty;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBColumnImpl extends DBObjectImpl implements DBColumn {
    private DBDataType dataType;
    private DBDataset dataset;
    private boolean isPrimaryKey;
    private boolean isForeignKey;
    private boolean isNullable;
    private boolean isHidden;
    private int position;

    private DBObjectList<DBConstraint> constraints;
    private DBObjectList<DBIndex> indexes;

    public DBColumnImpl(DBDataset dataset, ResultSet resultSet) throws SQLException {
        super(dataset);
        this.dataset = dataset;

        name = resultSet.getString("COLUMN_NAME");
        isPrimaryKey = "Y".equals(resultSet.getString("IS_PRIMARY_KEY"));
        isForeignKey = "Y".equals(resultSet.getString("IS_FOREIGN_KEY"));
        isNullable = "Y".equals(resultSet.getString("IS_NULLABLE"));
        isHidden = "Y".equals(resultSet.getString("IS_HIDDEN"));
        position = resultSet.getInt("POSITION");

        dataType = new DBDataType(this, resultSet);

        createLists();
    }

    private void createLists() {
        DBObjectListContainer container = getChildObjects();
        constraints = container.createSubcontentObjectList(DBObjectType.CONSTRAINT, this, CONSTRAINTS_LOADER, getDataset(), DBObjectRelationType.CONSTRAINT_COLUMN, false, false);
        indexes = container.createSubcontentObjectList(DBObjectType.INDEX, this, INDEXES_LOADER, getDataset(), DBObjectRelationType.INDEX_COLUMN, false, false);

        DBType declaredType = dataType.getDeclaredType();
        if (declaredType != null) {
            DBObjectList typeAttributes = declaredType.getChildObjects().getObjectList(DBObjectType.TYPE_ATTRIBUTE);
            container.addObjectList(typeAttributes);
        }
    }

    public DBObjectType getObjectType() {
        return DBObjectType.COLUMN;
    }

    public DBDataType getDataType() {
        return dataType;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public DBObject getDefaultNavigationObject() {
        if (isForeignKey()) {
            return getForeignKeyColumn();
        }
        return null;
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.append(false, " - ", true);
        ttb.append(false, dataType.getQualifiedName(), true);

        if (isPrimaryKey()) ttb.append(false,  "&nbsp;&nbsp;PK", true);
        if (isForeignKey()) ttb.append(false, isPrimaryKey() ? ",&nbsp;FK" : "&nbsp;&nbsp;FK", true);
        if (!isPrimaryKey() && !isForeignKey() && !isNullable()) ttb.append(false, "&nbsp;&nbsp;NOT NULL", true);

        if (isForeignKey() && getForeignKeyColumn() != null) {
            ttb.append(true, "FK column:&nbsp;", false);
            ttb.append(false, getForeignKeyColumn().getDataset().getName() + "." + getForeignKeyColumn().getName(), false);
        }

        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    public Icon getIcon() {
        return isPrimaryKey() ? isForeignKey() ? Icons.DBOBJECT_COLUMN_PFK : Icons.DBOBJECT_COLUMN_PK :
               isForeignKey() ? Icons.DBOBJECT_COLUMN_FK :
               isHidden() ? Icons.DBOBJECT_COLUMN_HIDDEN :
               Icons.DBOBJECT_COLUMN;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public DBDataset getDataset() {
        if (!isDisposed()) dataset = (DBDataset) dataset.getUndisposedElement();
        return dataset;
    }


    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public boolean isUniqueKey() {
        for (DBConstraint constraint : getConstraints()) {
            if (constraint.isUniqueKey()) {
                return true;
            }
        }
        return false;
    }

    public boolean isSinglePrimaryKey() {
        for (DBConstraint constraint : getConstraints()) {
            if (constraint.isPrimaryKey() && constraint.getColumns().size() == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean isForeignKey() {
        return isForeignKey;
    }

    public List<DBIndex> getIndexes() {
        return indexes.getObjects();
    }

    public List<DBConstraint> getConstraints() {
        return constraints.getObjects();
    }

    public int getConstraintPosition(DBConstraint constraint) {
        DBObjectRelationList<DBConstraintColumnRelation> constraintColumnRelations =
                getDataset().getChildObjectRelations().getObjectRelationList(DBObjectRelationType.CONSTRAINT_COLUMN);
        for (DBConstraintColumnRelation relation : constraintColumnRelations.getObjectRelations()) {
            if (relation.getColumn().equals(this) && relation.getConstraint().equals(constraint))
                return relation.getPosition();
        }
        return 0;
    }

    public DBConstraint getConstraintForPosition(int position) {
        DBObjectRelationList<DBConstraintColumnRelation> constraintColumnRelations =
                getDataset().getChildObjectRelations().getObjectRelationList(DBObjectRelationType.CONSTRAINT_COLUMN);
        for (DBConstraintColumnRelation relation : constraintColumnRelations.getObjectRelations()) {
            if (relation.getColumn().equals(this) && relation.getPosition() == position)
                return relation.getConstraint();
        }
        return null;
    }

    public DBColumn getForeignKeyColumn() {
        for (DBConstraint constraint : getConstraints()) {
            if (constraint.isForeignKey()) {
                Integer position = getConstraintPosition(constraint);
                DBConstraint foreignKeyConstraint = constraint.getForeignKeyConstraint();
                if (foreignKeyConstraint != null) {
                    return foreignKeyConstraint.getColumnForPosition(position);
                }
            }
        }
        return null;
    }

    public List<DBColumn> getReferencingColumns() {
        assert isPrimaryKey();

        DynamicContentLoaderRegistry.registerBulkLoad();
        List<DBColumn> list = new ArrayList<DBColumn>();
        boolean isSystemSchema = getDataset().getSchema().isSystemSchema();
        for (DBSchema schema : getConnectionHandler().getObjectBundle().getSchemas()) {
            ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
            if (progressIndicator != null && progressIndicator.isCanceled()) {
                break;
            }
            if (schema.isSystemSchema() == isSystemSchema) {
                List<DBColumn> columns = (List<DBColumn>) schema.getChildObjects().getHiddenObjectList(DBObjectType.COLUMN).getObjects();
                for (DBColumn column : columns){
                    if (this.equals(column.getForeignKeyColumn())) {
                        list.add(column);
                    }
                }
            }
        }
        DynamicContentLoaderRegistry.unregisterBulkLoad();

        return list;
    }

    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> navigationLists = new ArrayList<DBObjectNavigationList>();

        if (dataType.isDeclared()) {
            navigationLists.add(new DBObjectNavigationListImpl<DBType>("Type", dataType.getDeclaredType()));
        }

        if (constraints.size() > 0) {
            navigationLists.add(new DBObjectNavigationListImpl<DBConstraint>("Constraints", constraints.getObjects()));
        }

        if (getParentObject() instanceof DBTable) {
            if (indexes != null && indexes.size() > 0) {
                navigationLists.add(new DBObjectNavigationListImpl<DBIndex>("Indexes", indexes.getObjects()));
            }

            if (isForeignKey()) {
                navigationLists.add(new DBObjectNavigationListImpl<DBColumn>("Referenced column", getForeignKeyColumn()));
            }
        }

        if (isPrimaryKey()) {
            ObjectListProvider<DBColumn> objectListProvider = new ObjectListProvider<DBColumn>() {
                public List<DBColumn> getObjects() {
                    return getReferencingColumns();
                }
            };
            navigationLists.add(new DBObjectNavigationListImpl<DBColumn>("Foreign-key columns", objectListProvider));
        }
        return navigationLists;
    }

    @Override
    public String getPresentableTextConditionalDetails() {
        return getDataType().getQualifiedName();
    }

    @Override
    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = super.getPresentableProperties();

        if (isForeignKey()) properties.add(0, new DBObjectPresentableProperty("Foreign key column", getForeignKeyColumn(), true));

        StringBuilder attributes  = new StringBuilder();
        if (isPrimaryKey()) attributes.append("PK");
        if (isForeignKey()) attributes.append(" FK");
        if (!isPrimaryKey() && !isNullable()) attributes.append(" not null");

        if (attributes.length() > 0) {
            properties.add(0, new SimplePresentableProperty("Attributes", attributes.toString().trim()));
        }
        properties.add(0, new DBDataTypePresentableProperty(dataType));

        return properties;
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    private static final DynamicContentLoader CONSTRAINTS_LOADER = new DBObjectListFromRelationListLoader<DBConstraint>();
    private static final DynamicContentLoader INDEXES_LOADER = new DBObjectListFromRelationListLoader<DBIndex>();

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    public boolean isLeafTreeElement() {
        return true;
    }

    @NotNull
    public List<BrowserTreeElement> buildAllPossibleTreeChildren() {
        return BrowserTreeElement.EMPTY_LIST;
    }

    public int compareTo(Object o) {
        if (o instanceof DBColumn)  {
            DBColumn column = (DBColumn) o;
            if (getDataset().equals(column.getDataset())) {
                if (isPrimaryKey() && column.isPrimaryKey()) {
                    return super.compareTo(o);
                } else if (isPrimaryKey()) {
                    return -1;
                } else if (column.isPrimaryKey()){
                    return 1;
                } else {
                    return super.compareTo(o);
                }
            }
        }
        return super.compareTo(o);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
