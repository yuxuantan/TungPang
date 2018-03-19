package com.shrmn.is416.tumpang;

public class Location {
    private String locationID;
    private String name;
    private String address;
    private long beaconIDMajor;
    private long beaconIDMinor;
    private Menu menu;

    public Location (String locationID, String name, String address, long beaconIDMajor, long beaconIDMinor, Menu menu){
        this.locationID = locationID;
        this.name = name;
        this.address = address;
        this.beaconIDMajor = beaconIDMajor;
        this.beaconIDMinor = beaconIDMinor;
        this.menu = menu;
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        return locationID != null ? locationID.equals(location.locationID) : location.locationID == null;
    }

    @Override
    public int hashCode() {
        return locationID != null ? locationID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Location{" +
                "locationID='" + locationID + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", beaconIDMajor=" + beaconIDMajor +
                ", beaconIDMinor=" + beaconIDMinor +
                ", menu=" + menu +
                '}';
    }
}
