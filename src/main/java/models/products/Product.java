package models.products;

import com.google.gson.annotations.SerializedName;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import interfaces.HomeDataInterface;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.paint.Paint;
import models.SuperModel;

public class Product implements SuperModel{

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
    private double buyingPrice;
    private double sellingPrice;
    private Brand brand;
    private Category category;
    private UnitOfMeasure uom;
    private HomeDataInterface dataInterface;

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

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Button getSellButton(){
        Button sellBtn = new Button("sell");
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.USD);
        icon.setFill(Paint.valueOf("#19D019"));
        icon.setSize("12.0");
        sellBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: 19D019;");
        sellBtn.setGraphic(icon);
        sellBtn.setOnAction(event -> {
            if (dataInterface != null) dataInterface.sellProduct(this);
        });
        return sellBtn;
    }

    public void setDataInterface(HomeDataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    @Override
    public String getSearchString() {
        return name;
    }
}
