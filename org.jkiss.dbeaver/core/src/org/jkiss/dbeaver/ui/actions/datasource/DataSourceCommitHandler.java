/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.actions.datasource;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.actions.DataSourceHandler;

import java.lang.reflect.InvocationTargetException;

public class DataSourceCommitHandler extends DataSourceHandler
{
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        DBSDataSourceContainer dataSourceContainer = getDataSourceContainer(event, true, false);
        if (dataSourceContainer != null && dataSourceContainer.isConnected()) {
            execute(HandlerUtil.getActiveShell(event), dataSourceContainer);
        }
        return null;
    }

    public static void execute(Shell shell, final DBSDataSourceContainer dataSourceContainer) {
        final DBPDataSource dataSource = dataSourceContainer.getDataSource();
        try {
            DBeaverCore.getInstance().runAndWait2(new DBRRunnableWithProgress() {
                public void run(DBRProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException
                {
                    DBCExecutionContext context = dataSource.openContext(monitor, DBCExecutionPurpose.UTIL, "Commit '" + dataSourceContainer.getName() + "' transaction");
                    try {
                        context.getTransactionManager().commit();
                    }
                    catch (DBCException e) {
                        throw new InvocationTargetException(e);
                    }
                    finally {
                        context.close();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            UIUtils.showErrorDialog(shell, "Commit", "Error while committing session", e);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

}