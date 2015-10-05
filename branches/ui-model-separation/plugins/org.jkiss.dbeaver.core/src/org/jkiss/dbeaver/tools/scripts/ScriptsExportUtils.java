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

package org.jkiss.dbeaver.tools.scripts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.impl.resources.ScriptsHandlerImpl;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.navigator.DBNProject;
import org.jkiss.dbeaver.model.navigator.DBNResource;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;

/**
 * Utils
 */
class ScriptsExportUtils {

    static final Log log = LogFactory.getLog(ScriptsExportUtils.class);

    static DBNNode getScriptsNode()
    {
        final IProject activeProject = DBeaverCore.getInstance().getProjectRegistry().getActiveProject();
        final DBNProject projectNode = DBeaverCore.getInstance().getNavigatorModel().getRoot().getProject(activeProject);
        DBNNode scriptsNode = projectNode;
        final IFolder scriptsFolder;
        try {
            scriptsFolder = ScriptsHandlerImpl.getScriptsFolder(activeProject, false);
        } catch (CoreException e) {
            log.error(e);
            return scriptsNode;
        }
        if (!scriptsFolder.exists()) {
            return scriptsNode;
        }
        try {
            for (DBNNode projectFolder : projectNode.getChildren(VoidProgressMonitor.INSTANCE)) {
                if (projectFolder instanceof DBNResource && ((DBNResource) projectFolder).getResource().equals(scriptsFolder)) {
                    scriptsNode = projectFolder;
                    break;
                }
            }
        } catch (DBException e) {
            log.error(e);
        }
        return scriptsNode;
    }

}