package com.example.mygrocery.models;

public class ModelCartItem {
    String id, pid, name, price, quantity, cost;


    public ModelCartItem(String id, String pid, String name, String price, String quantity, String cost) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.cost = cost;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }
}
