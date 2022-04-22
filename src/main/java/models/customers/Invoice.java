package models.customers;

import models.SuperModel;
import models.products.Warehouse;

import java.sql.Timestamp;
import java.util.Date;

public class Invoice implements SuperModel {
    /**
     * 'customerId', 'invoiceNo', 'invoiceDate','createdBy',
     *             'warehouseId', 'posted','postedOn', 'reversed', 'reversedOn', 'deleted',**/
    private int id;
    private Customer customer;
    private String invoiceNo;
    private String invoiceDate;
    private Warehouse warehouse;
    private boolean posted, reversed;
    private String postedOn, reversedOn;
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

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
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

    @Override
    public String getSearchString() {
        return invoiceNo + customer.toString();
    }
}
