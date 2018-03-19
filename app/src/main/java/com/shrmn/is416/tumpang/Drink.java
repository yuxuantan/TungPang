package com.shrmn.is416.tumpang;

/**
 * Created by man on 19/3/18.
 */

class Drink {
    private String name;
    private double unitPrice;

    public Drink(String name, double unitPrice) {
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
}


