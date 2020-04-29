package models.finance;

public class Bank {
    //'name', 'branch', 'accountNo','enabled','addedBy','requireRefNo',
    private String name, branch, accountNo;
    private boolean enabled, requireRefNo;
    private double balance;
    private int id;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRequireRefNo() {
        return requireRefNo;
    }

    public void setRequireRefNo(boolean requireRefNo) {
        this.requireRefNo = requireRefNo;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return name+"-"+accountNo;
    }
}
