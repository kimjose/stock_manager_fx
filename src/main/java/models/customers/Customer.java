package models.customers;

public class Customer {
    //'name', 'is_active','email','phone','addedBy','isActive'

    private int id;

    public int getId() {
        return id;
    }

    private String name, email, phone;

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

    @Override
    public String toString() {
        return name;
    }
}
