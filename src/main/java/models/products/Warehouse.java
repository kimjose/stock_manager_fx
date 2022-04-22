package models.products;

import models.SuperModel;

public class Warehouse implements SuperModel {

    //'name', 'isActive', 'location','addedBy'

    private int id;
    private String name;
    private String location;
    private ProductsAvailable[] availables;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ProductsAvailable[] getAvailables() {
        return availables;
    }

    public void setAvailables(ProductsAvailable[] availables) {
        this.availables = availables;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getSearchString() {
        return name;
    }

    public static class ProductsAvailable{
        int id, quantity;

        public ProductsAvailable(int id, int quantity) {
            this.id = id;
            this.quantity = quantity;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
