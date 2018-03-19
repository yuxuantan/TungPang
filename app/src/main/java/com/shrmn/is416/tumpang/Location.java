package com.shrmn.is416.tumpang;

public class Location {
    private String name;
    private String address;
    private long beaconIDMajor;
    private long beaconIDMinor;
    private Menu menu;

    public Location (String name, String address, long beaconIDMajor, long beaconIDMinor, Menu menu){
        this.name = name;
        this.address = address;
        this.beaconIDMajor = beaconIDMajor;
        this.beaconIDMinor = beaconIDMinor;
        this.menu = menu;
    }

    public Menu getMenu() {
        return menu;
    }
}
