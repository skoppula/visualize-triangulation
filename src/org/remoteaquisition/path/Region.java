package org.remoteaquisition.path;

import java.util.ArrayList;

import org.processing.wiki.triangulate.Triangle;

import processing.core.PVector;

public class Region {
	Region(int regionId) {
		this.regionId = regionId;
	}
	int regionId = 0;
	ArrayList<PVector> boundary = new ArrayList<PVector>();
	ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	
	int getRegionId () {
		return regionId;
	}
	
	void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	
	ArrayList<PVector> getBoundary () {
		return boundary;
	}
	
	ArrayList<Triangle> getTriangles () {
		return triangles;
	}
	
	public void generateBoundary(double[][] b) {
	    for (int i = 0; i < b.length; ++i)
    	boundary.add(new PVector ((float)b[i][0],
    						   (float)b[i][1]));
	}
	
	public void generateTriangles(ArrayList<ArrayList<Integer>> ts) {
	    ArrayList<Integer> t = null;
	    for (int i = 0; i < ts.size(); ++i){
	    	t = (ArrayList<Integer>)ts.get(i);
	    	triangles.add(new Triangle(
	    				boundary.get((Integer)t.get(0)),
	    				boundary.get((Integer)t.get(1)),
	    				boundary.get((Integer)t.get(2))));
	    }
	}
}

