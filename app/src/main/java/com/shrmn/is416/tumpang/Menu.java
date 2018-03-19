package com.shrmn.is416.tumpang;

import java.util.ArrayList;

/**
 * Created by man on 19/3/18.
 */

public class Menu {
    private ArrayList<Drink> drinks;
    private ArrayList<Food> food;

    public Menu(ArrayList<Drink> drinks, ArrayList<Food> food) {
        this.drinks = drinks;
        this.food = food;
    }

    public Menu() {
        this.drinks = new ArrayList<>();
        this.food = new ArrayList<>();
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
                "drinks=" + drinks +
                ", food=" + food +
                '}';
    }
}
