package models.vendors;

import models.products.Warehouse;

import java.util.Date;

public class Invoice {
    /**
     * 'customerId', 'invoiceNo', 'invoiceDate','createdBy',
     *             'warehouseId', 'posted','postedOn', 'reversed', 'reversedOn', 'deleted',**/
    private int id;
    private Vendor vendor;
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


    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
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