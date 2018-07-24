package com.id.drapp.models;

public class doctorAppointmentsModel {

    public String fullName;
    public String problem;
    public String address;
    public String email;
    public String dob;
    public String phoneno;
    public long appointmentTime;
    public String appointmentPushId;

    public doctorAppointmentsModel(){

    }

    public doctorAppointmentsModel(String fullName, String problem, String address, String email, String dob, String phoneno, long appointmentTime, String appointmentPushId) {
        this.fullName = fullName;
        this.problem = problem;
        this.address = address;
        this.email = email;
        this.dob = dob;
        this.phoneno = phoneno;
        this.appointmentTime = appointmentTime;
        this.appointmentPushId = appointmentPushId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getPhoneno() {
        return phoneno;
    }

    public void setPhoneno(String phoneno) {
        this.phoneno = phoneno;
    }

    public long getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(long appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getAppointmentPushId() {
        return appointmentPushId;
    }

    public void setAppointmentPushId(String appointmentPushId) {
        this.appointmentPushId = appointmentPushId;
    }
}
