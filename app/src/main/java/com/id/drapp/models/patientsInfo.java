package com.id.drapp.models;

public class patientsInfo {
    public String imageResourceId;
    public String name;
    public String address;
    public int age;
    public String result;

    public long id;
    public String phone;
    public String email;
    public String dob;
    public int gender;



    public patientsInfo(String imageResourceId, String name, String address, int age, String result){
        this.imageResourceId = imageResourceId;
        this.name = name;
        this.address = address;
        this.age = age;
        this.result = result;
    }

    public patientsInfo(long id, String name, String phone, String email, String dob, String address, int gender, String imageResourceId){

        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.dob = dob;
        this.address = address;
        this.gender = gender;
        this.imageResourceId = imageResourceId;
    }

    public String getImageResourceId() {
        return imageResourceId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getAge() {
        return age;
    }

    public String getResult() {
        return result;
    }

    public void setImageResourceId(String imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }
}
