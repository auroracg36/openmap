// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/StandardMapMouseInterpreter.java,v $
// $RCSfile: StandardMapMouseInterpreter.java,v $
// $Revision: 1.2 $
// $Date: 2003/09/22 23:24:12 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;

public class StandardMapMouseInterpreter implements MapMouseInterpreter, MapMouseListener {

    protected boolean DEBUG = false;
    protected OMGraphicHandlerLayer layer = null;
    protected String[] mouseModeServiceList = null;
    protected String lastToolTip = null;
    protected GestureResponsePolicy grp = null;
    protected GeometryOfInterest clickInterest = null;
    protected GeometryOfInterest movementInterest = null;

    public StandardMapMouseInterpreter() {
	DEBUG = Debug.debugging("grp");
    }

    public StandardMapMouseInterpreter(OMGraphicHandlerLayer l) {
	setLayer(l);
    }

    public class GeometryOfInterest {
	OMGraphic omg;
	int button;
	boolean leftButton;

	public GeometryOfInterest(OMGraphic geom, MouseEvent me) {
	    omg = geom;
	    button = me.getButton();
	    leftButton = isLeftMouseButton(me);
	}

	public boolean appliesTo(OMGraphic geom) {
	    return (geom == omg);
	}

	public boolean appliesTo(OMGraphic geom, MouseEvent me) {
	    return (geom == omg && sameButton(me));
	}

	public boolean sameButton(MouseEvent me) {
	    return button == me.getButton();
	}
	
	public OMGraphic getGeometry() {
	    return omg;
	}

	public int getButton() {
	    return button;
	}

	public boolean isLeftButton() {
	    return leftButton;
	}
    }

    public void setLayer(OMGraphicHandlerLayer l) {
	layer = l;
    }

    public OMGraphicHandlerLayer getLayer() {
	return layer;
    }

    public void setMouseModeServiceList(String[] list) {
	mouseModeServiceList = list;
    }

    public boolean isLeftMouseButton(MouseEvent me) {
	return SwingUtilities.isLeftMouseButton(me) && !me.isControlDown();
    }

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener.  You MUST override this with the modes you're
     * interested in.
     */
    public String[] getMouseModeServiceList() {
	return mouseModeServiceList;
    }

    protected void setClickInterest(GeometryOfInterest goi) {
	clickInterest = goi;
    }

    protected GeometryOfInterest getClickInterest() {
	return clickInterest;
    }

    protected void setMovementInterest(GeometryOfInterest goi) {
	movementInterest = goi;
    }

    protected GeometryOfInterest getMovementInterest() {
	return movementInterest;
    }

    /**
     * Return the OMGraphic object that is under a mouse event
     * occurance on the map, null if nothing applies.
     */
    public OMGraphic getGeometryUnder(MouseEvent me) {
	OMGraphic omg = null;
	OMGraphicList list = null;
	if (layer != null) {
	    list = layer.getList();
	    if (list != null) {
		omg = list.findClosest(me.getX(), me.getY(), 4);
	    }
	}
	return omg;
    }

    // Mouse Listener events
    ////////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mousePressed(MouseEvent e) { 
	boolean ret = false;

	GeometryOfInterest goi = getClickInterest();
	OMGraphic omg = getGeometryUnder(e);

	if (goi != null && !goi.appliesTo(omg, e)) {
	    // If the click doesn't match the geometry or button
	    // of the geometry of interest, need to tell the goi
	    // that is was clicked off, and set goi to null.
	    if (goi.isLeftButton()) {
		leftClickOff(goi.getGeometry(), e);
	    } else {
		rightClickOff(goi.getGeometry(), e);
	    }
	    setClickInterest(null);
	}

	if (omg != null && grp.isSelectable(omg)) {
	    setClickInterest(new GeometryOfInterest(omg, e));
	    ret = true;
	}

	return ret;
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseReleased(MouseEvent e) {
	return false;
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseClicked(MouseEvent e) {
	GeometryOfInterest goi = getClickInterest();

	// If there is a click interest
	if (goi != null) {
	    // Tell the policy it an OMGraphic was clicked.
	    if (isLeftMouseButton(e)) {
		leftClick(goi.getGeometry(), e);
	    } else {
		rightClick(goi.getGeometry(), e);
	    }
	} else {
	    if (isLeftMouseButton(e)) {
		leftClick(e);
	    } else {
		rightClick(e);
	    }
	}

	return true;
    }

    /**
     * Invoked when the mouse enters a component.
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {}

    // Mouse Motion Listener events
    ///////////////////////////////

    /**
     * Invoked when a mouse button is pressed on a component and then 
     * dragged.  The listener will receive these events if it
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseDragged(MouseEvent e) {
	GeometryOfInterest goi = getClickInterest();
	if (goi != null) {
	    setClickInterest(null);
	}

	return mouseMoved(e);
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {
	boolean ret = false;

	OMGraphic omg = getGeometryUnder(e);
	GeometryOfInterest goi = getMovementInterest();

	if (omg != null && grp != null) {

	    // This gets called if the goi is new or if the goi
	    // refers to a different OMGraphic as previously noted.
	    if (goi == null || !goi.appliesTo(omg)) {

		if (goi != null) {
		    mouseNotOver(goi.getGeometry());
		}

		goi = new GeometryOfInterest(omg, e);
		setMovementInterest(goi);
		mouseOver(omg, e);
	    }

	} else {
	    if (goi != null) {
		mouseNotOver(goi.getGeometry());
		setMovementInterest(null);
	    }
	    mouseOver(e);
	}

	return ret;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {
	GeometryOfInterest goi = getMovementInterest();
	if (goi != null) {
	    mouseNotOver(goi.getGeometry());
	    setMovementInterest(null);
	}
    }

    public boolean leftClick(MouseEvent me) {
	if (DEBUG) {
	    Debug.output("leftClick(MAP) at " + me.getX() + ", " + me.getY());
	}

	return false;
    }

    public boolean leftClick(OMGraphic omg, MouseEvent me) {
	if (DEBUG) {
	    Debug.output("leftClick(" + omg.getClass().getName() + ") at " + 
			 me.getX() + ", " + me.getY());
	}

	if (grp != null && grp.isSelectable(omg)) {
	    OMGraphicList omgl = new OMGraphicList();
	    omgl.add(omg);
	    grp.select(omgl);
	}

	return true;
    }

    public boolean leftClickOff(OMGraphic omg, MouseEvent me) {
	if (DEBUG) {
	    Debug.output("leftClickOff(" + omg.getClass().getName() + ") at " + 
			 me.getX() + ", " + me.getY());
	}

	return false;
    }

    public boolean rightClick(MouseEvent me) {
	if (DEBUG) {
	    Debug.output("rightClick(MAP) at " + me.getX() + ", " + me.getY());
	}

	return false;
    }

    public boolean rightClick(OMGraphic omg, MouseEvent me) {
	if (DEBUG) {
	    Debug.output("rightClick(" + omg.getClass().getName() + ") at " + 
			 me.getX() + ", " + me.getY());
	}

	return true;
    }

    public boolean rightClickOff(OMGraphic omg, MouseEvent me) {
	if (DEBUG) {
	    Debug.output("rightClickOff(" + omg.getClass().getName() + ") at " + 
			 me.getX() + ", " + me.getY());
	}

	return false;
    }

    public boolean mouseOver(MouseEvent me) {
	if (DEBUG) {
	    Debug.output("mouseOver(MAP) at " + me.getX() + ", " + me.getY());
	}

	return false;
    }

    public boolean mouseOver(OMGraphic omg, MouseEvent me) {
	if (DEBUG) {
	    Debug.output("mouseOver(" + omg.getClass().getName() + ") at " + 
			 me.getX() + ", " + me.getY());
	}

	if (grp != null) {
	    lastToolTip = grp.getToolTipTextFor(omg);


	    if (grp.isHighlightable(omg)) {
		grp.highlight(omg);
	    }

	    if (layer != null) {

		if (lastToolTip != null) {
		    layer.fireRequestToolTip(me, lastToolTip);
		} else {
		    layer.fireHideToolTip(me);
		}

		String infoText = grp.getInfoText(omg);
		if (infoText != null) {
		    layer.fireRequestInfoLine(infoText);
		}
	    }
	}
	return true;
    }

    public boolean mouseNotOver(OMGraphic omg) {
	if (DEBUG) {
	    Debug.output("mouseNotOver(" + omg.getClass().getName() + ")");
	}

	if (grp != null) {
	    grp.unhighlight(omg);
	}

	if (layer != null) {
	    if (lastToolTip != null) {
		layer.fireHideToolTip(null);
	    }
	    lastToolTip = null;

	    layer.fireRequestInfoLine("");
	}
	return false;
    }

    public boolean keyPressed(OMGraphic omg, int virtualKey) {
	if (DEBUG) {
	    Debug.output("keyPressed(" + omg.getClass().getName() + " , " + virtualKey + ")");
	}
	return true;
    }

    public void setGRP(GestureResponsePolicy grp) {
	this.grp = grp;
    }

    public GestureResponsePolicy getGRP() {
	return grp;
    }

}