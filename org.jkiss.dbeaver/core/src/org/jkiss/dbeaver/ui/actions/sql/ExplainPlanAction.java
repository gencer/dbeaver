/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.actions.sql;

import org.jkiss.dbeaver.core.DBeaverActivator;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.jkiss.dbeaver.ui.DBIcon;

public class ExplainPlanAction extends AbstractSQLAction
{

    public ExplainPlanAction()
    {
        setImageDescriptor(DBIcon.SQL_EXPLAIN_PLAN.getImageDescriptor());
        setText("Explain plan");
    }

    protected void execute(SQLEditor editor)
    {

    }

}