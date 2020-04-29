package models.vendors;

import models.finance.Bank;
import utils.Utility;


public class PaymentVoucher {

    private Vendor vendor;
    private String description;
    private String voucherDate;
    private String voucherNo;
    private String extDocNo, amountString;
    private boolean posted, reversed;
    private String postedOn, reversedOn;
    private double amount;
    private Bank bank;
    private int id;

    public int getId() {
        return id;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(String voucherDate) {
        this.voucherDate = voucherDate;
    }

    public String getVoucherNo() {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public String getExtDocNo() {
        return extDocNo;
    }

    public void setExtDocNo(String extDocNo) {
        this.extDocNo = extDocNo;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Bank getBank() {
        return bank;
    }

    public String getAmountString() {
        return Utility.formatNumber(amount);
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }
}
