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
package org.jkiss.dbeaver.model;

import org.jkiss.dbeaver.model.net.DBWHandlerConfiguration;
import org.jkiss.dbeaver.model.net.DBWHandlerType;
import org.jkiss.dbeaver.model.runtime.DBRShellCommand;

import java.util.*;

/**
 * DBPConnectionInfo
 */
public class DBPConnectionInfo implements DBPObject
{

    private String hostName;
    private String hostPort;
    private String serverName;
    private String databaseName;
    private String userName;
    private String userPassword;
    private String url;
    private String clientHomeId;
    private final Map<Object, Object> properties;
    private final Map<DBPConnectionEventType, DBRShellCommand> events;
    private final List<DBWHandlerConfiguration> handlers;

    public DBPConnectionInfo()
    {
        this.properties = new HashMap<Object, Object>();
        this.events = new HashMap<DBPConnectionEventType, DBRShellCommand>();
        this.handlers = new ArrayList<DBWHandlerConfiguration>();
    }

    public DBPConnectionInfo(DBPConnectionInfo info)
    {
        this.hostName = info.hostName;
        this.hostPort = info.hostPort;
        this.serverName = info.serverName;
        this.databaseName = info.databaseName;
        this.userName = info.userName;
        this.userPassword = info.userPassword;
        this.url = info.url;
        this.clientHomeId = info.clientHomeId;
        this.properties = new HashMap<Object, Object>(info.properties);
        this.events = new HashMap<DBPConnectionEventType, DBRShellCommand>(info.events.size());
        for (Map.Entry<DBPConnectionEventType, DBRShellCommand> entry : info.events.entrySet()) {
            this.events.put(entry.getKey(), new DBRShellCommand(entry.getValue()));
        }
        this.handlers = new ArrayList<DBWHandlerConfiguration>(info.handlers.size());
        for (DBWHandlerConfiguration handler : info.handlers) {
            this.handlers.add(new DBWHandlerConfiguration(handler));
        }
    }

/*
	public DBPConnectionInfo(DBPDriver driver)
	{
		this.driver = driver;
	}

	public DBPDriver getDriver()
	{
		return driver;
	}
*/

    public String getClientHomeId()
    {
        return clientHomeId;
    }

    public void setClientHomeId(String clientHomeId)
    {
        this.clientHomeId = clientHomeId;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public String getHostPort()
    {
        return hostPort;
    }

    public void setHostPort(String hostPort)
    {
        this.hostPort = hostPort;
    }

    public String getServerName()
    {
        return serverName;
    }

    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }

    public String getDatabaseName()
    {
        return databaseName;
    }

    public void setDatabaseName(String databaseName)
    {
        this.databaseName = databaseName;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserPassword()
    {
        return userPassword;
    }

    public void setUserPassword(String userPassword)
    {
        this.userPassword = userPassword;
    }

    public Map<Object, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<Object, Object> properties)
    {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    public DBRShellCommand getEvent(DBPConnectionEventType eventType)
    {
        return events.get(eventType);
    }

    public void setEvent(DBPConnectionEventType eventType, DBRShellCommand command)
    {
        if (command == null) {
            events.remove(eventType);
        } else {
            events.put(eventType, command);
        }
    }

    public DBPConnectionEventType[] getDeclaredEvents()
    {
        Set<DBPConnectionEventType> eventTypes = events.keySet();
        return eventTypes.toArray(new DBPConnectionEventType[eventTypes.size()]);
    }

    public List<DBWHandlerConfiguration> getDeclaredHandlers()
    {
        return handlers;
    }

    public void setHandlers(List<DBWHandlerConfiguration> handlers)
    {
        this.handlers.clear();
        this.handlers.addAll(handlers);
    }

    public void addHandler(DBWHandlerConfiguration handler)
    {
        this.handlers.add(handler);
    }

    public DBWHandlerConfiguration getHandler(DBWHandlerType handlerType)
    {
        for (DBWHandlerConfiguration handler : handlers) {
            if (handler.getType() == handlerType && handler.isEnabled()) {
                return handler;
            }
        }
        return null;
    }

}