/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.mysql.editors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ext.mysql.model.MySQLTable;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.ui.editors.AbstractDatabaseObjectEditor;

import java.lang.reflect.InvocationTargetException;

/**
 * MySQLDDLEditor
 */
public class MySQLDDLEditor extends AbstractDatabaseObjectEditor<MySQLTable>
{
    static final Log log = LogFactory.getLog(MySQLDDLEditor.class);

    private Text ddlText;

    public void createPartControl(Composite parent)
    {
        ddlText = new Text(parent, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP | SWT.BORDER);
        ddlText.setForeground(getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        ddlText.setBackground(getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    }

    @Override
    public void setFocus()
    {
        ddlText.setFocus();
    }

    public void activatePart()
    {
        if (!ddlText.isDisposed() && ddlText.isVisible()) {
            final StringBuilder ddl = new StringBuilder();
            try {
                DBeaverCore.getInstance().runInProgressService(new DBRRunnableWithProgress() {
                    public void run(DBRProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException
                    {
                        try {
                            ddl.append(getDatabaseObject().getDDL(monitor));
                        } catch (DBException e) {
                            throw new InvocationTargetException(e);
                        }
                    }
                });
            } catch (InvocationTargetException e) {
                log.error("Can't obtain table DDL", e.getTargetException());
            } catch (InterruptedException e) {
                // skip
            }
            ddlText.setText(ddl.toString());
        }
    }

    public void refreshPart(Object source)
    {
        activatePart();
    }
}