package com.shrmn.is416.tumpang;

/**
 * Created by man on 19/3/18.
 */

public abstract class MenuItem {
    private String path;

    public MenuItem(String path) {
        this.path = path;
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
}


