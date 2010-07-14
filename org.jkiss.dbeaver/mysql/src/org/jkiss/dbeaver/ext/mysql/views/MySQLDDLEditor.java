package org.jkiss.dbeaver.ext.mysql.views;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ext.mysql.model.MySQLTable;
import org.jkiss.dbeaver.model.DBPObject;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.ui.editors.AbstractObjectEditor;

import java.lang.reflect.InvocationTargetException;

/**
 * MySQLDDLEditor
 */
public class MySQLDDLEditor extends AbstractObjectEditor
{
    static final Log log = LogFactory.getLog(MySQLDDLEditor.class);

    private Text ddlText;
    private MySQLTable table;

    public void createPartControl(Composite parent)
    {
        ddlText = new Text(parent, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP | SWT.BORDER);
        ddlText.setForeground(getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        ddlText.setBackground(getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    }

    public void activatePart()
    {
        final StringBuilder ddl = new StringBuilder();
        DBeaverCore.getInstance().runAndWait(true, true, new DBRRunnableWithProgress() {
            public void run(DBRProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException
            {
                try {
                    ddl.append(table.getDDL(monitor));
                }
                catch (DBException e) {
                    log.error("Can't obtain table DDL", e);
                }
            }
        });
        ddlText.setText(ddl.toString());
    }

    public void deactivatePart()
    {
    }

    public DBPObject getObject()
    {
        return table;
    }

    public void setObject(DBPObject object)
    {
        if (!(object instanceof MySQLTable)) {
            throw new IllegalArgumentException("object must be of type " + MySQLTable.class);
        }
        table = (MySQLTable)object;
    }

}