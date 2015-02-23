/*
 * Copyright (C) 2010-2015 Serge Rieder
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
package org.jkiss.dbeaver.ui.views.navigator.database.load;

import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.navigator.DBNContainer;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.runtime.load.DatabaseLoadService;
import org.jkiss.dbeaver.DBeaverPreferences;
import org.jkiss.utils.CommonUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TreeLoadService
 */
public class TreeLoadService extends DatabaseLoadService<Object[]> {

    private DBNNode parentNode;

    public TreeLoadService(String serviceName, DBNDatabaseNode parentNode)
    {
        super(serviceName, parentNode);
        this.parentNode = parentNode;
    }

    public DBNNode getParentNode() {
        return parentNode;
    }

    @Override
    public Object[] evaluate()
        throws InvocationTargetException, InterruptedException
    {
        try {
            List<? extends DBNNode> children = filterNavigableChildren(
                parentNode.getChildren(getProgressMonitor()));
            return CommonUtils.isEmpty(children) ? new Object[0] : children.toArray(); 
        } catch (Throwable ex) {
            if (ex instanceof InvocationTargetException) {
                throw (InvocationTargetException)ex;
            } else {
                throw new InvocationTargetException(ex);
            }
        }
    }

    public static List<? extends DBNNode> filterNavigableChildren(List<? extends DBNNode> children)
    {
        if (CommonUtils.isEmpty(children)) {
            return children;
        }
        List<DBNNode> filtered = null;
        for (int i = 0; i < children.size(); i++) {
            DBNNode node = children.get(i);
            if (node instanceof DBNDatabaseNode && !((DBNDatabaseNode) node).getMeta().isNavigable()) {
                if (filtered == null) {
                    filtered = new ArrayList<DBNNode>(children.size());
                    for (int k = 0; k < i; k++) {
                        filtered.add(children.get(k));
                    }
                }
            } else if (filtered != null) {
                filtered.add(node);
            }
        }
        List<? extends DBNNode> result = filtered == null ? children : filtered;
        if (!result.isEmpty()) {
            sortChildren(result);
        }
        return result;
    }

    public static void sortChildren(List<? extends DBNNode> children)
    {
        // Sort children is we have this feature on in preferences
        // and if children are not folders
        if (!children.isEmpty() && DBeaverCore.getGlobalPreferenceStore().getBoolean(DBeaverPreferences.NAVIGATOR_SORT_ALPHABETICALLY)) {
            if (!(children.get(0) instanceof DBNContainer)) {
                Collections.sort(children, new Comparator<DBNNode>() {
                    @Override
                    public int compare(DBNNode node1, DBNNode node2)
                    {
                        return node1.getNodeName().compareToIgnoreCase(node2.getNodeName());
                    }
                });
            }
        }
    }

}
