/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.properties;

import net.sf.jkiss.utils.CommonUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.jkiss.dbeaver.ui.UIUtils;

/**
 * StandardPropertiesSection
 */
public class StandardPropertiesSection extends AbstractPropertySection implements ILazyPropertyLoadListener, IPropertySourceListener {

	protected PropertyTreeViewer propertyTree;
    private IPropertySource curPropertySource;
    private Font boldFont;

    public void createControls(Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage)
    {
		super.createControls(parent, tabbedPropertySheetPage);
        this.boldFont = UIUtils.makeBoldFont(parent.getFont());

		propertyTree = new PropertyTreeViewer(parent, SWT.NONE);
        propertyTree.setExtraLabelProvider(new PropertyLabelProvider());
        PropertiesContributor.getInstance().addLazyListener(this);

	}

	public void setInput(IWorkbenchPart part, ISelection newSelection) {
        if (!CommonUtils.equalObjects(getSelection(), newSelection)) {
		    super.setInput(part, newSelection);
            if (!newSelection.isEmpty() && newSelection instanceof IStructuredSelection) {
                Object element = ((IStructuredSelection) newSelection).getFirstElement();
                if (element instanceof IPropertySource && element != curPropertySource) {
                    if (curPropertySource instanceof IPropertySourceEditable) {
                        ((IPropertySourceEditable) curPropertySource).removePropertySourceListener(this);
                    }
                    curPropertySource = (IPropertySource)element;
                    propertyTree.loadProperties(curPropertySource);
                    if (curPropertySource instanceof IPropertySourceEditable) {
                        ((IPropertySourceEditable) curPropertySource).addPropertySourceListener(this);
                    }

                }
            }
		    //pageStandard.selectionChanged(part, newSelection);
        }
	}

	public void dispose() {
        UIUtils.dispose(boldFont);
        PropertiesContributor.getInstance().removeLazyListener(this);
		super.dispose();
	}

	public void refresh() {
		//propertyTree.refresh();
	}

	public boolean shouldUseExtraSpace()
    {
		return true;
	}

    @Override
    public void aboutToBeShown()
    {
//        if (editor instanceof IProgressControlProvider) {
//            ((IProgressControlProvider)editor).getProgressControl().activate(true);
//        }
    }

    @Override
    public void aboutToBeHidden()
    {

    }

    public void handlePropertyLoad(Object object, Object propertyId, Object propertyValue, boolean completed)
    {
        if (curPropertySource.getEditableValue() == object && !propertyTree.getControl().isDisposed()) {
            //propertyTree.get
            propertyTree.refresh();
        }
    }

    public void handlePropertyChange(Object editableValue, IPropertyDescriptor prop, Object value)
    {
        if (!propertyTree.getTree().isDisposed()) {
            propertyTree.refresh();
        }
    }

    private class PropertyLabelProvider extends ColumnLabelProvider implements IFontProvider {
        public Font getFont(Object element)
        {
            if (element instanceof IPropertyDescriptorEx && curPropertySource != null && ((IPropertyDescriptorEx) element).isEditable(curPropertySource.getEditableValue())) {
                return boldFont;
            }
            return null;
        }
    }

}