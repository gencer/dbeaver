/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

/*
 * Created on Jul 13, 2004
 */
package org.jkiss.dbeaver.ext.erd.part;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.swt.SWT;
import org.jkiss.dbeaver.ext.erd.model.ERDAssociation;
import org.jkiss.dbeaver.ext.erd.policy.AssociationBendEditPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the editable primary key/foreign key relationship
 *
 * @author Phil Zoio
 */
public class AssociationPart extends PropertyAwareConnectionPart {

    public void activate() {
        super.activate();
    }

    public void deactivate() {
        super.deactivate();
    }

    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
        //installEditPolicy(EditPolicy.COMPONENT_ROLE, new AssociationEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new AssociationBendEditPolicy());
    }

    protected IFigure createFigure() {
        ERDAssociation association = (ERDAssociation) getModel();

        PolylineConnection conn = (PolylineConnection) super.createFigure();
        conn.setConnectionRouter(new BendpointConnectionRouter());
        conn.setTargetDecoration(new PolygonDecoration());

        //ChopboxAnchor sourceAnchor = new ChopboxAnchor(classFigure);
        //ChopboxAnchor targetAnchor = new ChopboxAnchor(classFigure2);
        //conn.setSourceAnchor(sourceAnchor);
        //conn.setTargetAnchor(targetAnchor);

/*
        ConnectionEndpointLocator relationshipLocator = new ConnectionEndpointLocator(conn, true);
        //relationshipLocator.setUDistance(30);
        //relationshipLocator.setVDistance(-20);
        Label relationshipLabel = new Label(association.getObject().getName());
        conn.add(relationshipLabel, relationshipLocator);
*/

        return conn;
    }

    /**
     * Sets the width of the line when selected
     */
    public void setSelected(int value) {
        super.setSelected(value);
        if (value != EditPart.SELECTED_NONE) {
            ((PolylineConnection) getFigure()).setLineWidth(2);
        } else {
            ((PolylineConnection) getFigure()).setLineWidth(1);
        }
    }

    public void addBendpoint(int bendpointIndex, Point location) {
        Bendpoint bendpoint = new AbsoluteBendpoint(location);
        List<Bendpoint> bendpoints = getBendpoints();
        bendpoints.add(bendpointIndex, bendpoint);
        updateBendpoints(bendpoints);
    }

    public void removeBendpoint(int bendpointIndex) {
        List<Bendpoint> bendpoints = getBendpoints();
        if (bendpointIndex < bendpoints.size()) {
            bendpoints.remove(bendpointIndex);
            updateBendpoints(bendpoints);
        }
    }

    public void moveBendpoint(int bendpointIndex, Point location) {
        Bendpoint bendpoint = new AbsoluteBendpoint(location);
        List<Bendpoint> bendpoints = getBendpoints();
        if (bendpointIndex < bendpoints.size()) {
            bendpoints.set(bendpointIndex, bendpoint);
            updateBendpoints(bendpoints);
        }
    }

    private List<Bendpoint> getBendpoints() {
        Object constraint = getConnectionFigure().getRoutingConstraint();
        if (constraint instanceof List) {
            // Make constraint copy
            List<Bendpoint> curList = (List<Bendpoint>) constraint;
            return new ArrayList<Bendpoint>(curList);
        } else {
            return new ArrayList<Bendpoint>();
        }
    }

    private void updateBendpoints(List<Bendpoint> bendpoints) {
        getConnectionFigure().setRoutingConstraint(bendpoints);
    }
}