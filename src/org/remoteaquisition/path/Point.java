package org.remoteaquisition.path;

class Point {
	double x, y;
	double fromX, fromY, toX, toY;
	int regionId;
	
	Point(int regionId, double fromX, double fromY,
						double toX, double toY,
						double x, double y) {
		this.regionId = regionId;
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
		this.x = x;
		this.y = y;
	}

	Point(int regionId, double x, double y) {
		this.regionId = regionId;
		this.x = x;
		this.y = y;
	}
	
	int getRegionId () {
		return regionId;
	}
	
	double getX () {
		return x;
	}
	
	double getY () {
		return y;
	}
	
	double getFromX () {
		return fromX;
	}
	
	double getFromY () {
		return fromY;
	}
	
	void setFromX (double fromX) {
		this.fromX = fromX;
	}
	
	void setFromY (double fromY) {
		this.fromY = fromY;
	}

	double getToX () {
		return toX;
	}
	
	double getToY () {
		return toY;
	}
	
	void setToX (double toX) {
		this.toX = toX;
	}
	
	void setToY (double toY) {
		this.toY = toY;
	}
}

