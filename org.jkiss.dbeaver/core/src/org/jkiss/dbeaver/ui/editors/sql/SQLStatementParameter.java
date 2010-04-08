package org.jkiss.dbeaver.ui.editors.sql;

import org.jkiss.dbeaver.model.data.DBDValueHandler;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;

/**
 * SQL statement parameter info
 */
public class SQLStatementParameter {
    private DBDValueHandler valueHandler;
    DBSTypedObject paramType;
    private int index;
    private Object value;

    public DBDValueHandler getValueHandler() {
        return valueHandler;
    }

    public DBSTypedObject getParamType() {
        return paramType;
    }

    public int getIndex() {
        return index;
    }

    public Object getValue() {
        return value;
    }
}
