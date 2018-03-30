package com.shrmn.is416.tumpang;

import java.io.Serializable;

/**
 * Created by man on 19/3/18.
 */

class Drink extends MenuItem implements Serializable {
    private String name;
    private double unitPrice;
    private int quantity;

    public Drink(String path, String name, double unitPrice) {
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
    public int getQuantity() {
        return this.quantity;
    }

    @Override
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Drink{" +
                "name='" + name + '\'' +
                ", unitPrice=" + unitPrice +
                ", path=" + super.getPath() +
                '}';
    }
}


