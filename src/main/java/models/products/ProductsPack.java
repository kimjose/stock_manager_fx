package models.products;

import models.SuperModel;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class ProductsPack implements SuperModel {
    /**
     * This class helps when a product package is opened for retail selling
     *
     * @author kim jose
     */
    //'name', 'is_active', 'description', 'product_from', 'product_to', 'quantity'
    private int id;
    private String name;
    @Nullable
    private String description;
    private Product productFrom;
    private Product productTo;
    private int quantityFrom;
    private int quantityTo;
    private Warehouse warehouse;
    private boolean posted, reversed;
    private Date postedOn, reversedOn;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public Product getProductFrom() {
        return productFrom;
    }

    public void setProductFrom(Product productFrom) {
        this.productFrom = productFrom;
    }

    public Product getProductTo() {
        return productTo;
    }

    public void setProductTo(Product productTo) {
        this.productTo = productTo;
    }

    public int getQuantityFrom() {
        return quantityFrom;
    }

    public void setQuantityFrom(int quantityFrom) {
        this.quantityFrom = quantityFrom;
    }

    public int getQuantityTo() {
        return quantityTo;
    }

    public void setQuantityTo(int quantityTo) {
        this.quantityTo = quantityTo;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public Date getPostedOn() {
        return postedOn;
    }

    public void setPostedOn(Date postedOn) {
        this.postedOn = postedOn;
    }

    public Date getReversedOn() {
        return reversedOn;
    }

    public void setReversedOn(Date reversedOn) {
        this.reversedOn = reversedOn;
    }

    @Override
    public String getSearchString() {
        return name;
    }
}
