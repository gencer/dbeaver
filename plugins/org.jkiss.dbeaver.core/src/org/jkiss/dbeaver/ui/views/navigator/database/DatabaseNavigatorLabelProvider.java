/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.views.navigator.database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.navigator.DBNProject;
import org.jkiss.dbeaver.model.struct.DBSObjectSelector;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSWrapper;
import org.jkiss.dbeaver.ui.UIUtils;

/**
 * DatabaseNavigatorLabelProvider
*/
class DatabaseNavigatorLabelProvider extends LabelProvider implements IFontProvider, IColorProvider
{
    static final Log log = LogFactory.getLog(DatabaseNavigatorLabelProvider.class);

    //private DatabaseNavigatorView view;
    private Font normalFont;
    private Font boldFont;
    private Font italicFont;
    private Font boldItalicFont;
    private Color lockedForeground;
    private Color transientForeground;

    DatabaseNavigatorLabelProvider(Viewer viewer)
    {
        //this.view = view;
        this.normalFont = viewer.getControl().getFont();
        this.boldFont = UIUtils.makeBoldFont(normalFont);
        this.italicFont = UIUtils.modifyFont(normalFont, SWT.ITALIC);
        this.boldItalicFont = UIUtils.modifyFont(normalFont, SWT.BOLD | SWT.ITALIC);
        this.lockedForeground = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
        this.transientForeground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
    }

    @Override
    public void dispose()
    {
        UIUtils.dispose(boldFont);
        boldFont = null;
        UIUtils.dispose(italicFont);
        italicFont = null;
        UIUtils.dispose(boldItalicFont);
        boldItalicFont = null;
        super.dispose();
    }

    @Override
    public String getText(Object obj)
    {
        String text;
        if (obj instanceof ILabelProvider) {
            text = ((ILabelProvider)obj).getText(obj);
/*
        } else if (obj instanceof DBSObject) {
            text = ((DBSObject) obj).getName();
*/
        } else if (obj instanceof DBNNode) {
            text = ((DBNNode) obj).getNodeName();
        } else {
            text = obj.toString();
        }
        if (text == null) {
            text = "?";
        }
        if (isFilteredElement(obj)) {
            text += " (...)";
        }
        return text;
    }

    @Override
    public Image getImage(Object obj)
    {
        if (obj instanceof ILabelProvider) {
            return ((ILabelProvider)obj).getImage(obj);
        }
        if (obj instanceof DBNNode) {
            return ((DBNNode)obj).getNodeIconDefault();
        } else {
            return null;
        }
    }

    @Override
    public Font getFont(Object element)
    {
        if (boldFont == null || !isDefaultElement(element)) {
            return normalFont;
        } else {
            return boldFont;
        }
    }

    @Override
    public Color getForeground(Object element)
    {
        if (element instanceof DBNNode) {
            DBNNode node = (DBNNode)element;
            if (node.isLocked()) {
                return lockedForeground;
            }
            if (node instanceof DBSWrapper && ((DBSWrapper)node).getObject() != null && !((DBSWrapper)node).getObject().isPersisted()) {
                return transientForeground;
            }
        }
        return null;
    }

    @Override
    public Color getBackground(Object element)
    {
        return null;
    }

    private boolean isDefaultElement(Object element)
    {
        if (element instanceof DBSWrapper) {
            DBSObject object = ((DBSWrapper) element).getObject();
            DBSObjectSelector activeContainer = DBUtils.getParentAdapter(
                DBSObjectSelector.class, object);
            if (activeContainer != null) {
                return activeContainer.getSelectedObject() == object;
            }
        } else if (element instanceof DBNProject) {
            if (((DBNProject)element).getProject() == DBeaverCore.getInstance().getProjectRegistry().getActiveProject()) {
                return true;
            }
        }
        return false;
    }

    private boolean isFilteredElement(Object element)
    {
        if (element instanceof DBNNode) {
            return ((DBNNode) element).isFiltered();
        }
        return false;
    }
}
