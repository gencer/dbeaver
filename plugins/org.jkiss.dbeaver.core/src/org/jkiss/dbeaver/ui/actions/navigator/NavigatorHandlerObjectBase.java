/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.actions.navigator;

import net.sf.jkiss.utils.CommonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ext.IDatabaseNodeEditor;
import org.jkiss.dbeaver.ext.IDatabaseNodeEditorInput;
import org.jkiss.dbeaver.ext.IDatabasePersistAction;
import org.jkiss.dbeaver.ext.ui.IFolderedPart;
import org.jkiss.dbeaver.model.edit.DBECommand;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEStructEditor;
import org.jkiss.dbeaver.model.impl.edit.DBECommandContextImpl;
import org.jkiss.dbeaver.model.navigator.DBNContainer;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseFolder;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNModel;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.dialogs.ViewSQLDialog;
import org.jkiss.dbeaver.ui.views.navigator.database.DatabaseNavigatorView;
import org.jkiss.dbeaver.utils.ViewUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public abstract class NavigatorHandlerObjectBase extends AbstractHandler {

    static final Log log = LogFactory.getLog(NavigatorHandlerObjectBase.class);

    protected static class CommandTarget {
        private DBECommandContext context;
        private IDatabaseNodeEditor editor;
        private IDatabaseNodeEditorInput editorInput;
        private CommandTarget()
        {
        }
        private CommandTarget(DBECommandContextImpl context)
        {
            this.context = context;
        }
        public CommandTarget(IDatabaseNodeEditor editor)
        {
            this.editor = editor;
            this.context = editor.getEditorInput().getCommandContext();
        }

        public DBECommandContext getContext()
        {
            return context;
        }
        public IDatabaseNodeEditor getEditor()
        {
            return editor;
        }
        public IDatabaseNodeEditorInput getEditorInput()
        {
            return editorInput;
        }
    }

    protected static CommandTarget getCommandTarget(
        IWorkbenchWindow workbenchWindow,
        DBNContainer container,
        Class<?> childType,
        boolean openEditor)
        throws DBException
    {
        final Object parentObject = container.getValueObject();

        DBSObject objectToSeek = null;
        if (parentObject instanceof DBSObject) {
            final DBEStructEditor parentStructEditor = DBeaverCore.getInstance().getEditorsRegistry().getObjectManager(parentObject.getClass(), DBEStructEditor.class);
            if (parentStructEditor != null && RuntimeUtils.isTypeSupported(childType, parentStructEditor.getChildTypes())) {
                objectToSeek = (DBSObject) parentObject;
            }
        }
        if (objectToSeek != null) {
            for (final IEditorReference editorRef : workbenchWindow.getActivePage().getEditorReferences()) {
                final IEditorPart editor = editorRef.getEditor(false);
                if (editor instanceof IDatabaseNodeEditor) {
                    final IDatabaseNodeEditorInput editorInput = ((IDatabaseNodeEditor) editor).getEditorInput();
                    if (editorInput.getDatabaseObject() == objectToSeek) {
                        workbenchWindow.getActivePage().activate(editor);
                        switchEditorFolder(container, editor);
                        return new CommandTarget((IDatabaseNodeEditor) editor);
                    }
                }
            }

            if (openEditor && container instanceof DBNDatabaseNode) {
                final IDatabaseNodeEditor editor = (IDatabaseNodeEditor) NavigatorHandlerObjectOpen.openEntityEditor(
                    (DBNDatabaseNode) container,
                    null,
                    workbenchWindow);
                if (editor != null) {
                    switchEditorFolder(container, editor);
                    return new CommandTarget(editor);
                }
            }
        }
        if (container instanceof DBNDatabaseNode) {
            // No editor found and no need to create one - create new command context
            DBSDataSourceContainer dsContainer = ((DBNDatabaseNode) container).getObject().getDataSource().getContainer();
            return new CommandTarget(new DBECommandContextImpl(dsContainer));
        } else {
            return new CommandTarget();
        }
    }

    private static void switchEditorFolder(DBNContainer container, IEditorPart editor)
    {
        if (editor instanceof IFolderedPart && container instanceof DBNDatabaseFolder) {
            ((IFolderedPart) editor).switchFolder(((DBNDatabaseFolder) container).getMeta().getLabel());
        }
    }

    public static DBNDatabaseNode getNodeByObject(DBSObject object)
    {
        DBNModel model = DBeaverCore.getInstance().getNavigatorModel();
        DBNDatabaseNode node = model.findNode(object);
        if (node == null) {
            NodeLoader nodeLoader = new NodeLoader(model, object);
            try {
                DBeaverCore.getInstance().runInProgressService(nodeLoader);
            } catch (InvocationTargetException e) {
                log.warn("Could not load node for object '" + object.getName() + "'", e.getTargetException());
            } catch (InterruptedException e) {
                // do nothing
            }
            node = nodeLoader.node;
        }
        return node;
    }

    protected static boolean showScript(IWorkbenchWindow workbenchWindow, DBECommandContext commandContext, String dialogTitle)
    {
        Collection<? extends DBECommand> commands = commandContext.getFinalCommands();
        StringBuilder script = new StringBuilder();
        for (DBECommand command : commands) {
            IDatabasePersistAction[] persistActions = command.getPersistActions();
            if (!CommonUtils.isEmpty(persistActions)) {
                for (IDatabasePersistAction action : persistActions) {
                    if (script.length() > 0) {
                        script.append('\n');
                    }
                    script.append(action.getScript());
                    script.append(commandContext.getDataSourceContainer().getDataSource().getInfo().getScriptDelimiter());
                }
            }
        }
        DatabaseNavigatorView view = ViewUtils.findView(workbenchWindow, DatabaseNavigatorView.class);
        if (view != null) {
            ViewSQLDialog dialog = new ViewSQLDialog(
                view.getSite(),
                commandContext.getDataSourceContainer(),
                dialogTitle,
                script.toString());
            dialog.setImage(DBIcon.SQL_PREVIEW.getImage());
            dialog.setShowSaveButton(true);
            return dialog.open() == IDialogConstants.PROCEED_ID;
        } else {
            return false;
        }
    }

    private static class NodeLoader implements DBRRunnableWithProgress {
        private final DBNModel model;
        private final DBSObject object;
        private DBNDatabaseNode node;

        public NodeLoader(DBNModel model, DBSObject object)
        {
            this.model = model;
            this.object = object;
        }

        public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
        {
            node = model.getNodeByObject(monitor, object, true);
        }
    }

    protected static class ObjectSaver implements DBRRunnableWithProgress {
        private final DBECommandContext commander;

        public ObjectSaver(DBECommandContext commandContext)
        {
            this.commander = commandContext;
        }

        public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
        {
            try {
                commander.saveChanges(monitor);
            } catch (DBException e) {
                throw new InvocationTargetException(e);
            }
        }
    }

}