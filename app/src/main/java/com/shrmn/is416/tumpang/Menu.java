package com.shrmn.is416.tumpang;

import java.util.ArrayList;

/**
 * Created by man on 19/3/18.
 */

public class Menu {
    private ArrayList<MenuItem> items;
    private ArrayList<Drink> drinks;
    private ArrayList<Food> food;

    public Menu(ArrayList<Drink> drinks, ArrayList<Food> food) {
        this.drinks = drinks;
        this.food = food;
    }

    public Menu(ArrayList<MenuItem> items) {
        this.items = items;
    }

    public Menu() {
        this.items = new ArrayList<>();
    }

    public ArrayList<MenuItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<MenuItem> items) {
        this.items = items;
    }

    public void addItem(MenuItem item) {
        items.add(item);
    }

    public void removeItem(MenuItem item) {
        items.remove(item);
    }

    public void removeItem(int position) {
        items.remove(position);
    }

    public ArrayList<Drink> getDrinks() {
        return drinks;
    }

    public void setDrinks(ArrayList<Drink> drinks) {
        this.drinks = drinks;
    }

    public ArrayList<Food> getFood() {
        return food;
    }

    public void setFood(ArrayList<Food> food) {
        this.food = food;
    }

    @Override
    public String toString() {
        return "Menu{" +
                "items=" + items +
                '}';
    }
}
