package models.products;

import com.google.gson.annotations.SerializedName;
import models.SuperModel;

public class Product {

    /**
     * 'name', 'is_active', 'description', 'sku_code', 'upc_code', 'quantity',
     *         'price', 'brand', 'category', 'uom', 'image'**/

    private int id;
    private String name, description, image;
    @SerializedName("sku_code")
    private String skuCode;
    @SerializedName("upc_code")
    private String upcCode;
    private int quantity;
    private double price;
    private double buyingPrice;
    private Brand brand;
    private Category category;
    private UnitOfMeasure uom;

    /*public int getId() {
        return id;
    }*/

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

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getUpcCode() {
        return upcCode;
    }

    public void setUpcCode(String upcCode) {
        this.upcCode = upcCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public UnitOfMeasure getUom() {
        return uom;
    }

    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getId() {
        return id;
    }
}
