/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.editors.entity;

import net.sf.jkiss.utils.CommonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ext.IDatabasePersistAction;
import org.jkiss.dbeaver.ext.ui.IDatabaseObjectEditor;
import org.jkiss.dbeaver.ext.ui.INavigatorModelView;
import org.jkiss.dbeaver.ext.ui.IRefreshablePart;
import org.jkiss.dbeaver.model.edit.DBOCommand;
import org.jkiss.dbeaver.model.edit.DBOEditor;
import org.jkiss.dbeaver.model.edit.DBOManager;
import org.jkiss.dbeaver.model.navigator.*;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.registry.EntityEditorDescriptor;
import org.jkiss.dbeaver.registry.EntityEditorsRegistry;
import org.jkiss.dbeaver.registry.EntityManagerDescriptor;
import org.jkiss.dbeaver.registry.tree.DBXTreeItem;
import org.jkiss.dbeaver.registry.tree.DBXTreeNode;
import org.jkiss.dbeaver.runtime.DefaultProgressMonitor;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.ConfirmationDialog;
import org.jkiss.dbeaver.ui.dialogs.ViewSQLDialog;
import org.jkiss.dbeaver.ui.editors.MultiPageDatabaseEditor;
import org.jkiss.dbeaver.ui.preferences.PrefConstants;
import org.jkiss.dbeaver.ui.views.properties.PropertyPageTabbed;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * EntityEditor
 */
public class EntityEditor extends MultiPageDatabaseEditor<EntityEditorInput> implements INavigatorModelView, ISaveablePart2
{
    static final Log log = LogFactory.getLog(EntityEditor.class);

    private static Map<Class<?>, String> defaultPageMap = new HashMap<Class<?>, String>();

    private DBOManager objectManager;
    private Map<String, IEditorPart> editorMap = new HashMap<String, IEditorPart>();

    public DBOManager<?> getObjectManager()
    {
        return objectManager;
    }

    public DBOEditor<?> getObjectEditor()
    {
        return objectManager instanceof DBOEditor ? (DBOEditor<?>) objectManager : null;
    }

    @Override
    public void dispose()
    {
        if (objectManager != null && objectManager.getObject() != null && !objectManager.getObject().isPersisted()) {
            // If edited object is still not persisted then remove object's node
            DBNNode treeNode = getEditorInput().getTreeNode();
            if (treeNode instanceof DBNDatabaseItem) {
                DBNNode parentNode = treeNode.getParentNode();
                if (parentNode instanceof DBNDatabaseFolder) {
                    try {
                        ((DBNDatabaseFolder)parentNode).removeChildItem(treeNode);
                    } catch (DBException e) {
                        log.error(e);
                    }
                }
            }
        }
        objectManager = null;
        super.dispose();
    }

    @Override
    public boolean isDirty()
    {
        return objectManager instanceof DBOEditor && ((DBOEditor) objectManager).isDirty();
    }

    /**
     * Saves data in all nested editors
     * @param monitor progress monitor
     */
    public void doSave(IProgressMonitor monitor)
    {
        if (!isDirty()) {
            return;
        }

        monitor.beginTask("Preview changes", 1);
        int previewResult = showChanges(true);
        monitor.done();

        if (previewResult == IDialogConstants.PROCEED_ID) {
            Throwable error = null;
            try {
                getObjectEditor().saveChanges(new DefaultProgressMonitor(monitor));
            } catch (DBException e) {
                error = e;
            }
            final Throwable showError = error;
            Display.getDefault().asyncExec(new Runnable() {
                public void run()
                {
                    if (showError != null) {
                        UIUtils.showErrorDialog(getSite().getShell(), "Could not save '" + objectManager.getObject().getName() + "'", null, showError);
                    }
                    firePropertyChange(IEditorPart.PROP_DIRTY);
                }
            });
        }
    }

    public void revertChanges()
    {
        if (isDirty()) {
            getObjectEditor().resetChanges();
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    public void undoChanges()
    {
        if (getObjectEditor() != null && getObjectEditor().canUndoCommand()) {
            getObjectEditor().undoCommand();
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    public void redoChanges()
    {
        if (getObjectEditor() != null && getObjectEditor().canRedoCommand()) {
            getObjectEditor().redoCommand();
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    public int showChanges(boolean allowSave)
    {
        if (getObjectEditor() == null) {
            return IDialogConstants.CANCEL_ID;
        }
        Collection<? extends DBOCommand> commands = getObjectEditor().getCommands();
        StringBuilder script = new StringBuilder();
        for (DBOCommand command : commands) {
            try {
                command.validateCommand(getObjectManager().getObject());
            } catch (DBException e) {
                UIUtils.showErrorDialog(getSite().getShell(), "Validation", e.getMessage());
                return IDialogConstants.CANCEL_ID;
            }
            IDatabasePersistAction[] persistActions = command.getPersistActions(getObjectManager().getObject());
            if (!CommonUtils.isEmpty(persistActions)) {
                for (IDatabasePersistAction action : persistActions) {
                    if (script.length() > 0) {
                        script.append('\n');
                    }
                    script.append(action.getScript());
                    script.append(getObjectManager().getDataSource().getInfo().getScriptDelimiter());
                }
            }
        }

        ChangesPreviewer changesPreviewer = new ChangesPreviewer(script, allowSave);
        getSite().getShell().getDisplay().syncExec(changesPreviewer);
        return changesPreviewer.getResult();
/*

        Shell shell = getSite().getShell();
        ViewTextDialog dialog = new ViewTextDialog(shell, "Script", script.toString());
        dialog.setTextWidth(0);
        dialog.setTextHeight(0);
        dialog.setImage(DBIcon.SQL_PREVIEW.getImage());
        dialog.open();
*/
    }

    protected void createPages()
    {
        addPropertyListener(new IPropertyListener() {
            public void propertyChanged(Object source, int propId)
            {
                if (propId == IEditorPart.PROP_DIRTY) {
                    EntityEditorPropertyTester.firePropertyChange(EntityEditorPropertyTester.PROP_DIRTY);
                    EntityEditorPropertyTester.firePropertyChange(EntityEditorPropertyTester.PROP_CAN_UNDO);
                    EntityEditorPropertyTester.firePropertyChange(EntityEditorPropertyTester.PROP_CAN_REDO);
                }
            }
        });

        super.createPages();

        EntityEditorsRegistry editorsRegistry = DBeaverCore.getInstance().getEditorsRegistry();
        DBSObject databaseObject = getEditorInput().getDatabaseObject();

        this.objectManager = getEditorInput().getObjectManager();

        if (this.objectManager == null) {
            // Instantiate new object manager
            EntityManagerDescriptor managerDescriptor = editorsRegistry.getEntityManager(databaseObject.getClass());
            if (managerDescriptor != null) {
                this.objectManager = managerDescriptor.createManager();
                if (this.objectManager == null) {
                    log.warn("Could not instantiate object manager '" + managerDescriptor.getName() + "'");
                }
            }
            if (this.objectManager == null) {
                // Create default object manager
                this.objectManager = new DefaultDatabaseObjectManager();
            }

            this.objectManager.setObject(databaseObject);
        }

        // Add object editor page
        EntityEditorDescriptor defaultEditor = editorsRegistry.getMainEntityEditor(databaseObject.getClass());
        boolean mainAdded = false;
        if (defaultEditor != null) {
            mainAdded = addEditorTab(defaultEditor);
        }
        if (mainAdded) {
            DBNNode node = getEditorInput().getTreeNode();
            setPageText(0, "Properties");
            setPageToolTip(0, node.getNodeType() + " Properties");
            setPageImage(0, node.getNodeIconDefault());
        }
/*
        if (!mainAdded) {
            try {
                DBNNode node = getEditorInput().getTreeNode();
                int index = addPage(new DefaultObjectEditor(node), getEditorInput());
                setPageText(index, "Properties");
                if (node instanceof DBNDatabaseNode) {
                    setPageToolTip(index, ((DBNDatabaseNode)node).getMeta().getLabel() + " Properties");
                }
                setPageImage(index, node.getNodeIconDefault());
            } catch (PartInitException e) {
                log.error("Error creating object editor");
            }
        }
*/

        // Add contributed pages
        addContributions(EntityEditorDescriptor.POSITION_START);

        final List<TabInfo> tabs = new ArrayList<TabInfo>();
        DBRRunnableWithProgress tabsCollector = new DBRRunnableWithProgress() {
            public void run(DBRProgressMonitor monitor)
            {
                tabs.addAll(collectTabs(monitor));
            }
        };
        DBNDatabaseNode node = getEditorInput().getTreeNode();
        try {
            if (node.isLazyNode()) {
                DBeaverCore.getInstance().runAndWait2(tabsCollector);
            } else {
                tabsCollector.run(VoidProgressMonitor.INSTANCE);
            }
        } catch (InvocationTargetException e) {
            log.error(e.getTargetException());
        } catch (InterruptedException e) {
            // just go further
        }

        //tabs.addAll(collectTabs(VoidProgressMonitor.INSTANCE));

        for (TabInfo tab : tabs) {
            if (tab.meta == null) {
                addNodeTab(tab.node);
            } else {
                addNodeTab(tab.node, tab.meta);
            }
        }

        // Add contributed pages
        addContributions(EntityEditorDescriptor.POSITION_END);

        String defPageId = getEditorInput().getDefaultPageId();
        if (defPageId == null) {
            defPageId = defaultPageMap.get(getEditorInput().getDatabaseObject().getClass()); 
        }
        if (defPageId != null) {
            IEditorPart defEditorPage = editorMap.get(defPageId);
            if (defEditorPage != null) {
                setActiveEditor(defEditorPage);
            }
        }
    }

    @Override
    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);

        DBSObject object = getEditorInput().getDatabaseObject();
        IEditorPart editor = getEditor(newPageIndex);
        for (Map.Entry<String,IEditorPart> entry : editorMap.entrySet()) {
            if (entry.getValue() == editor) {
                defaultPageMap.put(object.getClass(), entry.getKey());
                break;
            }
        }
    }

    public int promptToSaveOnClose()
    {
        final int result = ConfirmationDialog.showConfirmDialog(
            getSite().getShell(),
            PrefConstants.CONFIRM_ENTITY_EDIT_CLOSE,
            ConfirmationDialog.QUESTION_WITH_CANCEL,
            getObjectManager().getObject().getName());
        if (result == IDialogConstants.YES_ID) {
//            getWorkbenchPart().getSite().getPage().saveEditor(this, false);
            return ISaveablePart2.YES;
        } else if (result == IDialogConstants.NO_ID) {
            return ISaveablePart2.NO;
        } else {
            return ISaveablePart2.CANCEL;
        }
    }

    private static class TabInfo {
        DBNNode node;
        DBXTreeNode meta;
        private TabInfo(DBNNode node)
        {
            this.node = node;
        }
        private TabInfo(DBNNode node, DBXTreeNode meta)
        {
            this.node = node;
            this.meta = meta;
        }
    }

    private List<TabInfo> collectTabs(DBRProgressMonitor monitor)
    {
        List<TabInfo> tabs = new ArrayList<TabInfo>();

        // Add all nested folders as tabs
        DBNNode node = getEditorInput().getTreeNode();
        if (node instanceof DBNDataSource && !((DBNDataSource)node).getDataSourceContainer().isConnected()) {
            // Do not add children tabs
        } else if (node != null) {
            try {
                List<? extends DBNNode> children = node.getChildren(monitor);
                if (children != null) {
                    for (DBNNode child : children) {
                        if (child instanceof DBNDatabaseFolder) {
                            monitor.subTask("Add folder '" + child.getNodeName() + "'");
                            tabs.add(new TabInfo(child));
                        }
                    }
                }
            } catch (DBException e) {
                log.error("Error initializing entity editor");
            }
            // Add itself as tab (if it has child items)
            if (node instanceof DBNDatabaseNode) {
                DBNDatabaseNode databaseNode = (DBNDatabaseNode)node;
                List<DBXTreeNode> subNodes = databaseNode.getMeta().getChildren();
                if (subNodes != null) {
                    for (DBXTreeNode child : subNodes) {
                        if (child instanceof DBXTreeItem) {
                            try {
                                if (!((DBXTreeItem)child).isOptional() || databaseNode.hasChildren(monitor, child)) {
                                    monitor.subTask("Add node '" + node.getNodeName() + "'");
                                    tabs.add(new TabInfo(node, child));
                                }
                            } catch (DBException e) {
                                log.debug("Can't add child items tab", e);
                            }
                        }
                    }
                }
            }
        }
        return tabs;
    }

    private void addContributions(String position)
    {
        EntityEditorsRegistry editorsRegistry = DBeaverCore.getInstance().getEditorsRegistry();
        List<EntityEditorDescriptor> descriptors = editorsRegistry.getEntityEditors(getEditorInput().getDatabaseObject().getClass(), position);
        for (EntityEditorDescriptor descriptor : descriptors) {
            addEditorTab(descriptor);
        }
    }

    private boolean addEditorTab(EntityEditorDescriptor descriptor)
    {
        try {
            IEditorPart editor = descriptor.createEditor();
            if (editor == null) {
                return false;
            }
            Object object = this.objectManager.getObject();
            if (editor instanceof IDatabaseObjectEditor) {
                try {
                    Method initMethod = editor.getClass().getMethod("initObjectEditor", DBOManager.class);
                    Type initParam = initMethod.getGenericParameterTypes()[0];
                    if (initParam instanceof ParameterizedType) {
                        Type typeArgument = ((ParameterizedType) initParam).getActualTypeArguments()[0];
                        if (typeArgument instanceof Class && !((Class<?>) typeArgument).isAssignableFrom(object.getClass())) {
                            // Bad parameter type
                            log.error(descriptor.getName() + " editor misconfiguration - invalid object type '" + object.getClass().getName() + "' specified while '" + ((Class<?>) typeArgument).getName() + "' was expected");
                            return false;
                        }
                    }
                } catch (Throwable e) {
                    log.error(e);
                    return false;
                }
                ((IDatabaseObjectEditor)editor).initObjectEditor(this.objectManager);
            }
            int index = addPage(editor, getEditorInput());
            setPageText(index, descriptor.getName());
            if (descriptor.getIcon() != null) {
                setPageImage(index, descriptor.getIcon());
            }
            if (!CommonUtils.isEmpty(descriptor.getDescription())) {
                setPageToolTip(index, descriptor.getDescription());
            }
            editorMap.put(descriptor.getId(), editor);
            return true;
        } catch (Exception ex) {
            log.error("Error adding nested editor", ex);
            return false;
        }
    }

    private void addNodeTab(DBNNode node)
    {
        try {
            EntityNodeEditor nodeEditor = new EntityNodeEditor(node);
            int index = addPage(nodeEditor, getEditorInput());
            setPageText(index, node.getNodeName());
            setPageImage(index, node.getNodeIconDefault());
            DBNNode editorNode = getEditorInput().getTreeNode();
            setPageToolTip(index, editorNode.getNodeType() + " " + node.getNodeName());
            editorMap.put("node." + node.getNodeName(), nodeEditor);
        } catch (PartInitException ex) {
            log.error("Error adding nested editor", ex);
        }
    }

    private void addNodeTab(DBNNode node, DBXTreeNode metaItem)
    {
        try {
            EntityNodeEditor nodeEditor = new EntityNodeEditor(node, metaItem);
            int index = addPage(nodeEditor, getEditorInput());
            setPageText(index, metaItem.getLabel());
            if (metaItem.getDefaultIcon() != null) {
                setPageImage(index, metaItem.getDefaultIcon());
            } else {
                setPageImage(index, PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
            }
            setPageToolTip(index, metaItem.getLabel());
            editorMap.put("node." + node.getNodeName(), nodeEditor);
        } catch (PartInitException ex) {
            log.error("Error adding nested editor", ex);
        }
    }

    public void refreshDatabaseContent(final DBNEvent event)
    {
        // Reinit object manager
        final boolean persisted = objectManager.getObject().isPersisted();
        if (objectManager != null) {
            if (persisted) {
                this.objectManager.setObject(((DBNDatabaseNode)event.getNode()).getObject());
            }
        }
        // Refresh visual content in parts
        getSite().getShell().getDisplay().asyncExec(new Runnable() { public void run() {
            if (persisted) {
                int pageCount = getPageCount();
                for (int i = 0; i < pageCount; i++) {
                    IWorkbenchPart part = getEditor(i);
                    if (part instanceof IRefreshablePart) {
                        ((IRefreshablePart)part).refreshPart(event);
                    }
                }
            }
            setPartName(getEditorInput().getName());
            setTitleImage(getEditorInput().getImageDescriptor());
        }});
    }

    public DBNNode getRootNode() {
        return getEditorInput().getTreeNode();
    }

    public Viewer getNavigatorViewer()
    {
        IWorkbenchPart activePart = getActiveEditor();
        if (activePart instanceof INavigatorModelView) {
            return ((INavigatorModelView)activePart).getNavigatorViewer();
        }
        return null;
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySheetPage.class) {
            return new PropertyPageTabbed();
        }/* else if (adapter == INavigatorModelView.class) {
            IWorkbenchPart activePart = getActiveEditor();
            if (activePart instanceof INavigatorModelView) {
                return activePart;
            }
        }*/
        return super.getAdapter(adapter);
    }

    private class ChangesPreviewer implements Runnable {

        private final StringBuilder script;
        private final boolean allowSave;
        private int result;

        public ChangesPreviewer(StringBuilder script, boolean allowSave)
        {
            this.script = script;
            this.allowSave = allowSave;
        }

        public void run()
        {
            ViewSQLDialog dialog = new ViewSQLDialog(
                getEditorSite(),
                getDataSource(),
                allowSave ? "Persist Changes" : "Preview Changes", 
                script.toString());
            dialog.setShowSaveButton(allowSave);
            dialog.setImage(DBIcon.SQL_PREVIEW.getImage());
            result = dialog.open();
        }

        public int getResult()
        {
            return result;
        }
    }
}
