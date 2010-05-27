/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.data;

import org.jkiss.dbeaver.model.dbc.DBCColumnMetaData;

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
    Collection<DBCColumnMetaData> getColumnsMetaData();

    /**
     * Tries to read value of certain column from result set.
     * @param column column, must belong to the same result set as controller's value
     * @return value or null
     */
    Object getColumnValue(DBCColumnMetaData column);

}