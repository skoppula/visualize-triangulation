package org.remoteaquisition.path;

import java.util.ArrayList;

import org.remoteaquisition.poly2Tri.Triangulation;

public class TriangulatedPolygon {
	public static final int triangulationDepth = 2;
	public static final double seglen = 50.0;
	
	private double[] boundary[] = null;
	private ArrayList<ArrayList<Integer>> triangles = null;
	
	private ArrayList<TriangulatedPolygon> tps = 
		new ArrayList<TriangulatedPolygon>();
	
	public TriangulatedPolygon() {
	}
	
	public double[][] getBoundary() {
		return this.boundary;
	}

	public void setBoundary(double[][] boundary) {
		this.boundary = boundary;
	}
	
	public ArrayList<TriangulatedPolygon> getTriangulatedPolygons () {
		return tps;
	}
	
	public ArrayList<ArrayList<Integer>> getTriangles() {
		return triangles;
	}
	
	public static double segdist (double x1, double y1, double x2, double y2) {
		return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	}

	public void triangulate () {
		triangulate (1);
	}
	
	public ArrayList<ArrayList<Integer>> triangulate (int tlevel) {
		System.out.println("Level " + tlevel + ", boundary " +
							boundary.length);
		
		triangles = (ArrayList<ArrayList<Integer>>)
			Triangulation.triangulate(1, new int[]{
				boundary.length}, boundary);
		
		System.out.println ("Triangles generated: " +
				((triangles == null) ? "None" : triangles.size()));
		
		if (triangles == null)
			return null;
		
		if (tlevel < triangulationDepth) {
			ArrayList<Integer> t = null;
			double x, y, x1, x2, x3, y1, y2, y3, d1, d2, d3;
			ArrayList<double[]> b = null;
			double[] b2[] = null;
			TriangulatedPolygon tp = null;

			for (int i = 0; i < triangles.size(); ++i){
				t = (ArrayList<Integer>)triangles.get(i);
				x1 = boundary[t.get(0)][0];
				y1 = boundary[t.get(0)][1];
				x2 = boundary[t.get(1)][0];
				y2 = boundary[t.get(1)][1];
				x3 = boundary[t.get(2)][0];
				y3 = boundary[t.get(2)][1];
				d1 = segdist (x1, y1, x2, y2);
				d2 = segdist (x2, y2, x3, y3);
				d3 = segdist (x3, y3, x1, y1);
						
				System.out.println ("Triangle " + ": ((" +
						triangles.get(i).get(0) + " (" + x1 + ", " + y1 + "), " +
						triangles.get(i).get(1) + " (" + x2 + ", " + y2 + "), " +
						triangles.get(i).get(2) + " (" + x3 + ", " + y3 + ") [" +
						d1 + ", " + d2 + ", " + d3 + "]"
						);
				
				if ((d1 > seglen) || (d2 > seglen) || (d3 > seglen)) {
					b = new ArrayList<double[]>();
					b.add(new double[] {x1, y1});
					for (int k = 1; k <= (int) Math.floor(d1/seglen); k++) {
						x = x1 + (seglen/d1) * (x2 - x1) * k;
						y = y1 + (seglen/d1) * (y2 - y1) * k;
						
						if (x != x2) {
							b.add(new double[] {x, y});
							System.out.println ("Dividing d1 at (" +
									x + ", " + y +
									") between (x1,y1) - (x2, y2): (" +
									x1 + ", " + y1 + ") - (" +
									x2 + ", " + y2 + ")");
						}
					}
					b.add(new double[] {x2, y2});
					for (int k = 1; k <= (int) Math.floor(d2/seglen); k++) {
						x = x2 + (seglen/d2) * (x3 - x2) * k;
						y = y2 + (seglen/d2) * (y3 - y2) * k;
						
						if (x != x3) {
							b.add(new double[] {x, y});
							System.out.println ("Dividing d2 at (" +
									x + ", " + y +
									") between (x2, y2) - (x3, y3): (" +
									x2 + ", " + y2 + ") - (" +
									x3 + ", " + y3 + ")");
						}
					}
					b.add(new double[] {x3, y3});
					for (int k = 1; k <= (int) Math.floor(d3/seglen); k++) {
						x = x3 + (seglen/d3) * (x1 - x3) * k;
						y = y3 + (seglen/d3) * (y1 - y3) * k;
						
						if (x != x1) {
							b.add(new double[] {x, y});
							System.out.println ("Dividing d3 at (" + x +
									", " + y +
									") between (x3,y3) - (x1, y1): (" +
									x3 + ", " + y3 + ") - (" +
									x1 + ", " + y1 + ")");
						}
					}
					
					tp = new TriangulatedPolygon();
					tp.setBoundary(b.toArray((double[][]) new double[0][0]));
					tp.triangulate (tlevel+1);
					
					if (tp.triangles == null) {
						System.out.println ("Re-trying triangulation.");
						b2 = new double[b.size()][2];
						for (int j = 0; j < b.size(); j++) {
							b2[j] = new double[] {b.get(b.size()-j-1)[0],
									b.get(b.size()-j-1)[1]};
						}
						tp.setBoundary(b2);
						tp.triangulate (tlevel+1);
						if (tp.triangles == null) {
							System.out.println ("ERROR: Unable to triangulate.");
						}
					}
					
					tps.add(tp);

					System.out.println ("Converted a triangle to a region.");
					triangles.remove(i--);
				}
			}
		}
		return triangles;
	}
	
	public ArrayList<Region> getRegions() {
		ArrayList<Region> regions = new ArrayList<Region>();
		ArrayList<Region> rlist = null;
		int regionId = 0;
		
		System.out.println ("Adding a region with boundary (" +
							boundary.length + "), triangles (" +
							triangles.size() + ")");
		Region r = new Region(regionId++);
		r.generateBoundary(boundary);
		r.generateTriangles(triangles);
		regions.add(r);
		System.out.println ("Region with boundary (" +
							r.getBoundary().size() + "), triangles (" +
							r.getTriangles().size() + ")");

		for (int i = 0; i < tps.size(); i++) {
			rlist = tps.get(i).getRegions();
			for (int j = 0; j < rlist.size(); j++) {
				r = rlist.get(j);
				r.setRegionId(regionId++);
				regions.add(r);
			}
		}
    	
		return regions;
	}
}
