package models.products;

import models.SuperModel;
import models.finance.Bank;
import utils.Utility;

public class ExpressSale implements SuperModel {
    /*'saleNo', 'description', 'saleDate', 'bankId', 'warehouseId', 'refNo', 'createdBy','deleted',
        'posted', 'postedBy',
        'postedOn', 'reversed', 'reversedOn', 'reversedBy'*/

    private String saleNo, description, saleDate, refNo, postedOn, reversedOn;
    private Bank bank;
    private Warehouse warehouse;
    private int id;
    private ExpressSaleLine[] lines;
    private boolean posted, reversed;

    public int getId() {
        return id;
    }

    public String getSaleNo() {
        return saleNo;
    }

    public void setSaleNo(String saleNo) {
        this.saleNo = saleNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
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

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public ExpressSaleLine[] getLines() {
        return lines;
    }


    public String getTotalString(){
        double total = 0;
        for (ExpressSaleLine line: lines) {
            total += line.getTotal();
        }
        return Utility.formatNumber(total);
    }

    public void setLines(ExpressSaleLine[] lines) {
        this.lines = lines;
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

    @Override
    public String getSearchString() {
        return saleNo + " " +warehouse+" "+bank;
    }
}
