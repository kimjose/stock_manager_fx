package models.auth;

import models.SuperModel;

public class User implements SuperModel {
    /*
    *
    * {
    "id": 1,
    "userName": "admin",
    "firstName": "Jose",
    "lastName": "Kim",
    "nationalId": "34467599",
    "email": "admin@gmail.com",
    "dob": "2020-04-11",
    "gender": "Male",
    "email_verified_at": null,
    "photo": null,
    "isAdmin": 1,
    "created_at": "2020-04-11 20:35:05",
    "updated_at": "2020-04-11 20:35:05",
    "admin": true
}
    * */

    private int id, nationalId;
    private String userName, firstName, lastName, email, dob, gender, photo, phoneNo;
    private boolean admin;

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public int getNationalId() {
        return nationalId;
    }

    public void setNationalId(int nationalId) {
        this.nationalId = nationalId;
    }

    @Override
    public String getSearchString() {
        return userName+" "+email+" "+firstName+" "+lastName+" "+nationalId;
    }
}
