/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 * Copyright (C) 2017 Alexander Fedorov (alexander.fedorov@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.postgresql.internal.debug.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class PgSqlLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

    public PgSqlLaunchConfigurationTabGroup()
    {
    }

    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode)
    {
        PgSqlTab pgSqlTab = new PgSqlTab();
        CommonTab commonTab = new CommonTab();
        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
                pgSqlTab,
                commonTab
            };
            setTabs(tabs);
    }

}
