package com.shrmn.is416.tumpang;

import java.io.Serializable;

public class Location implements Serializable {
    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBeaconMacAddress() {
        return beaconMacAddress;
    }

    public void setBeaconMacAddress(String beaconMacAddress) {
        this.beaconMacAddress = beaconMacAddress;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    private String locationID;
    private String name;
    private String address;
    private String beaconMacAddress;
    private Menu menu;

    public Location (String locationID, String name, String address, String beaconMacAddress, Menu menu){
        this.locationID = locationID;
        this.name = name;
        this.address = address;
        this.beaconMacAddress = beaconMacAddress;
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
                ", beaconMacAdd=" + beaconMacAddress +
//                ", beaconIDMinor=" + beaconIDMinor +
                ", menu=" + menu +
                '}';
    }
}
