/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.actions.navigator;

import net.sf.jkiss.utils.CommonUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.navigator.DBNContainer;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.EnterNameDialog;

import java.lang.reflect.InvocationTargetException;

public class NavigatorHandlerObjectRename extends NavigatorHandlerObjectBase {

    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        final ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structSelection = (IStructuredSelection) selection;
            Object element = structSelection.getFirstElement();
            if (element instanceof DBNNode) {
                renameNode(HandlerUtil.getActiveWorkbenchWindow(event), (DBNNode) element, null);
            }
        }
        return null;
    }

    public static boolean renameNode(IWorkbenchWindow workbenchWindow, final DBNNode node, String newName)
    {
        if (node instanceof DBNDatabaseNode) {
            if (renameDatabaseObject(
                workbenchWindow,
                (DBNDatabaseNode) node,
                null)) {
                return true;
            }
        }

        Shell activeShell = workbenchWindow.getShell();
        if (newName == null) {
            newName = EnterNameDialog.chooseName(activeShell, "Rename " + node.getNodeType(), node.getNodeName());
        }
        if (!CommonUtils.isEmpty(newName) && !newName.equals(node.getNodeName())) {
            try {
                // Rename with null monitor because it is some local resource
                node.rename(VoidProgressMonitor.INSTANCE, newName);
/*
                final String newNodeName = newName;
                DBeaverCore.getInstance().runInProgressService(new DBRRunnableWithProgress() {
                    public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try {
                            node.rename(monitor, newNodeName);
                        } catch (DBException e1) {
                            throw new InvocationTargetException(e1);
                        }
                    }
                });
*/
            } catch (DBException e) {
                UIUtils.showErrorDialog(activeShell, "Rename", "Can't rename object '" + node.getNodeName() + "'", e);
            }
        }
        return true;
    }

    public static boolean renameDatabaseObject(IWorkbenchWindow workbenchWindow, DBNDatabaseNode node, String newName)
    {
        try {
            if (node.getParentNode() instanceof DBNContainer) {
                final DBNContainer container = (DBNContainer) node.getParentNode();
                DBSObject object = node.getObject();
                if (object != null) {
                    DBEObjectRenamer objectRenamer = DBeaverCore.getInstance().getEditorsRegistry().getObjectManager(object.getClass(), DBEObjectRenamer.class);
                    if (objectRenamer != null) {
                        CommandTarget commandTarget = getCommandTarget(
                            workbenchWindow,
                            container,
                            object.getClass(),
                            false);

                        objectRenamer.renameObject(commandTarget.getContext(), object, newName);
                        if (object.isPersisted() && commandTarget.getEditor() == null) {
                            if (!showScript(workbenchWindow, commandTarget.getContext(), "Rename script")) {
                                commandTarget.getContext().resetChanges();
                                return false;
                            } else {
                                ObjectSaver deleter = new ObjectSaver(commandTarget.getContext());
                                DBeaverCore.getInstance().runInProgressService(deleter);
                            }
                        }
                        return true;
                    }
                }
            }
        } catch (InterruptedException e) {
            // do nothing
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException)e).getTargetException();
            }
            UIUtils.showErrorDialog(workbenchWindow.getShell(), "Rename object", "Can't rename object '" + node.getNodeName() + "'", e);
            return false;
        }
        return false;
    }

}