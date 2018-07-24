package com.id.drapp.models;

public class patientsAppointmentModel {

    public String doctorname;
    public String hospitalName;
    public String problem;
    public long time;
    public String appointmentPushId;
    public String doctorPushId;

    public patientsAppointmentModel(){

    }

    public patientsAppointmentModel(String doctorname, String hospitalName, String problem, long time, String appointmentPushId, String doctorPushId) {
        this.doctorname = doctorname;
        this.hospitalName = hospitalName;
        this.problem = problem;
        this.time = time;
        this.appointmentPushId = appointmentPushId;
        this.doctorPushId = doctorPushId;
    }

    public String getDoctorname() {
        return doctorname;
    }

    public void setDoctorname(String doctorname) {
        this.doctorname = doctorname;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAppointmentPushId() {
        return appointmentPushId;
    }

    public void setAppointmentPushId(String appointmentPushId) {
        this.appointmentPushId = appointmentPushId;
    }

    public String getDoctorPushId() {
        return doctorPushId;
    }

    public void setDoctorPushId(String doctorPushId) {
        this.doctorPushId = doctorPushId;
    }
}
