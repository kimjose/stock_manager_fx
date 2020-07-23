package models.products;

import models.SuperModel;

public class Service implements SuperModel {
    private int id;
    private String name, description;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
