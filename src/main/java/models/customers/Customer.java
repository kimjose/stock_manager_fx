package models.customers;

import models.SuperModel;
import utils.Utility;

public class Customer implements SuperModel {
    //'name', 'is_active','email','phone','addedBy','isActive'

    private int id;
    private String name, email, phone;
    private double balance;

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getBalanceString() {
        return Utility.formatNumber(balance);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getSearchString() {
        return name + email;
    }
}
