/*
 * Copyright (C) 2010-2012 Serge Rieder serge@jkiss.org
 * Copyright (C) 2011-2012 Eugene Fradkin eugene.fradkin@gmail.com
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
package org.jkiss.dbeaver.ext.mysql.tools;

import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.mysql.model.MySQLCatalog;
import org.jkiss.dbeaver.model.DBPObject;
import org.jkiss.dbeaver.tools.IExternalTool;
import org.jkiss.dbeaver.ui.dialogs.tools.ToolWizardDialog;

/**
 * Database import
 */
public class MySQLToolImport implements IExternalTool
{
    @Override
    public void execute(IWorkbenchWindow window, DBPObject object) throws DBException
    {
        if (object instanceof MySQLCatalog) {
            ToolWizardDialog dialog = new ToolWizardDialog(
                window,
                new MySQLScriptExecuteWizard((MySQLCatalog) object, true));
            dialog.open();
        }
    }
}