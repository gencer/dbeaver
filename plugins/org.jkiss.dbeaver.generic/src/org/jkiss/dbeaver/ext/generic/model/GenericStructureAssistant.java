/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.generic.model;

import net.sf.jkiss.utils.CommonUtils;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCStructureAssistant;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.struct.RelationalObjectType;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectType;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * GenericDataSource
 */
public class GenericStructureAssistant extends JDBCStructureAssistant
{
    private final GenericDataSource dataSource;

    public GenericStructureAssistant(GenericDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    protected GenericDataSource getDataSource()
    {
        return dataSource;
    }

    public DBSObjectType[] getSupportedObjectTypes()
    {
        return new DBSObjectType[] {
            RelationalObjectType.TYPE_TABLE,
            RelationalObjectType.TYPE_PROCEDURE
            };
    }

    @Override
    protected void findObjectsByMask(JDBCExecutionContext context, DBSObjectType objectType, DBSObject parentObject, String objectNameMask, int maxResults, List<DBSObject> objects) throws DBException, SQLException
    {
        GenericSchema schema = parentObject instanceof GenericSchema ? (GenericSchema)parentObject : null;
        GenericCatalog catalog = parentObject instanceof GenericCatalog ? (GenericCatalog)parentObject :
            schema == null ? null : schema.getCatalog();
        objectNameMask = getDataSource().getNameConverter().convert(objectNameMask);

        if (objectType == RelationalObjectType.TYPE_TABLE) {
            findTablesByMask(context, catalog, schema, objectNameMask, maxResults, objects);
        } else if (objectType == RelationalObjectType.TYPE_PROCEDURE) {
            findProceduresByMask(context, catalog, schema, objectNameMask, maxResults, objects);
        }
    }

    private void findTablesByMask(JDBCExecutionContext context, GenericCatalog catalog, GenericSchema schema, String tableNameMask, int maxResults, List<DBSObject> objects)
        throws SQLException, DBException
    {
        DBRProgressMonitor monitor = context.getProgressMonitor();
        JDBCResultSet dbResult = context.getMetaData().getTables(
            catalog == null ? null : catalog.getName(),
            schema == null ? null : schema.getName(),
            tableNameMask,
            null);
        try {
            while (dbResult.next()) {
                if (monitor.isCanceled()) {
                    break;
                }
                String catalogName = JDBCUtils.safeGetString(dbResult, JDBCConstants.TABLE_CAT);
                String schemaName = JDBCUtils.safeGetString(dbResult, JDBCConstants.TABLE_SCHEM);
                String tableName = JDBCUtils.safeGetString(dbResult, JDBCConstants.TABLE_NAME);
                if (CommonUtils.isEmpty(tableName)) {
                    continue;
                }
                GenericCatalog tableCatalog = catalog != null ? catalog : CommonUtils.isEmpty(catalogName) ? null : dataSource.getCatalog(catalogName);
                if (tableCatalog == null && CommonUtils.isEmpty(catalogName) && !CommonUtils.isEmpty(dataSource.getCatalogs()) && dataSource.getCatalogs().size() == 1) {
                    // there is only one catalog - let's use it (PostgreSQL)
                    tableCatalog = dataSource.getCatalogs().get(0);
                }
                GenericSchema tableSchema = schema != null ?
                    schema :
                    CommonUtils.isEmpty(schemaName) ? null :
                        tableCatalog == null ? dataSource.getSchema(schemaName) : tableCatalog.getSchema(monitor, schemaName);
                GenericTable table;
                if (tableSchema != null) {
                    table = tableSchema.getTable(monitor, tableName);
                } else if (tableCatalog != null) {
                    table = tableCatalog.getTable(monitor, tableName);
                } else {
                    table = dataSource.getTable(monitor, tableName);
                }
                if (table == null) {
                    log.debug("Couldn't find table '" + tableName + "' in '" + catalogName + "/" + schemaName + "'");
                } else {
                    objects.add(table);
                }
                if (objects.size() >= maxResults) {
                    break;
                }
            }
        }
        finally {
            dbResult.close();
        }
    }

    private void findProceduresByMask(JDBCExecutionContext context, GenericCatalog catalog, GenericSchema schema, String procNameMask, int maxResults, List<DBSObject> objects)
        throws SQLException, DBException
    {
        DBRProgressMonitor monitor = context.getProgressMonitor();
        JDBCResultSet dbResult = context.getMetaData().getProcedures(
            catalog == null ? null : catalog.getName(),
            schema == null ? null : schema.getName(),
            procNameMask);
        try {
            while (dbResult.next()) {
                if (monitor.isCanceled()) {
                    break;
                }
                String catalogName = JDBCUtils.safeGetString(dbResult, JDBCConstants.PROCEDURE_CAT);
                String schemaName = JDBCUtils.safeGetString(dbResult, JDBCConstants.PROCEDURE_SCHEM);
                String procName = JDBCUtils.safeGetString(dbResult, JDBCConstants.PROCEDURE_NAME);
                if (CommonUtils.isEmpty(procName)) {
                    continue;
                }
                GenericCatalog procCatalog = catalog != null ? catalog : CommonUtils.isEmpty(catalogName) ? null : dataSource.getCatalog(catalogName);
                GenericSchema procSchema = schema != null ?
                    schema :
                    CommonUtils.isEmpty(schemaName) ? null :
                        procCatalog == null ? dataSource.getSchema(schemaName) : procCatalog.getSchema(monitor, schemaName);
                Collection<GenericProcedure> procedures = null;
                if (procSchema != null) {
                    // Try to use catalog name as package name (Oracle)
                    if (!CommonUtils.isEmpty(catalogName)) {
                        GenericPackage procPackage = procSchema.getPackage(monitor, catalogName);
                        if (procPackage != null) {
                            procedures = procPackage.getProcedures(monitor, procName);
                        }
                    }
                    if (procedures == null) {
                        procedures = procSchema.getProcedures(monitor, procName);
                    }
                } else if (procCatalog != null) {
                    procedures = procCatalog.getProcedures(monitor, procName);
                } else {
                    procedures = dataSource.getProcedures(monitor, procName);
                }
                if (CommonUtils.isEmpty(procedures)) {
                    log.debug("Couldn't find procedure '" + procName + "'");
                } else {
                    for (GenericProcedure proc : procedures) {
                        if (!objects.contains(proc)) {
                            objects.add(proc);
                        }
                    }
                }
                if (objects.size() >= maxResults) {
                    break;
                }
            }
        }
        finally {
            dbResult.close();
        }
    }

}
