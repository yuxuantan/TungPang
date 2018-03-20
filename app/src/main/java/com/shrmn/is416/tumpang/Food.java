package com.shrmn.is416.tumpang;

/**
 * Created by man on 19/3/18.
 */

class Food extends MenuItem {
    private String name;
    private double unitPrice;

    public Food(String path, String name, double unitPrice) {
        super(path);
        this.name = name;
        this.unitPrice = unitPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return "Food{" +
                "name='" + name + '\'' +
                ", unitPrice=" + unitPrice +
                ", path=" + super.getPath() +
                '}';
    }
}
