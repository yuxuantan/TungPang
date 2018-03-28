package com.shrmn.is416.tumpang;

import java.io.Serializable;

/**
 * Created by man on 19/3/18.
 */

public abstract class MenuItem implements Serializable{
    private String name;
    private double unitPrice;
    private int quantity;
    private String path;

    public MenuItem(String path) {
        this.path = path;
    }

    public MenuItem(String name, double unitPrice, int quantity){
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public abstract String getName();
    public abstract void setName(String name);
    public abstract double getUnitPrice();
    public abstract void setUnitPrice(double price);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MenuItem menuItem = (MenuItem) o;

        return path.equals(menuItem.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "path='" + path + '\'' +
                '}';
    }

    public int getQuantity(){
        return this.quantity;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }
}


