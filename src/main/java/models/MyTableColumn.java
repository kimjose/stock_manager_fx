package models;

public class MyTableColumn {
    private String name, property;
    private double multiplier;

    public MyTableColumn(String name, String property, double multiplier) {
        this.name = name;
        this.property = property;
        this.multiplier = multiplier;
    }

    public String getName() {
        return name;
    }

    public String getProperty() {
        return property;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
