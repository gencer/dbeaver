/*
 * Copyright (C) 2010-2012 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ui.dialogs.connection;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.navigator.DBNDataSource;
import org.jkiss.dbeaver.model.navigator.DBNProject;
import org.jkiss.dbeaver.registry.DataSourceDescriptor;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.itemlist.ItemListControl;

import java.util.List;

/**
 * SelectDataSourceDialog
 *
 * @author Serge Rieder
 */
public class SelectDataSourceDialog extends Dialog {

    private DataSourceDescriptor dataSource = null;

    private SelectDataSourceDialog(Shell parentShell)
    {
        super(parentShell);
    }

    @Override
    protected boolean isResizable()
    {
        return true;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        getShell().setText(CoreMessages.dialog_select_datasource_title);

        Composite group = (Composite) super.createDialogArea(parent);
        GridData gd = new GridData(GridData.FILL_BOTH);
        group.setLayoutData(gd);

        DBeaverCore core = DBeaverCore.getInstance();
        DBNProject rootNode = core.getNavigatorModel().getRoot().getProject(core.getProjectRegistry().getActiveProject());

        ItemListControl dsList = new ItemListControl(
            group,
            SWT.BORDER | SWT.SHEET,
            null,
            rootNode.getDatabases(),
            null);
        dsList.createProgressPanel();
        gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 300;
        gd.widthHint = 500;
        dsList.setLayoutData(gd);
        //dsList.setLoadProperties(false);
        //dsList.setBrief(true);
        dsList.loadData();
        dsList.getNavigatorViewer().addSelectionChangedListener(new ISelectionChangedListener()
        {
            @Override
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection)event.getSelection();
                if (selection.isEmpty()) {
                    dataSource = null;
                    getButton(IDialogConstants.OK_ID).setEnabled(false);
                } else {
                    Object selNode = selection.getFirstElement();
                    if (selNode instanceof DBNDataSource) {
                        dataSource = ((DBNDataSource) selNode).getObject();
                        getButton(IDialogConstants.OK_ID).setEnabled(true);
                    } else {
                        dataSource = null;
                        getButton(IDialogConstants.OK_ID).setEnabled(false);
                    }
                }
            }
        });
        dsList.setDoubleClickHandler(new IDoubleClickListener()
        {
            @Override
            public void doubleClick(DoubleClickEvent event)
            {
                if (getButton(IDialogConstants.OK_ID).isEnabled()) {
                    okPressed();
                }
            }
        });

        return group;
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Control ctl = super.createContents(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        return ctl;
    }

    public DataSourceDescriptor getDataSource()
    {
        return dataSource;
    }

    public static DataSourceDescriptor selectDataSource(Shell parentShell)
    {
        List<DataSourceDescriptor> datasources = DBeaverCore.getInstance().getProjectRegistry().getActiveDataSourceRegistry().getDataSources();
        if (datasources.isEmpty()) {
            UIUtils.showMessageBox(parentShell, CoreMessages.dialog_select_datasource_error_title, CoreMessages.dialog_select_datasource_error_message, SWT.ICON_ERROR);
            return null;
        } else if (datasources.size() == 1) {
            return datasources.get(0);
        } else {
            SelectDataSourceDialog scDialog = new SelectDataSourceDialog(parentShell);
            if (scDialog.open() == IDialogConstants.OK_ID) {
                return scDialog.getDataSource();
            } else {
                return null;
            }
        }
    }

}