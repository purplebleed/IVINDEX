package model;

/**
 * Created by linleo on 8/10/2016.
 */
public class Point {

    private double lat;
    private double lon;

    public Point(double la, double lo){
        lat = la;
        lon = lo;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
