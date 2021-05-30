package models.products;

import models.SuperModel;

public class Category implements SuperModel {
    private int id;
    private String name;

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getSearchString() {
        return name;
    }
}
