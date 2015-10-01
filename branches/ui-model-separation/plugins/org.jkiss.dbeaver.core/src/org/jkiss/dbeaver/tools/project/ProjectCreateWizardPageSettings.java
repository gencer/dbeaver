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
package org.jkiss.dbeaver.tools.project;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.utils.CommonUtils;


class ProjectCreateWizardPageSettings extends WizardPage {

    private ProjectCreateData createData;

    protected ProjectCreateWizardPageSettings(ProjectCreateData importData)
    {
        super(CoreMessages.dialog_project_create_settings_name);
        this.createData = importData;

        setTitle(CoreMessages.dialog_project_create_settings_title);
        setDescription(CoreMessages.dialog_project_create_settings_description);
    }

    @Override
    public boolean isPageComplete()
    {
        return !CommonUtils.isEmpty(createData.getName());
    }

    @Override
    public void createControl(Composite parent)
    {
        Composite placeholder = UIUtils.createPlaceholder(parent, 1);
        Composite configGroup = UIUtils.createControlGroup(placeholder, CoreMessages.dialog_project_create_settings_group_general, 2, GridData.FILL_HORIZONTAL, 0);

        final Text projectNameText = UIUtils.createLabelText(configGroup, CoreMessages.dialog_project_create_settings_label_name, ""); //$NON-NLS-2$
        projectNameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                createData.setName(projectNameText.getText());
                updateState();
            }
        });

        final Text projectDescriptionText = UIUtils.createLabelText(configGroup, CoreMessages.dialog_project_create_settings_label_description, ""); //$NON-NLS-2$
        projectDescriptionText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e)
            {
                createData.setDescription(projectDescriptionText.getText());
            }
        });

        setControl(placeholder);
    }

    private void updateState()
    {
        getContainer().updateButtons();
    }

}