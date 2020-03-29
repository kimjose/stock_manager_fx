package models.customers;

import models.products.Warehouse;

import java.util.Date;

public class Invoice {
    /**
     * 'customerId', 'invoiceNo', 'invoiceDate','createdBy',
     *             'warehouseId', 'posted','postedOn', 'reversed', 'reversedOn', 'deleted',**/
    private int id;
    private Customer customer;
    private String invoiceNo;
    private Date invoiceDate;
    private Warehouse warehouse;
    private boolean posted, reversed;
    private Date postedOn, reversedOn;
    private InvoiceLine[] invoiceLines;
    private double total;

    public int getId() {
        return id;
    }


    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
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

    public InvoiceLine[] getInvoiceLines() {
        return invoiceLines;
    }

    public void setInvoiceLines(InvoiceLine[] invoiceLines) {
        this.invoiceLines = invoiceLines;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
