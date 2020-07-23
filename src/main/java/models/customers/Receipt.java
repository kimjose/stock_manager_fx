package models.customers;

import models.SuperModel;
import models.finance.Bank;
import utils.Utility;

public class Receipt implements SuperModel {

    /**
     * 'customerId', 'description', 'receiptDate','posted','postedBy',
     *         'postedOn','reversed','reversedOn','reversedBy','amount','bankId','deleted','createdBy','no', 'extDocNo',
     * */
    private Customer customer;
    private String description, receiptDate, postedOn, reversedOn, extDocNo, no, amountString;
    private boolean posted, reversed;
    private Bank bank;
    private double amount;
    private int id;

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
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

    public String getExtDocNo() {
        return extDocNo;
    }

    public void setExtDocNo(String extDocNo) {
        this.extDocNo = extDocNo;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
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

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public double getAmount() {
        return amount;
    }

    public String getAmountString() {
        return Utility.formatNumber(amount);
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String getSearchString() {
        return customer.toString() + no;
    }
}
