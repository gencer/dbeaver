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
package org.jkiss.dbeaver.model.data;

import org.jkiss.dbeaver.model.exec.DBCAttributeMetaData;
import org.jkiss.dbeaver.model.exec.DBCEntityMetaData;

import java.util.Collection;

/**
 * DBD Row Controller
 */
public interface DBDRowController
{
    /**
     * Column meta data
     * @return meta data
     */
    Collection<DBCAttributeMetaData> getAttributesMetaData();

    /**
     * Find column metadata by specified table and column name
     * @param entity table
     * @param columnName column name
     * @return column meta data or null
     */
    DBCAttributeMetaData getAttributeMetaData(DBCEntityMetaData entity, String columnName);

    /**
     * Tries to read value of certain column from result set.
     * @param attribute column, must belong to the same result set as controller's value
     * @return value or null
     */
    Object getAttributeValue(DBCAttributeMetaData attribute);

}