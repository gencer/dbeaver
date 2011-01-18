/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.impl.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.navigator.DBNResource;
import org.jkiss.dbeaver.model.project.DBPResourceHandler;

/**
 * Abstract resource handler
 */
public abstract class AbstractResourceHandler implements DBPResourceHandler {

    public void initializeProject(IProject project) throws CoreException, DBException
    {
        // Just do nothing
    }

    public DBNNode makeNavigatorNode(DBNNode parentNode, IResource resource) throws CoreException, DBException
    {
        return new DBNResource(parentNode, resource);
    }

}
