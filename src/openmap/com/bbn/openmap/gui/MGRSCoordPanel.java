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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/MGRSCoordPanel.java,v $
// $RCSfile: MGRSCoordPanel.java,v $
// $Revision: 1.4 $
// $Date: 2004/05/10 20:43:03 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serializable;

import javax.swing.*;
import javax.swing.border.*;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.proj.coords.MGRSPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Environment;

/**
 * MGRSCoordPanel is a simple gui with an entry box for a MGRS
 * coordinate. It sets the center of a map by firing CenterEvents.
 */
public class MGRSCoordPanel extends CoordPanel implements Serializable {

    protected transient JTextField mgrs;
    
    /**
     *  Creates the panel.
     */
    public MGRSCoordPanel() {
	super();
    }

    /**
     *  Creates the panel.
     */
    public MGRSCoordPanel(CenterSupport support) {
	super(support);
    }

    /**
     *  Creates and adds the labels and entry fields for latitude and longitude
     */
    protected void makeWidgets() {
     String locText;
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();

	setLayout(gridbag);
        locText = i18n.get(MGRSCoordPanel.class,"border","MGRS Coordinate");
	setBorder(new TitledBorder(new EtchedBorder(), locText));

        locText = i18n.get(MGRSCoordPanel.class,"mgrsLabel","MGRS: ");
	JLabel mgrsLabel = new JLabel(locText);
	c.gridx = 0;
	gridbag.setConstraints(mgrsLabel, c);
	add(mgrsLabel);

	mgrs = new JTextField(20);
	c.gridx = 1;
	gridbag.setConstraints(mgrs, c);
	add(mgrs);
    }

    /**
     *  @return the LatLonPoint represented by contents of the entry boxes
     */
    public LatLonPoint getLatLon() {

	String mgrsString;

	try {
	    // Allow blank minutes and seconds fields to represent zero
	    
	    
	    return new MGRSPoint(mgrs.getText()).toLatLonPoint();

	} catch (NumberFormatException except) {
//  	    System.out.println(except.toString());
	    clearTextBoxes();
	}
	return null;
    }

    /**
     *  Sets the contents of the latitude and longitude entry boxes
     *  @param llpoint the object containing the coordinates that
     *  should go in the boxes.
     */
     public void setLatLon(LatLonPoint llpoint) {
	 if (llpoint == null) {
	     clearTextBoxes();
	     return;
	 }

	 MGRSPoint mgrsp = new MGRSPoint(llpoint);
	 mgrs.setText(mgrsp.getMGRS());
     }

    protected void clearTextBoxes() {
	mgrs.setText("");
    }
}
