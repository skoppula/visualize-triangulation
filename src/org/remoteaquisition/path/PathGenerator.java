package org.remoteaquisition.path;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.processing.wiki.triangulate.Triangle;
import org.remoteaquisition.CoordinateConversion.CoordinateConversion;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;

public class PathGenerator extends PApplet {
	static int winX = 1000;
	static int winY = 600;
	
	static int lineY = 50;
	static int startX = 10;
	static int startY = 80;
	static float velocity = 50; // mph
	static float bScale = 0;
	static float framedist = 0; // displacement (pixels/frame interval)
	
	String coordinateSystem = "UTM";
	String datum = "WGS84";
	String gridZone = "17 T";
	String winTitle = "REMOTE AQUISITION AND DISTRIBUTION OF OCEANOGRAPHIC DATA";
	String subTitle = "A rendering of the path traversed by the Boat";
	String timestr = String.valueOf(month()) + "/" +
					 String.valueOf(day()) + "/" +
	                 String.valueOf(year()) + "  " +
	                 String.valueOf(hour()) + ":" +
	                 String.valueOf(minute()) + ":" +
	                 String.valueOf(second());
	float x0 = 0;
	float y0 = 0;
	ArrayList<Point> path = new ArrayList<Point>();
	int idx = 0;

	PFont body;
	static ArrayList<Region> regions = new ArrayList<Region>();
	
	public PathGenerator() {
	}
	
	ArrayList<double[]> readBoundary(String boundaryPointsFile)
						throws FileNotFoundException, IOException {
		ArrayList<double[]> boundary = new ArrayList<double[]>();
		CoordinateConversion cc = new CoordinateConversion();

		// Read boundary points (in decimal LatLon format) from file.
		// Convert them into UTM format.
		BufferedReader in
		= new BufferedReader(new FileReader(boundaryPointsFile));
		String[] a;
		for (String s = in.readLine(); s != null;) {
			a = s.split(",\\s*",2);
			a = cc.latLon2UTM(Double.parseDouble(a[0]),
					Double.parseDouble(a[1])).split(" \\s*",4);
			boundary.add(new double[] {Double.parseDouble(a[2]),
					Double.parseDouble(a[3])});
			s = in.readLine();
		}
		return boundary;
	}

	double[][] translateBoundary(double[][] boundary) {
		double[][] translatedBoundary =
			new double[boundary.length][2];
		
		// Translate the frame containing the boundary
		// with the lowest (easting, northing) as origin

		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		for (int i=0; i < boundary.length; i++) {
			if (boundary[i][0] < minX)
				minX = boundary[i][0];
			if (boundary[i][1] < minY)
				minY = boundary[i][1];
			
			if (boundary[i][0] > maxX)
				maxX = boundary[i][0];
			if (boundary[i][1] > maxY)
				maxY = boundary[i][1];
		}
		
		bScale = (float)(((maxX - minX) < (maxY - minY)) ?
				 		((winX - 2*startX) / (maxX - minX)):
				 		((winY - 2*startY) / (maxY - minY)));
		
		for (int i=0; i < boundary.length; i++) {			
			translatedBoundary[i][0] = (bScale *
					(boundary[i][0] - minX)) + startX;
			translatedBoundary[i][1] = (bScale *
					(boundary[i][1] - minY)) + startY;
		}

		return translatedBoundary;
	}

	void generatePath() {
		float x, y, x1, y1, x2, y2, x3, y3, d, d1, d2, d3;
		Point pt = null;
		ArrayList<Triangle> tlist = null;
		Region r = null;
		int regionId;
		boolean startingPoint = true;

		for (int i = 0; i <regions.size(); i++) {
			r = regions.get(i);
			regionId = r.getRegionId();
			tlist = r.getTriangles();
			
			for (int j = 0; j < tlist.size(); j++) {
				x1 = tlist.get(j).p1.x;
				y1 = tlist.get(j).p1.y;
				x2 = tlist.get(j).p2.x;
				y2 = tlist.get(j).p2.y;
				x3 = tlist.get(j).p3.x;
				y3 = tlist.get(j).p3.y;
				
				if (startingPoint) {
					x0 = x1;
					y0 = y1;
//					System.out.println ("Adding (" + regionId +
//										", " + x0 + ", " + y0 + ") to path");
					path.add(new Point(regionId, x0, y0, x1, y1, x0, y0));
					startingPoint = false;
				}
				
				d = (float)TriangulatedPolygon.segdist (x0, y0, x1, y1);
				d1 = (float)TriangulatedPolygon.segdist (x1, y1, x2, y2);
				d2 = (float)TriangulatedPolygon.segdist (x2, y2, x3, y3);
				d3 = (float)TriangulatedPolygon.segdist (x3, y3, x1, y1);
				
				for (int k = 1; k <= (int) Math.floor(d/framedist); k++) {
					x = x0 + (framedist/d) * (x1 - x0) * k;
					y = y0 + (framedist/d) * (y1 - y0) * k;
					
					if (x != x1)
						path.add(new Point(regionId, x0, y0, x1, y1, x, y));
				}

				path.add(new Point(regionId, x1, y1, x2, y2, x1, y1));
				for (int k = 1; k <= (int) Math.floor(d1/framedist); k++) {
					x = x1 + (framedist/d1) * (x2 - x1) * k;
					y = y1 + (framedist/d1) * (y2 - y1) * k;
					
					if (x != x2)
						path.add(new Point (regionId, x1, y1, x2, y2, x, y));
				}

				path.add(new Point (regionId, x2, y2, x3, y3, x2, y2));
				for (int k = 1; k <= (int) Math.floor(d2/framedist); k++) {
					x = x2 + (framedist/d2) * (x3 - x2) * k;
					y = y2 + (framedist/d2) * (y3 - y2) * k;
					
					if (x != x3)
						path.add(new Point (regionId, x2, y2, x3, y3, x, y));
				}

				path.add(new Point (regionId, x3, y3, x1, y1, x3, y3));
				for (int k = 1; k <= (int) Math.floor(d3/framedist); k++) {
					x = x3 + (framedist/d3) * (x1 - x3) * k;
					y = y3 + (framedist/d3) * (y1 - y3) * k;
					
					if (x != x1)
						path.add(new Point (regionId, x3, y3, x1, y1, x, y));
				}
				x0 = x1; y0 = y1;
			}
		}
	}
	
	public void setup() {
	    size(winX, winY);
	    frame.setTitle(winTitle);
	    background(0);
//		background(200);
	    stroke(255);
	    frameRate(10); // frames per second
	    
		framedist = (velocity / frameRate)*0.447f*bScale;

	    // Setup title area.
	    textFont(loadFont("CourierNew36.vlw"), 20);
	    textAlign(CENTER);
	    text(winTitle, winX/2, 20);
	    textFont(loadFont("Eureka-90.vlw"), 15);
	    text(subTitle, winX/2, 40);
	    line(10, lineY, winX-10, lineY);
	    line(10, lineY+2, winX-10, lineY+2);
	    
	    textFont(loadFont("TheSans-Plain-12.vlw"));
	    textAlign(RIGHT);
	    text("Coordinate System: ", winX-110, startY);
	    text("Datum: ", winX-110, startY+20);
	    text("Grid Zone: ", winX-110, startY+40);
	    text("Velocity: ", winX-110, startY+60);
	    text("Scale: ", winX-110, startY+80);
	    text("Date: ", winX-110, startY+100);
	    text("Region: ", winX-110, startY+140);
	    text("Location From (Adjusted UTM): ", winX-110, startY+160);
	    text("Location To (Adjusted UTM): ", winX-110, startY+180);
	    text("Current Location (Adjusted UTM): ", winX-110, startY+200);
	    
	    textAlign(LEFT);
	    text(coordinateSystem, winX-108, startY);
	    text(datum, winX-108, startY+20);
	    text(gridZone, winX-108, startY+40);
	    text(velocity + " mph", winX-108, startY+60);
	    text("1 pixel : " + nf(1/bScale,3,2) + " m",
	    		winX-108, startY+80);
	    text(timestr, winX-108, startY+100);

		smooth();
		
		ArrayList<PVector> blist = null;
		ArrayList<Triangle> tlist = null;
		Region r = null;
		int regionId;

		for (int i = 0; i <regions.size(); i++) {
			r = regions.get(i);
			regionId = r.getRegionId();
//			System.out.println ("Region (" + i + ") regionId: " + regionId);

			// Draw points in the region
			blist = r.getBoundary();
//			System.out.println("    Boundary (" + blist.size() + "): ");
			
			noStroke();
			fill(255, 0, 0);
	
			for (int j = 0; j < blist.size(); j++) {
//				System.out.println("        (" + blist.get(j).x +
//								   ", " + blist.get(j).y + ") ");
//				text("(" + j + ")", blist.get(j).x + 10, blist.get(j).y +10);
				ellipse(blist.get(j).x, blist.get(j).y, 2.5f, 2.5f);
			}

			// Draw triangles in the region
			tlist = r.getTriangles();
//			System.out.println("    Triangles (" + tlist.size() + "): ");
			
			stroke(0, 40);
			fill (41, 178, 197);
			beginShape(TRIANGLES);

			for (int j = 0; j < tlist.size(); j++) {
//				System.out.println("        (" +
//								   tlist.get(j).p1.x + ", " +
//								   tlist.get(j).p1.y + "), (" +
//								   tlist.get(j).p2.x + ", " +
//								   tlist.get(j).p2.y + "), (" +
//								   tlist.get(j).p3.x + ", " +
//								   tlist.get(j).p3.y + ") ");
				vertex(tlist.get(j).p1.x, tlist.get(j).p1.y);
				vertex(tlist.get(j).p2.x, tlist.get(j).p2.y);
				vertex(tlist.get(j).p3.x, tlist.get(j).p3.y);
			}
			endShape();
		}
		
		generatePath();
		System.out.println ("Total points in path: " + path.size());
	}

	public void draw() {
		Point pt = path.get(idx);
		int regionId = pt.getRegionId();
		float fromX = (float)pt.getFromX();
		float fromY = (float)pt.getFromY();
		float toX = (float)pt.getToX();
		float toY = (float)pt.getToY();
		float x = (float)pt.getX();
		float y = (float)pt.getY();
		
		fill(0);
		noStroke();
	    rect(winX-108, startY+130,100, 80);
	    fill(255);
	    text(regionId, winX-108, startY+140);
	    text("(" + nf(fromX,3,2) + ", " + nf(fromY,3,2) + ")", winX-108, startY+160);
	    text("(" + nf(toX,3,2) + ", " + nf(toY,3,2) + ")", winX-108, startY+180);
	    text("(" + nf(x,3,2) + ", " + nf(y,3,2) + ")", winX-108, startY+200);

	    noFill();
	    noStroke();
//		System.out.println (idx + " (" + regionId + ", " + x + ", " + y + ")");
		fill(255, 0, 0);
		ellipse(x, y, 4f, 4f);

		idx++;
	}

	public void mousePressed() {
	  saveFrame("data/path-frame.jpg");
	}
	
	static public void main(String args[]) {
		PathGenerator pg = new PathGenerator();
		double[] boundary[] = new double[0][0];
		
        try {
        	boundary = pg.readBoundary(args[0]).
        				toArray((double[][]) boundary);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		double[][] translatedBoundary =
						pg.translateBoundary(boundary);
		
//    	for (int i=0; i <translatedBoundary.length; i++)
//    		System.out.println (
//    					"(" + boundary[i][0] +
//						", " + boundary[i][1] +
//		    			") => (" + translatedBoundary[i][0] +
//					    ", " + translatedBoundary[i][1] + ")");

    	// Divide the polygon into nested regions (polygons) and
    	// triangulate them
		TriangulatedPolygon tp = new TriangulatedPolygon();
		tp.setBoundary(translatedBoundary);
		tp.triangulate();
		regions = tp.getRegions();
		
		PApplet.main(new String[] {"--bgcolor=#ECE9D8",
					"org.remoteaquisition.path.PathGenerator"});
	}
}
