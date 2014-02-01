/*
 * Copyright (C) 2010-2014 Serge Rieder
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
package org.jkiss.dbeaver.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.jkiss.dbeaver.model.data.DBDValueHandlerProvider;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * EntityEditorDescriptor
 */
public class DataTypeProviderDescriptor extends AbstractDescriptor
{
    public static final String EXTENSION_ID = "org.jkiss.dbeaver.dataTypeProvider"; //$NON-NLS-1$

    static final Log log = LogFactory.getLog(DataTypeProviderDescriptor.class);

    private static final String ALL_TYPES_PATTERN = "*";

    private String id;
    private String className;
    private Set<Object> supportedTypes = new HashSet<Object>();
    private Set<DataSourceProviderDescriptor> supportedDataSources = new HashSet<DataSourceProviderDescriptor>();

    private DBDValueHandlerProvider instance;

    public DataTypeProviderDescriptor(DataSourceProviderRegistry registry, IConfigurationElement config)
    {
        super(config);

        this.id = config.getAttribute(RegistryConstants.ATTR_ID);
        this.className = config.getAttribute(RegistryConstants.ATTR_CLASS);

        if (className == null) {
            log.error("Empty class name of data type provider '" + this.id + "'"); //$NON-NLS-1$
        }

        IConfigurationElement[] typeElements = config.getChildren(RegistryConstants.TAG_TYPE);
        for (IConfigurationElement typeElement : typeElements) {
            String typeName = typeElement.getAttribute(RegistryConstants.ATTR_NAME);
            if (typeName != null) {
                supportedTypes.add(typeName.toLowerCase());
            } else {
                typeName = typeElement.getAttribute(RegistryConstants.ATTR_STANDARD);
                if (typeName == null) {
                    log.warn("Type element without name or standard type reference"); //$NON-NLS-1$
                    continue;
                }
                try {
                    Field typeField = java.sql.Types.class.getField(typeName);
                    int typeNumber = typeField.getInt(null);
                    supportedTypes.add(typeNumber);
                }
                catch (NoSuchFieldException e) {
                    log.warn("Standard type '" + typeName + "' not found in " + java.sql.Types.class.getName(), e); //$NON-NLS-1$
                }
                catch (IllegalAccessException e) {
                    log.warn("Standard type '" + typeName + "' cannot be accessed", e); //$NON-NLS-1$
                }
            }
        }

        IConfigurationElement[] dsElements = config.getChildren(RegistryConstants.TAG_DATASOURCE);
        for (IConfigurationElement dsElement : dsElements) {
            String dsId = dsElement.getAttribute(RegistryConstants.ATTR_ID);
            if (dsId == null) {
                log.warn("Datasource reference with null ID"); //$NON-NLS-1$
                continue;
            }
            DataSourceProviderDescriptor dsProvider = registry.getDataSourceProvider(dsId);
            if (dsProvider == null) {
                log.warn("Datasource provider '" + dsId + "' not found"); //$NON-NLS-1$
                continue;
            }
            supportedDataSources.add(dsProvider);
        }
    }

    public String getId()
    {
        return id;
    }

    public DBDValueHandlerProvider getInstance()
    {
        if (instance == null && className != null) {
            Class<?> providerClass = super.getObjectClass(className);
            if (providerClass == null) {
                log.error("Could not find data type provider class '" + this.className + "'"); //$NON-NLS-1$
            } else {
                try {
                    this.instance = (DBDValueHandlerProvider) providerClass.newInstance();
                }
                catch (Exception e) {
                    log.error("Can't instantiate data type provider '" + this.id + "'", e); //$NON-NLS-1$
                }
            }
        }
        return instance;
    }

    public boolean supportsType(String typeName, int valueType)
    {
        return
            supportedTypes.contains(valueType) ||
            (typeName != null && supportedTypes.contains(typeName.toLowerCase())) ||
            supportedTypes.contains(ALL_TYPES_PATTERN);
    }

    public Set<Object> getSupportedTypes()
    {
        return supportedTypes;
    }

    public boolean isDefault()
    {
        return supportedDataSources.isEmpty();
    }

    public boolean supportsDataSource(DataSourceProviderDescriptor descriptor)
    {
        return supportedDataSources.contains(descriptor);
    }

    public Set<DataSourceProviderDescriptor> getSupportedDataSources()
    {
        return supportedDataSources;
    }

}