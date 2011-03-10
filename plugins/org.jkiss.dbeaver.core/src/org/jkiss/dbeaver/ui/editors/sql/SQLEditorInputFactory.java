/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.editors.sql;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class SQLEditorInputFactory implements IElementFactory
{
    //static final Log log = LogFactory.getLog(SQLEditorInputFactory.class);

    private static final String ID_FACTORY = SQLEditorInputFactory.class.getName(); //$NON-NLS-1$

    private static final String TAG_PATH = "path"; //$NON-NLS-1$
    //private static final String TAG_NAME = "name"; //$NON-NLS-1$
    //private static final String TAG_DATA_SOURCE = "data-source"; //$NON-NLS-1$

    public SQLEditorInputFactory()
    {
    }

    public IAdaptable createElement(IMemento memento)
    {
        // Get the file name.
        String fileName = memento.getString(TAG_PATH);
        if (fileName == null) {
            return null;
        }

        // Get a handle to the IFile...which can be a handle
        // to a resource that does not exist in workspace
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileName));
        if (file != null) {
/*
            DataSourceDescriptor dataSource = null;
            DataSourceRegistry registry = DBeaverCore.getInstance().getProjectRegistry().getDataSourceRegistry(file.getProject());
            if (registry != null) {
                String dataSourceId = memento.getString(TAG_DATA_SOURCE);
                if (dataSourceId != null) {
                    dataSource = registry.getDataSource(dataSourceId);
                    if (dataSource == null) {
                        log.warn("Can't find datasource '" + dataSourceId + "' for file '" + fileName + "'");
                    }
                }
            }
*/
            return new SQLEditorInput(file);
        }
        return null;
    }

    public static String getFactoryId()
    {
        return ID_FACTORY;
    }

    public static void saveState(IMemento memento, SQLEditorInput input)
    {
        IFile file = input.getFile();
        memento.putString(TAG_PATH, file.getFullPath().toString());
        //memento.putString(TAG_NAME, input.getScriptName());
        //if (input.getDataSourceContainer() != null) {
        //    memento.putString(TAG_DATA_SOURCE, input.getDataSourceContainer().getId());
        //}
    }

}