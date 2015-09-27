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

package org.jkiss.dbeaver.ext.mysql.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jkiss.dbeaver.ui.controls.PairListControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PrivilegesPairList
 */
public class PrivilegesPairList extends PairListControl<String> {

    public PrivilegesPairList(Composite parent) {
        super(parent, SWT.NONE, "Available", "Granted");
    }

    public void setModel(Map<String, Boolean> privs)
    {
        List<String> availPrivs = new ArrayList<String>();
        List<String> grantedPrivs = new ArrayList<String>();
        for (Map.Entry<String,Boolean> priv : privs.entrySet()) {
            if (priv.getValue()) {
                grantedPrivs.add(priv.getKey());
            } else {
                availPrivs.add(priv.getKey());
            }
        }
        super.setModel(availPrivs, grantedPrivs);
    }
}