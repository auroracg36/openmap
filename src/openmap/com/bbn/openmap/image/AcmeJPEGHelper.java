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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/Attic/AcmeJPEGHelper.java,v $
// $RCSfile: AcmeJPEGHelper.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.image;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.net.URL;

import com.bbn.openmap.util.Debug;

/**
 * This class provides some utility methods for creating jpeg encoded
 * images.  It relies on the Acme Encoders, available at
 * http://www.acme.com.  A copy has been included in the contrib
 * directory.
 */
public class AcmeJPEGHelper {

    /**
     * This class has only static methods, so there is no need to
     * construct anything.
     */
    private AcmeJPEGHelper() {};

    /** 
     * Return a byte array that contains the JPEG encoded image.
     * @param image the image to encode
     * @param quality the JPEG quality factor to use in encoding 1-100
     * @exception IOException an error occured in encoding the image
     */
    public static byte[] encodeJPEG(BufferedImage image, int quality)
	throws IOException {

	ByteArrayOutputStream out = new ByteArrayOutputStream();
	if (Debug.debugging("jpeghelper")){
	    Debug.output("Got output stream..." + out);
	}

	Acme.JPM.Encoders.JpegEncoder enc = new	Acme.JPM.Encoders.JpegEncoder(image, out);
	enc.setQfactor(quality);
	if (Debug.debugging("jpeghelper")){
	    Debug.output("Got jpeg encoder...");
	}

	enc.encode();
	if (Debug.debugging("jpeghelper")){
	    Debug.output("encoded?");
	}
	
	return out.toByteArray();
    }
	
    /** 
     * Return a byte array that contains the JPEG encoded image.
     * @param w the width of the image
     * @param h the height of the image 
     * @param pixels the array of pixels in RGB directcolor
     * @param quality the JPEG quality factor to use in encoding
     * @exception IOException an error occured in encoding the image
     */
    public static byte[] encodeJPEG(int w, int h, int[] pixels, int quality)
	throws IOException {
	BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	bi.setRGB(0, 0, w, h, pixels, 0, w);
	pixels = null;
	return encodeJPEG(bi, quality);
    }

    /**
     * A test main that encodes an image url at various
     * jpeg quality factors.
     * @param args url [width height]
     */
    public static void main(String args[]) throws Exception {
	if (args.length == 0) {
	    System.out.println("java jpeg url [width height]");
	    System.exit(-1);
	}

	Debug.init();

	int uw = -1;
	int uh = -1;
	if (args.length > 1) {
	    uw = Integer.parseInt(args[1]);
	    uh = Integer.parseInt(args[2]);
	}
	 
	String urlsource = args[0];
	int lastslash = urlsource.lastIndexOf('/');
	if (lastslash == -1) {
	    lastslash = 0;
	} else {
	    lastslash++;
	}
	int lastdot = urlsource.lastIndexOf('.');
	if (lastdot == -1) {
	    lastdot = 0;
	}
	String filebase = urlsource.substring(lastslash, lastdot);
       
	Debug.output("url=" + urlsource + " filebase=" + filebase);

	BufferedImage bi = BufferedImageHelper.getBufferedImage(new URL(urlsource), 0, 0, uw, uh);
	if (bi == null) {
	    Debug.error("JPEGHelper: Image load failed");
	} else {
	    PrintStream html = new PrintStream(new FileOutputStream(new File(filebase + ".html")));
	
	    html.println("Source url = " + urlsource + " <br>");
	    html.println(" width = "+uw + " height=" + uh + 
			 " pixels=" + uw*uh + " <hr>");
	    for (int i = 0; i < 20; i++) {
		File f = new File(filebase+((i<10)?"0":"")+i+".jpg");
		int quality = (int)(0.0499f * i * 100.0);
		byte data[] = encodeJPEG(bi, quality);
		OutputStream writef = new FileOutputStream(f);
		writef.write(data);
		writef.close();
		html.println("Image Quality Factor: " + quality + " <br>");
		html.println("Image Size (bytes) : " + data.length + " <br>");
		html.println("<img src=\"" + f.getName() + "\"> <hr>");
	    }
	
	    html.close();
	}
	System.exit(-1); //awt stinks
    }
}