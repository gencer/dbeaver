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
package org.jkiss.dbeaver.runtime.qm;

import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPTransactionIsolation;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.DBCResultSet;
import org.jkiss.dbeaver.model.exec.DBCSavepoint;
import org.jkiss.dbeaver.model.exec.DBCStatement;
import org.jkiss.dbeaver.model.qm.QMExecutionHandler;

/**
 * Default execution handler.
 * Handle methods are no-ops.
 */
public abstract class DefaultExecutionHandler implements QMExecutionHandler {

    @Override
    public void handleSessionStart(DBPDataSource dataSource, boolean transactional)
    {

    }

    @Override
    public void handleSessionEnd(DBPDataSource dataSource)
    {

    }

    @Override
    public void handleContextOpen(DBCExecutionContext context)
    {

    }

    @Override
    public void handleContextClose(DBCExecutionContext context)
    {

    }

    @Override
    public void handleTransactionAutocommit(DBCExecutionContext context, boolean autoCommit)
    {

    }

    @Override
    public void handleTransactionIsolation(DBCExecutionContext context, DBPTransactionIsolation level)
    {

    }

    @Override
    public void handleTransactionCommit(DBCExecutionContext context)
    {

    }

    @Override
    public void handleTransactionSavepoint(DBCSavepoint savepoint)
    {

    }

    @Override
    public void handleTransactionRollback(DBCExecutionContext context, DBCSavepoint savepoint)
    {

    }

    @Override
    public void handleStatementOpen(DBCStatement statement)
    {

    }

    @Override
    public void handleStatementExecuteBegin(DBCStatement statement)
    {

    }

    @Override
    public void handleStatementExecuteEnd(DBCStatement statement, long rows, Throwable error)
    {
        
    }

    @Override
    public void handleStatementBind(DBCStatement statement, Object column, Object value)
    {

    }

    @Override
    public void handleStatementClose(DBCStatement statement)
    {

    }

    @Override
    public void handleResultSetOpen(DBCResultSet resultSet)
    {

    }

    @Override
    public void handleResultSetClose(DBCResultSet resultSet, long rowCount)
    {

    }

    @Override
    public void handleScriptBegin(DBCExecutionContext context)
    {

    }

    @Override
    public void handleScriptEnd(DBCExecutionContext context)
    {

    }
}