/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.actions.sql;

import org.jkiss.dbeaver.core.DBeaverActivator;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.jkiss.dbeaver.ui.DBIcon;

public class ValidateStatementAction extends AbstractSQLAction
{

    public ValidateStatementAction()
    {
        setImageDescriptor(DBIcon.SQL_VALIDATE.getImageDescriptor());
        setText("Validate statement");
    }

    protected void execute(SQLEditor editor)
    {

    }

}