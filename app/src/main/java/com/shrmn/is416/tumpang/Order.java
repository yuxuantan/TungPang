package com.shrmn.is416.tumpang;

import java.util.ArrayList;
import java.util.HashMap;

public class Order {
    private String orderID;
    private String locationID;
    private String locationName;
    private Location location;
    // How much commission earned can be calculated on app, then no need store, if its just a percentage of menuPrice
    private double tipAmount;
    private long estimatedTimeOfDelivery;
    private String deliveryManUserID;
    private String customerUserID;
    private HashMap<MenuItem, Integer> menuItems;

    // String or lat long?? must convert if Lat long
    private String deliveryLocation;
    // 0-“unassigned”, 1-“deliveryInProcess”, 2-“completed”
    private int status = 0;


    public Order(String orderID, String locationID, String locationName, double tipAmount, long estimatedTimeOfDelivery, String deliveryManUserID, String customerUserID, HashMap<MenuItem, Integer> menuItems, String deliveryLocation, int status) {
        this.orderID = orderID;
        this.locationID = locationID;
        this.locationName = locationName;
        this.tipAmount = tipAmount;
        this.estimatedTimeOfDelivery = estimatedTimeOfDelivery;
        this.deliveryManUserID = deliveryManUserID;
        this.customerUserID = customerUserID;
        this.menuItems = menuItems;
        this.deliveryLocation = deliveryLocation;
        this.status = status;
    }

    public Order(Location location, String locationID, String locationName, double tipAmount, String deliveryLocation) {
        this.location = location;
        this.locationID = locationID;
        this.locationName = locationName;
        this.tipAmount = tipAmount;
        this.deliveryLocation = deliveryLocation;
        this.menuItems = new HashMap<>();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(double tipAmount) {
        this.tipAmount = tipAmount;
    }

    public long getEstimatedTimeOfDelivery() {
        return estimatedTimeOfDelivery;
    }

    public void setEstimatedTimeOfDelivery(long estimatedTimeOfDelivery) {
        this.estimatedTimeOfDelivery = estimatedTimeOfDelivery;
    }

    public String getDeliveryManUserID() {
        return deliveryManUserID;
    }

    public void setDeliveryManUserID(String deliveryManUserID) {
        this.deliveryManUserID = deliveryManUserID;
    }

    public String getCustomerUserID() {
        return customerUserID;
    }

    public void setCustomerUserID(String customerUserID) {
        this.customerUserID = customerUserID;
    }

    public HashMap<MenuItem, Integer> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(HashMap<MenuItem, Integer> menuItems) {
        this.menuItems = menuItems;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void addMenuItem(MenuItem menuItem, int quantity) {
        menuItems.put(menuItem, quantity);
    }

    public void removeMenuItem(MenuItem menuItem) {
        menuItems.remove(menuItem);
    }

}


