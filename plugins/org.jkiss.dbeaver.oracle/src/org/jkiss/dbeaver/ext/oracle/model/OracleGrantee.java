/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.oracle.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPSaveableObject;
import org.jkiss.dbeaver.model.access.DBAUser;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectCache;
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * OracleGrantee
 */
public abstract class OracleGrantee extends OracleGlobalObject implements DBAUser, DBPSaveableObject
{
    static final Log log = LogFactory.getLog(OracleGrantee.class);

    final RolePrivCache rolePrivCache = new RolePrivCache();
    final SystemPrivCache systemPrivCache = new SystemPrivCache();

    public OracleGrantee(OracleDataSource dataSource) {
        super(dataSource, true);
    }

    @Association
    public Collection<OraclePrivRole> getRolePrivs(DBRProgressMonitor monitor) throws DBException
    {
        return rolePrivCache.getObjects(monitor, this);
    }

    @Association
    public Collection<OraclePrivSystem> getSystemPrivs(DBRProgressMonitor monitor) throws DBException
    {
        return systemPrivCache.getObjects(monitor, this);
    }

    static class RolePrivCache extends JDBCObjectCache<OracleGrantee, OraclePrivRole> {
        @Override
        protected JDBCPreparedStatement prepareObjectsStatement(JDBCExecutionContext context, OracleGrantee owner) throws SQLException
        {
            final JDBCPreparedStatement dbStat = context.prepareStatement(
                "SELECT * FROM DBA_ROLE_PRIVS WHERE GRANTEE=? ORDER BY GRANTED_ROLE");
            dbStat.setString(1, owner.getName());
            return dbStat;
        }

        @Override
        protected OraclePrivRole fetchObject(JDBCExecutionContext context, OracleGrantee owner, ResultSet resultSet) throws SQLException, DBException
        {
            return new OraclePrivRole(owner, resultSet);
        }
    }

    static class SystemPrivCache extends JDBCObjectCache<OracleGrantee, OraclePrivSystem> {
        @Override
        protected JDBCPreparedStatement prepareObjectsStatement(JDBCExecutionContext context, OracleGrantee owner) throws SQLException
        {
            final JDBCPreparedStatement dbStat = context.prepareStatement(
                "SELECT * FROM DBA_SYS_PRIVS WHERE GRANTEE=? ORDER BY PRIVILEGE");
            dbStat.setString(1, owner.getName());
            return dbStat;
        }

        @Override
        protected OraclePrivSystem fetchObject(JDBCExecutionContext context, OracleGrantee owner, ResultSet resultSet) throws SQLException, DBException
        {
            return new OraclePrivSystem(owner, resultSet);
        }
    }


}