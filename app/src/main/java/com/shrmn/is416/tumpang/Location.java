package com.shrmn.is416.tumpang;

import java.util.ArrayList;

public class Location {
    private String name;
    private String address;
    private long beaconIDMajor;
    private long beaconIDMinor;
    private Menu menu;

    private class Menu {
        private ArrayList<drink> drinksList;
        private ArrayList<food> foodList;
    }

    private class drink {
        private String name;
        private double unitPrice;
    }

    private class food {
        private String name;
        private double unitPrice;
    }

    public Location (String name, String address, long beaconIDMajor, long beaconIDMinor){
        this.name = name;
        this.address = address;
        this.beaconIDMajor = beaconIDMajor;
        this.beaconIDMinor = beaconIDMinor;
        Menu menu = new Menu();
    }

    public void setMenuList(Menu menu) {
        this.menu = menu;
    }

    public Menu getMenuList() {
        return this.menu;
    }
}
