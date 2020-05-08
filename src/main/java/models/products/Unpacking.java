package models.products;

public class Unpacking {
    /**'groupId', 'quantity', 'productQuantity', 'warehouseId', 'createdBy', 'posted', 'postedOn', 'postedBy',
     'reversed', 'reversedOn', 'reversedBy'*/
    private ProductGroup group;
    private int quantity, productQuantity;
    private Warehouse warehouse;
    private boolean posted, reversed;
    private String postedOn, reversedOn;
    private int id;

    public int getId() {
        return id;
    }

    public ProductGroup getGroup() {
        return group;
    }

    public void setGroup(ProductGroup group) {
        this.group = group;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
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

    public String getPostedOn() {
        return postedOn;
    }

    public void setPostedOn(String postedOn) {
        this.postedOn = postedOn;
    }

    public String getReversedOn() {
        return reversedOn;
    }

    public void setReversedOn(String reversedOn) {
        this.reversedOn = reversedOn;
    }
}
