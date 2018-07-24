package com.id.drapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.id.drapp.models.doctorAppointmentsModel;
import com.id.drapp.models.patientsAppointmentModel;

public class hospitalDetailActivity extends AppCompatActivity {

    private String pushId;
    private String email;

    private ImageView doctorPicture;
    private TextView doctorName;
    private TextView doctorTitle;
    private TextView hospitalName;
    private TextView hospitalAddress;
    private TextView doctorPhone;
    private Button getAppointment;

    private String firstName;
    private String lastName;
    private String hospitalname;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private ProgressDialog progressDialog;
    private AlertDialog.Builder alert;
    private Dialog dialog;

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_detail);

        String[] tags =  getIntent().getStringArrayExtra("tag");

        pushId = tags[0];
        email = tags[1];

        progressDialog=new ProgressDialog(this,R.style.AppTheme_Dark_Dialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        doctorPicture = findViewById(R.id.doctorPicture);
        doctorName = findViewById(R.id.doctorName);
        doctorTitle = findViewById(R.id.doctorTitle);
        hospitalName = findViewById(R.id.hospitalName);
        hospitalAddress = findViewById(R.id.hospitalAddress);
        doctorPhone = findViewById(R.id.doctorPhone);
        getAppointment = findViewById(R.id.getAppointment);

        createDialog();

        getAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo == null){
                    Toast.makeText(hospitalDetailActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                    return;
                }
                getTheAppointment();
            }
        });


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        ///Checking Internet
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        Query hekkQuery = mDatabaseReference;
        hekkQuery.addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firstName = (String) dataSnapshot.child(pushId).child(charUtility.filterString(email)).child("doctorInfo").child("firstName").getValue();
                lastName = (String) dataSnapshot.child(pushId).child(charUtility.filterString(email)).child("doctorInfo").child("lastName").getValue();
                String phone = (String) dataSnapshot.child(pushId).child(charUtility.filterString(email)).child("doctorInfo").child("phone").getValue();
                String emailId = (String) dataSnapshot.child(pushId).child(charUtility.filterString(email)).child("doctorInfo").child("email").getValue();
                String password = (String) dataSnapshot.child(pushId).child(charUtility.filterString(email)).child("doctorInfo").child("password").getValue();
                String title = (String) dataSnapshot.child(pushId).child(charUtility.filterString(email)).child("doctorInfo").child("title").getValue();
                hospitalname = (String) dataSnapshot.child(pushId).child(charUtility.filterString(email)).child("doctorInfo").child("instituteName").getValue();
                String hospitaladdress = (String) dataSnapshot.child(pushId).child(charUtility.filterString(email)).child("doctorInfo").child("instituteAddress").getValue();
                boolean isVerified = (boolean) dataSnapshot.child(pushId).child(charUtility.filterString(email)).child("doctorInfo").child("isVerified").getValue();
                String imageLink = (String) dataSnapshot.child(pushId).child(charUtility.filterString(email)).child("doctorInfo").child("imageLink").getValue();

                updateUi(firstName, lastName, phone, emailId, password, title, hospitalname, hospitaladdress, isVerified, imageLink);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(hospitalDetailActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.customdialog);

        Button cancelAppointment = dialog.findViewById(R.id.cancelAppointment);
        Button bookAppointment = dialog.findViewById(R.id.bookAppointment);
        final EditText patientFullName = dialog.findViewById(R.id.patientFullName);
        final EditText patientAddress = dialog.findViewById(R.id.patientAddress);
        final EditText patientProblem = dialog.findViewById(R.id.patientProblem);
        final EditText patientEmail = dialog.findViewById(R.id.patientEmail);
        final EditText patientDob = dialog.findViewById(R.id.patientDob);

        cancelAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        bookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo == null){
                    Toast.makeText(hospitalDetailActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                    return;
                }


                final String appointmentPushId;
                if(TextUtils.isEmpty(patientFullName.getText().toString().trim())){
                    patientFullName.setError("Cannot be Empty");
                }else {
                    if(TextUtils.isEmpty(patientProblem.getText().toString().trim())){
                        patientProblem.setError("Cannot be Empty");
                    }else {
                        if(TextUtils.isEmpty(patientAddress.getText().toString().trim())){
                            patientAddress.setError("Cannot be Empty");
                        }else {
                            if(TextUtils.isEmpty(patientEmail.getText().toString().trim())){
                                patientEmail.setError("Cannot be Empty");
                            }else {
                                if(TextUtils.isEmpty(patientDob.getText().toString().trim())){
                                    patientDob.setError("Cannot be Empty");
                                }else {
                                    progressDialog.show();
                                    appointmentPushId = mDatabaseReference.push().getKey();
                                    mDatabaseReference.child(doctorPreference.getPhoneNumberFromSP(hospitalDetailActivity.this))
                                            .child("appointments").child(appointmentPushId)
                                            .setValue(new patientsAppointmentModel(firstName + " " + lastName, hospitalname, patientProblem.getText().toString().trim(), System.currentTimeMillis(), appointmentPushId, pushId))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mDatabaseReference = mFirebaseDatabase.getReference();
                                                    mDatabaseReference = mDatabaseReference.child(pushId).child(charUtility.filterString(email))
                                                            .child("appointments").child(appointmentPushId);
                                                    mDatabaseReference.setValue(new doctorAppointmentsModel(patientFullName.getText().toString().trim(),
                                                            patientProblem.getText().toString().trim(),
                                                            patientAddress.getText().toString().trim(),
                                                            patientEmail.getText().toString().trim(),
                                                            patientDob.getText().toString().trim(),
                                                            doctorPreference.getPhoneNumberFromSP(hospitalDetailActivity.this),
                                                            System.currentTimeMillis(), appointmentPushId))
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(hospitalDetailActivity.this, "Booked your Appointment", Toast.LENGTH_LONG).show();
                                                                    finish();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(hospitalDetailActivity.this, "Something went Wrong", Toast.LENGTH_LONG).show();
                                                                }
                                                            });
                                                }
                                            });
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void getTheAppointment() {
        dialog.show();
    }

    private void updateUi(String firstName, String lastName, String phone, String emailId, String password, String title, String hospitalname, String hospitaladdress, boolean isVerified, String imageLink) {
        progressDialog.dismiss();
        doctorName.setText(firstName.concat(" ").concat(lastName));
        doctorTitle.setText(title);
        hospitalName.setText(hospitalname.substring(0,1).toUpperCase() + hospitalname.substring(1));
        hospitalAddress.setText(hospitaladdress);
        doctorPhone.setText(phone);

        if(imageLink == null){

        }else {
            Glide.with(doctorPicture.getContext())
                    .load(imageLink)
                    .into(doctorPicture);
        }
    }
}