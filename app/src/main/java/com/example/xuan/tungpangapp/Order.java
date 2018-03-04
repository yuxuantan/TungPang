package com.example.xuan.tungpangapp;

/**
 * Created by Xuan on 28/2/18.
 */

public class Order {
    private long orderID;
    private long restaurantID;
    private String menuItem;
    private double menuPrice;
    //How much commission earned can be calculated on app, then no need store, if its just a percentage of menuPrice
    private double commissionPrice;
    private long estimatedTimeOfDelivery;
    private long deliveryManUserID;
    private long customerUserID;
    private long beaconIDMajor;
    private long beaconIDMinor;

    //String or lat long?? must convert if Lat long
    private String deliveryLocation;
    // 0-“unassigned”, 1-“deliveryInProcess”, 2-“completed”
    private int status;

    private String restaurantName;


    public Order(long orderID, long restaurantID, String menuItem, double menuPrice, double commissionPrice, long estimatedTimeOfDelivery, long deliveryManUserID, long customerUserID, long beaconIDMajor, long beaconIDMinor, String deliveryLocation, int status, String restaurantName) {
        this.orderID = orderID;
        this.restaurantID = restaurantID;
        this.menuItem = menuItem;
        this.menuPrice = menuPrice;
        this.commissionPrice = commissionPrice;
        this.estimatedTimeOfDelivery = estimatedTimeOfDelivery;
        this.deliveryManUserID = deliveryManUserID;
        this.customerUserID = customerUserID;
        this.beaconIDMajor = beaconIDMajor;
        this.beaconIDMinor = beaconIDMinor;
        this.deliveryLocation = deliveryLocation;
        this.status = status;
        this.restaurantName = restaurantName;
    }

    public long getBeaconIDMinor() {
        return beaconIDMinor;
    }

    public void setBeaconIDMinor(long beaconIDMinor) {
        this.beaconIDMinor = beaconIDMinor;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    public long getOrderID() {
        return orderID;
    }

    public void setOrderID(long orderID) {
        this.orderID = orderID;
    }

    public long getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(long restaurantID) {
        this.restaurantID = restaurantID;
    }

    public String getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(String menuItem) {
        this.menuItem = menuItem;
    }

    public double getMenuPrice() {
        return menuPrice;
    }

    public void setMenuPrice(double menuPrice) {
        this.menuPrice = menuPrice;
    }

    public double getCommissionPrice() {
        return commissionPrice;
    }

    public void setCommissionPrice(double commissionPrice) {
        this.commissionPrice = commissionPrice;
    }

    public long getEstimatedTimeOfDelivery() {
        return estimatedTimeOfDelivery;
    }

    public void setEstimatedTimeOfDelivery(long estimatedTimeOfDelivery) {
        this.estimatedTimeOfDelivery = estimatedTimeOfDelivery;
    }

    public long getDeliveryManUserID() {
        return deliveryManUserID;
    }

    public void setDeliveryManUserID(long deliveryManUserID) {
        this.deliveryManUserID = deliveryManUserID;
    }

    public long getCustomerUserID() {
        return customerUserID;
    }

    public void setCustomerUserID(long customerUserID) {
        this.customerUserID = customerUserID;
    }

    public long getbeaconIDMajor() {
        return beaconIDMajor;
    }

    public void setbeaconIDMajor(long beaconIDMajor) {
        this.beaconIDMajor = beaconIDMajor;
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

    @Override
    public String toString() {
        return "Order{" +
                "orderID=" + orderID +
                ", restaurantID=" + restaurantID +
                ", menuItem='" + menuItem + '\'' +
                ", menuPrice=" + menuPrice +
                ", commissionPrice=" + commissionPrice +
                ", estimatedTimeOfDelivery=" + estimatedTimeOfDelivery +
                ", deliveryManUserID=" + deliveryManUserID +
                ", customerUserID=" + customerUserID +
                ", beaconIDMajor=" + beaconIDMajor +
                ", beaconIDMinor=" + beaconIDMinor +
                ", deliveryLocation='" + deliveryLocation + '\'' +
                ", status=" + status +
                ", restaurantName='" + restaurantName + '\'' +
                '}';
    }
}

