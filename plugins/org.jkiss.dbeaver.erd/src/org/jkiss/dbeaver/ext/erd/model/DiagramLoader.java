/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

/*
 * Created on Jul 13, 2004
 */
package org.jkiss.dbeaver.ext.erd.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.RelativeBendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.ext.erd.ERDConstants;
import org.jkiss.dbeaver.ext.erd.part.AssociationPart;
import org.jkiss.dbeaver.ext.erd.part.DiagramPart;
import org.jkiss.dbeaver.ext.erd.part.EntityPart;
import org.jkiss.dbeaver.ext.erd.part.NotePart;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPQualifiedObject;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.*;
import org.jkiss.dbeaver.registry.DataSourceDescriptor;
import org.jkiss.dbeaver.registry.DataSourceRegistry;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.utils.CommonUtils;
import org.jkiss.utils.xml.XMLBuilder;
import org.jkiss.utils.xml.XMLException;
import org.jkiss.utils.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Entity diagram loader/saver
 * @author Serge Rieder
 */
public class DiagramLoader
{
    static final Log log = LogFactory.getLog(DiagramLoader.class);

    public static final String TAG_DIAGRAM = "diagram";
    public static final String TAG_ENTITIES = "entities";
    public static final String TAG_DATA_SOURCE = "data-source";
    public static final String TAG_ENTITY = "entity";
    public static final String TAG_PATH = "path";
    public static final String TAG_RELATIONS = "relations";
    public static final String TAG_RELATION = "relation";
    public static final String TAG_BEND = "bend";

    public static final String ATTR_VERSION = "version";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_TIME = "time";
    public static final String ATTR_ID = "id";
    public static final String ATTR_FQ_NAME = "fq-name";
    public static final String ATTR_REF_NAME = "ref-name";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_PK_REF = "pk-ref";
    public static final String ATTR_FK_REF = "fk-ref";
    private static final String TAG_COLUMN = "column";
    public static final String ATTR_X = "x";
    public static final String ATTR_Y = "y";
    public static final String ATTR_W = "w";
    public static final String ATTR_H = "h";

    public static final int ERD_VERSION_1 = 1;
    private static final String BEND_ABSOLUTE = "abs";
    private static final String BEND_RELATIVE = "rel";
    public static final String TAG_NOTES = "notes";
    private static final String TAG_NOTE = "note";

    private static class TableSaveInfo {
        final ERDTable erdTable;
        final EntityPart tablePart;
        final int objectId;

        private TableSaveInfo(ERDTable erdTable, EntityPart tablePart, int objectId)
        {
            this.erdTable = erdTable;
            this.tablePart = tablePart;
            this.objectId = objectId;
        }
    }

    private static class TableLoadInfo {
        final String objectId;
        final DBSEntity table;
        final Rectangle bounds;

        private TableLoadInfo(String objectId, DBSEntity table, Rectangle bounds)
        {
            this.objectId = objectId;
            this.table = table;
            this.bounds = bounds;
        }
    }

    private static class RelationLoadInfo {
        final String name;
        final String type;
        final TableLoadInfo pkTable;
        final TableLoadInfo fkTable;
        final Map<String, String> columns = new LinkedHashMap<String, String>();
        final List<Point> bends = new ArrayList<Point>();

        private RelationLoadInfo(String name, String type, TableLoadInfo pkTable, TableLoadInfo fkTable)
        {
            this.name = name;
            this.type = type;
            this.pkTable = pkTable;
            this.fkTable = fkTable;
        }
    }

    private static class DataSourceObjects {
        List<ERDTable> tables = new ArrayList<ERDTable>();
    }

    public static void load(DBRProgressMonitor monitor, IProject project, DiagramPart diagramPart, InputStream in)
        throws IOException, XMLException, DBException
    {
        monitor.beginTask("Parse diagram", 1);
        final EntityDiagram diagram = diagramPart.getDiagram();

        final DataSourceRegistry dsRegistry = DBeaverCore.getInstance().getProjectRegistry().getDataSourceRegistry(project);
        if (dsRegistry == null) {
            throw new DBException("Cannot find datasource registry for project '" + project.getName() + "'");
        }

        final Document document = XMLUtils.parseDocument(in);

        final Element diagramElem = document.getDocumentElement();
        monitor.done();

        // Check version
        final String diagramVersion = diagramElem.getAttribute(ATTR_VERSION);
        if (CommonUtils.isEmpty(diagramVersion)) {
            throw new DBException("Diagram version not found");
        }
        if (!diagramVersion.equals(String.valueOf(ERD_VERSION_1))) {
            throw new DBException("Unsupported diagram version: " + diagramVersion);
        }

        List<TableLoadInfo> tableInfos = new ArrayList<TableLoadInfo>();
        List<RelationLoadInfo> relInfos = new ArrayList<RelationLoadInfo>();
        Map<String, TableLoadInfo> tableMap = new HashMap<String, TableLoadInfo>();

        final Element entitiesElem = XMLUtils.getChildElement(diagramElem, TAG_ENTITIES);
        if (entitiesElem != null) {

            // Parse data source
            for (Element dsElem : XMLUtils.getChildElementList(entitiesElem, TAG_DATA_SOURCE)) {
                String dsId = dsElem.getAttribute(ATTR_ID);
                if (CommonUtils.isEmpty(dsId)) {
                    log.warn("Missing datasource ID");
                    continue;
                }
                // Get connected datasource
                final DataSourceDescriptor dataSourceContainer = dsRegistry.getDataSource(dsId);
                if (dataSourceContainer == null) {
                    log.warn("Datasource '" + dsId + "' not found");
                    continue;
                }
                if (!dataSourceContainer.isConnected()) {
                    monitor.subTask("Connect to '" + dataSourceContainer.getName() + "'");
                    try {
                        dataSourceContainer.connect(monitor);
                    } catch (DBException e) {
                        diagram.addErrorMessage("Can't connect to '" + dataSourceContainer.getName() + "': " + e.getMessage());
                        continue;
                    }
                }
                final DBPDataSource dataSource = dataSourceContainer.getDataSource();
                if (!(dataSource instanceof DBSObjectContainer)) {
                    diagram.addErrorMessage("Datasource '" + dataSourceContainer.getName() + "' entities cannot be loaded - no entity container found");
                    continue;
                }
                DBSObjectContainer rootContainer = (DBSObjectContainer)dataSource;
                // Parse entities
                Collection<Element> entityElemList = XMLUtils.getChildElementList(dsElem, TAG_ENTITY);
                monitor.beginTask("Parse entities", entityElemList.size());
                for (Element entityElem : entityElemList) {
                    String tableId = entityElem.getAttribute(ATTR_ID);
                    String tableName = entityElem.getAttribute(ATTR_NAME);
                    monitor.subTask("Load " + tableName);
                    List<String> path = new ArrayList<String>();
                    for (Element pathElem : XMLUtils.getChildElementList(entityElem, TAG_PATH)) {
                        path.add(0, pathElem.getAttribute(ATTR_NAME));
                    }
                    DBSObjectContainer container = rootContainer;
                    for (String conName : path) {
                        final DBSObject child = container.getChild(monitor, conName);
                        if (child == null) {
                            diagram.addErrorMessage("Object '" + conName + "' not found within '" + container.getName() + "'");
                            container = null;
                            break;
                        }
                        if (child instanceof DBSObjectContainer) {
                            container = (DBSObjectContainer) child;
                        } else {
                            diagram.addErrorMessage("Object '" + child.getName() + "' is not a container");
                            container = null;
                            break;
                        }
                    }
                    if (container == null) {
                        continue;
                    }
                    final DBSObject child = container.getChild(monitor, tableName);
                    if (!(child instanceof DBSEntity)) {
                        diagram.addErrorMessage("Cannot find table '" + tableName + "' in '" + container.getName() + "'");
                        continue;
                    }
                    String locX = entityElem.getAttribute(ATTR_X);
                    String locY = entityElem.getAttribute(ATTR_Y);

                    DBSEntity table = (DBSEntity) child;
                    Rectangle bounds = new Rectangle();
                    if (CommonUtils.isEmpty(locX) || CommonUtils.isEmpty(locY)) {
                        diagram.setNeedsAutoLayout(true);
                    } else {
                        bounds.x = Integer.parseInt(locX);
                        bounds.y = Integer.parseInt(locY);
                    }

                    TableLoadInfo info = new TableLoadInfo(tableId, table, bounds);
                    tableInfos.add(info);
                    tableMap.put(info.objectId, info);
                    monitor.worked(1);
                }
                monitor.done();
            }
        }

        final Element relationsElem = XMLUtils.getChildElement(diagramElem, TAG_RELATIONS);
        if (relationsElem != null) {
            // Parse relations
            Collection<Element> relElemList = XMLUtils.getChildElementList(relationsElem, TAG_RELATION);
            monitor.beginTask("Parse relations", relElemList.size());
            for (Element relElem : relElemList) {
                String relName = relElem.getAttribute(ATTR_NAME);
                monitor.subTask("Load " + relName);
                String relType = relElem.getAttribute(ATTR_TYPE);
                String pkRefId = relElem.getAttribute(ATTR_PK_REF);
                String fkRefId = relElem.getAttribute(ATTR_FK_REF);
                if (CommonUtils.isEmpty(relName) || CommonUtils.isEmpty(pkRefId) || CommonUtils.isEmpty(fkRefId)) {
                    log.warn("Missing relation ID");
                    continue;
                }
                TableLoadInfo pkTable = tableMap.get(pkRefId);
                TableLoadInfo fkTable = tableMap.get(fkRefId);
                if (pkTable == null || fkTable == null) {
                    log.debug("PK (" + pkRefId + ") or FK (" + fkRefId +") table(s) not found for relation " + relName);
                    continue;
                }
                RelationLoadInfo relationLoadInfo = new RelationLoadInfo(relName, relType, pkTable, fkTable);
                relInfos.add(relationLoadInfo);

                // Load columns (present only in logical relations)
                for (Element columnElem : XMLUtils.getChildElementList(relElem, TAG_COLUMN)) {
                    String name = columnElem.getAttribute(ATTR_NAME);
                    String refName = columnElem.getAttribute(ATTR_REF_NAME);
                    relationLoadInfo.columns.put(name, refName);
                }

                // Load bends
                for (Element bendElem : XMLUtils.getChildElementList(relElem, TAG_BEND)) {
                    String type = bendElem.getAttribute(ATTR_TYPE);
                    if (!BEND_RELATIVE.equals(type)) {
                        String locX = bendElem.getAttribute(ATTR_X);
                        String locY = bendElem.getAttribute(ATTR_Y);
                        if (!CommonUtils.isEmpty(locX) && !CommonUtils.isEmpty(locY)) {
                            relationLoadInfo.bends.add(new Point(
                                Integer.parseInt(locX),
                                Integer.parseInt(locY)));
                        }
                    }
                }
                monitor.worked(1);
            }
            monitor.done();
        }

        // Load notes
        final Element notesElem = XMLUtils.getChildElement(diagramElem, TAG_NOTES);
        if (notesElem != null) {
            // Parse relations
            Collection<Element> noteElemList = XMLUtils.getChildElementList(notesElem, TAG_NOTE);
            monitor.beginTask("Parse notes", noteElemList.size());
            for (Element noteElem : noteElemList) {
                final String noteText = XMLUtils.getElementBody(noteElem);
                ERDNote note = new ERDNote(noteText);
                diagram.addNote(note, false);
                String locX = noteElem.getAttribute(ATTR_X);
                String locY = noteElem.getAttribute(ATTR_Y);
                String locW = noteElem.getAttribute(ATTR_W);
                String locH = noteElem.getAttribute(ATTR_H);
                if (!CommonUtils.isEmpty(locX) && !CommonUtils.isEmpty(locY) && !CommonUtils.isEmpty(locW) && !CommonUtils.isEmpty(locH)) {
                    Rectangle bounds = new Rectangle(
                        Integer.parseInt(locX), Integer.parseInt(locY), Integer.parseInt(locW), Integer.parseInt(locH));
                    diagram.addInitBounds(note, bounds);
                }
            }
        }

        // Fill tables
        List<DBSEntity> tableList = new ArrayList<DBSEntity>();
        for (TableLoadInfo info : tableInfos) {
            tableList.add(info.table);
        }
        diagram.fillTables(monitor, tableList, null);

        // Set initial bounds
        for (TableLoadInfo info : tableInfos) {
            final ERDTable erdTable = diagram.getERDTable(info.table);
            if (erdTable != null) {
                diagram.addInitBounds(erdTable, info.bounds);
            }
        }

        // Add logical relations
        for (RelationLoadInfo info : relInfos) {
            if (info.type.equals(ERDConstants.CONSTRAINT_LOGICAL_FK.getId())) {
                final ERDTable sourceTable = diagram.getERDTable(info.pkTable.table);
                final ERDTable targetTable = diagram.getERDTable(info.fkTable.table);
                if (sourceTable != null && targetTable != null) {
                    new ERDAssociation(targetTable, sourceTable, false);
                }
            }
        }
        // Set relations' bends
        for (RelationLoadInfo info : relInfos) {
            if (!CommonUtils.isEmpty(info.bends)) {
                final ERDTable sourceTable = diagram.getERDTable(info.pkTable.table);
                if (sourceTable == null) {
                    log.warn("Source table " + info.pkTable.table.getName() + " not found");
                    continue;
                }
                final ERDTable targetTable = diagram.getERDTable(info.fkTable.table);
                if (targetTable == null) {
                    log.warn("Target table " + info.pkTable.table.getName() + " not found");
                    continue;
                }
                diagram.addInitRelationBends(sourceTable, targetTable, info.name, info.bends);
            }
        }
    }

    public static void save(DiagramPart diagramPart, final EntityDiagram diagram, boolean verbose, OutputStream out)
        throws IOException
    {
        // Prepare DS objects map
        Map<DBSDataSourceContainer, DataSourceObjects> dsMap = new IdentityHashMap<DBSDataSourceContainer, DataSourceObjects>();
        if (diagram != null) {
            for (ERDTable erdTable : diagram.getTables()) {
                final DBSDataSourceContainer dsContainer = erdTable.getObject().getDataSource().getContainer();
                DataSourceObjects desc = dsMap.get(dsContainer);
                if (desc == null) {
                    desc = new DataSourceObjects();
                    dsMap.put(dsContainer, desc);
                }
                desc.tables.add(erdTable);
            }
        }

        Map<ERDTable, TableSaveInfo> infoMap = new IdentityHashMap<ERDTable, TableSaveInfo>();

        // Save as XML
        XMLBuilder xml = new XMLBuilder(out, ContentUtils.DEFAULT_FILE_CHARSET);
        xml.setButify(true);
        if (verbose) {
            xml.addContent(
                "\n<!DOCTYPE diagram [\n" +
                "<!ATTLIST diagram version CDATA #REQUIRED\n" +
                " name CDATA #IMPLIED\n" +
                " time CDATA #REQUIRED>\n" +
                "<!ELEMENT diagram (entities, relations, notes)>\n" +
                "<!ELEMENT entities (data-source*)>\n" +
                "<!ELEMENT data-source (entity*)>\n" +
                "<!ATTLIST data-source id CDATA #REQUIRED>\n" +
                "<!ELEMENT entity (path*)>\n" +
                "<!ATTLIST entity id ID #REQUIRED\n" +
                " name CDATA #REQUIRED\n" +
                " fq-name CDATA #REQUIRED>\n" +
                "<!ELEMENT relations (relation*)>\n" +
                "<!ELEMENT relation (bend*)>\n" +
                "<!ATTLIST relation name CDATA #REQUIRED\n" +
                " fq-name CDATA #REQUIRED\n" +
                " pk-ref IDREF #REQUIRED\n" +
                " fk-ref IDREF #REQUIRED>\n" +
                "]>\n"
            );
        }
        xml.startElement(TAG_DIAGRAM);
        xml.addAttribute(ATTR_VERSION, ERD_VERSION_1);
        if (diagram != null) {
            xml.addAttribute(ATTR_NAME, diagram.getName());
        }
        xml.addAttribute(ATTR_TIME, RuntimeUtils.getCurrentTimeStamp());

        if (diagram != null) {
            xml.startElement(TAG_ENTITIES);
            for (DBSDataSourceContainer dsContainer : dsMap.keySet()) {
                xml.startElement(TAG_DATA_SOURCE);
                xml.addAttribute(ATTR_ID, dsContainer.getId());

                final DataSourceObjects desc = dsMap.get(dsContainer);
                int tableCounter = ERD_VERSION_1;
                for (ERDTable erdTable : desc.tables) {
                    final DBSEntity table = erdTable.getObject();
                    EntityPart tablePart = diagramPart == null ? null : diagramPart.getEntityPart(erdTable);
                    TableSaveInfo info = new TableSaveInfo(erdTable, tablePart, tableCounter++);
                    infoMap.put(erdTable, info);

                    xml.startElement(TAG_ENTITY);
                    xml.addAttribute(ATTR_ID, info.objectId);
                    xml.addAttribute(ATTR_NAME, table.getName());
                    if (table instanceof DBPQualifiedObject) {
                        xml.addAttribute(ATTR_FQ_NAME, ((DBPQualifiedObject)table).getFullQualifiedName());
                    }
                    Rectangle tableBounds;
                    if (tablePart != null) {
                        tableBounds = tablePart.getBounds();
                    } else {
                        tableBounds = diagram.getInitBounds(erdTable);
                    }
                    if (tableBounds != null) {
                        xml.addAttribute(ATTR_X, tableBounds.x);
                        xml.addAttribute(ATTR_Y, tableBounds.y);
                    }
                    for (DBSObject parent = table.getParentObject(); parent != null && parent != dsContainer; parent = parent.getParentObject()) {
                        xml.startElement(TAG_PATH);
                        xml.addAttribute(ATTR_NAME, parent.getName());
                        xml.endElement();
                    }
                    xml.endElement();
                }
                xml.endElement();
            }
            xml.endElement();

            // Relations
            xml.startElement(TAG_RELATIONS);

            for (ERDTable erdTable : diagram.getTables()) {
                for (ERDAssociation rel : erdTable.getPrimaryKeyRelationships()) {
                    xml.startElement(TAG_RELATION);
                    DBSEntityAssociation association = rel.getObject();
                    xml.addAttribute(ATTR_NAME, association.getName());
                    if (association instanceof DBPQualifiedObject) {
                        xml.addAttribute(ATTR_FQ_NAME, ((DBPQualifiedObject) association).getFullQualifiedName());
                    }
                    xml.addAttribute(ATTR_TYPE, association.getConstraintType().getId());
                    TableSaveInfo pkInfo = infoMap.get(rel.getPrimaryKeyTable());
                    if (pkInfo == null) {
                        log.error("Cannot find PK table '" + rel.getPrimaryKeyTable().getObject().getFullQualifiedName() + "' in info map");
                        continue;
                    }
                    TableSaveInfo fkInfo = infoMap.get(rel.getForeignKeyTable());
                    if (fkInfo == null) {
                        log.error("Cannot find FK table '" + rel.getForeignKeyTable().getObject().getFullQualifiedName() + "' in info map");
                        continue;
                    }
                    xml.addAttribute(ATTR_PK_REF, pkInfo.objectId);
                    xml.addAttribute(ATTR_FK_REF, fkInfo.objectId);

                    if (association instanceof ERDLogicalForeignKey) {
                        // Save columns
                        for (DBSConstraintColumn column : ((ERDLogicalForeignKey) association).getColumns(VoidProgressMonitor.INSTANCE)) {
                            xml.startElement(TAG_COLUMN);
                            xml.addAttribute(ATTR_NAME, column.getName());
                            xml.addAttribute(ATTR_REF_NAME, ((DBSForeignKeyColumn)column).getReferencedColumn().getName());
                            xml.endElement();
                        }
                    }

                    // Save bends
                    if (pkInfo.tablePart != null) {
                        AssociationPart associationPart = pkInfo.tablePart.getConnectionPart(rel, false);
                        if (associationPart != null) {
                            final List<Bendpoint> bendpoints = associationPart.getBendpoints();
                            if (!CommonUtils.isEmpty(bendpoints)) {
                                for (Bendpoint bendpoint : bendpoints) {
                                    xml.startElement(TAG_BEND);
                                    if (bendpoint instanceof AbsoluteBendpoint) {
                                        xml.addAttribute(ATTR_TYPE, BEND_ABSOLUTE);
                                        xml.addAttribute(ATTR_X, bendpoint.getLocation().x);
                                        xml.addAttribute(ATTR_Y, bendpoint.getLocation().y);
                                    } else if (bendpoint instanceof RelativeBendpoint) {
                                        xml.addAttribute(ATTR_TYPE, BEND_RELATIVE);
                                        xml.addAttribute(ATTR_X, bendpoint.getLocation().x);
                                        xml.addAttribute(ATTR_Y, bendpoint.getLocation().y);
                                    }
                                    xml.endElement();
                                }
                            }
                        }
                    }

                    xml.endElement();
                }
            }

            xml.endElement();

            // Notes
            xml.startElement(TAG_NOTES);
            for (ERDNote note : diagram.getNotes()) {
                NotePart notePart = diagramPart == null ? null : diagramPart.getNotePart(note);
                xml.startElement(TAG_NOTE);
                if (notePart != null) {
                    Rectangle noteBounds = notePart.getBounds();
                    if (noteBounds != null) {
                        xml.addAttribute(ATTR_X, noteBounds.x);
                        xml.addAttribute(ATTR_Y, noteBounds.y);
                        xml.addAttribute(ATTR_W, noteBounds.width);
                        xml.addAttribute(ATTR_H, noteBounds.height);
                    }
                }
                xml.addText(note.getObject());
                xml.endElement();
            }
            xml.endElement();
        }

        xml.endElement();

        xml.flush();
    }


}