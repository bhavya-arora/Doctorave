package com.id.drapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.id.drapp.doctorContract.doctorEntry;

public class loginActivity extends AppCompatActivity {

    private Button login;
    private EditText username;
    private EditText password;
    private static String user;

    private ProgressDialog progressDialog;

    private FirebaseAuth.AuthStateListener mAuthStateChangeListener;

    private FirebaseAuth firebaseAuth;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");

        login = findViewById(R.id.login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        user = doctorPreference.getUsernameFromSP(this);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        progressDialog=new ProgressDialog(this,R.style.AppTheme_Dark_Dialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);


        if(doctorPreference.getBooleanFromSP(this)){
            Intent intent = new Intent(this, patientsListActivity.class);
            startActivity(intent);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCredentials();
            }
        });
    }

    public void checkCredentials(){
        final String doctorUsername = username.getText().toString();
        String doctorPassword = password.getText().toString();

        if(TextUtils.isEmpty(doctorUsername)){
            username.setError("Cannot be Empty");
        }else {
            if(TextUtils.isEmpty(doctorPassword)){
                password.setError("Cannot be Empty");
            }else {
                Cursor cursor = getContentResolver().query(Uri.parse(doctorContract.doctorEntry.CONTENT_URI + "/" + doctorUsername + "/" + doctorPassword),
                        null, null, null, null);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                progressDialog.show();
                if(networkInfo == null){
                    progressDialog.dismiss();
                    Toast.makeText(this, "No Internet", Toast.LENGTH_LONG).show();
                }else {
                    if(cursor.getCount() == 0){
                        firebaseAuth.signInWithEmailAndPassword(doctorUsername, doctorPassword)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {

                                        createDoctorTable(doctorUsername);

                                        patientDbHelper.createPatientDb(loginActivity.this, doctorUsername);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(loginActivity.this, "Login UnSuccessfull", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });
                    }else {
                        cursor.moveToFirst();
                        String userNa = cursor.getString(cursor.getColumnIndex(doctorEntry.COLUMN_EMAIL));
                        final String pushId = cursor.getString(cursor.getColumnIndex(doctorEntry.COLUMN_PUSHID));
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(doctorUsername, doctorPassword)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        loginSuccessfull(doctorUsername, pushId);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                    }
                                });
                    }
                }
            }
        }
    }

    private void createDoctorTable(final String doctorUsername) {

        Query hekkQuery = mDatabaseReference.orderByChild(charUtility.filterString(doctorUsername));


        hekkQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {

                    for(DataSnapshot appleSnapshot1: appleSnapshot.getChildren()){

                        if(appleSnapshot1.getKey().equals(charUtility.filterString(doctorUsername))){
                            String firstname = (String) appleSnapshot.child(charUtility.filterString(doctorUsername)).child("doctorInfo").child("firstName").getValue();
                            String lastname = (String) appleSnapshot.child(charUtility.filterString(doctorUsername)).child("doctorInfo").child("lastName").getValue();
                            String userphone = (String) appleSnapshot.child(charUtility.filterString(doctorUsername)).child("doctorInfo").child("phone").getValue();
                            String useremail = (String) appleSnapshot.child(charUtility.filterString(doctorUsername)).child("doctorInfo").child("email").getValue();
                            String userpassword = (String) appleSnapshot.child(charUtility.filterString(doctorUsername)).child("doctorInfo").child("password").getValue();
                            String title = (String) appleSnapshot.child(charUtility.filterString(doctorUsername)).child("doctorInfo").child("title").getValue();
                            String doctorInstitute = (String) appleSnapshot.child(charUtility.filterString(doctorUsername)).child("doctorInfo").child("instituteName").getValue();
                            String doctorInstituteAddress = (String) appleSnapshot.child(charUtility.filterString(doctorUsername)).child("doctorInfo").child("instituteAddress").getValue();

                            ContentValues cv = new ContentValues();
                            cv.put(doctorEntry.COLUMN_PUSHID, appleSnapshot.getKey());
                            cv.put(doctorEntry.COLUMN_NAME, firstname.concat("@@@@").concat(lastname));
                            cv.put(doctorEntry.COLUMN_PHONE_NUMBER, userphone);
                            cv.put(doctorEntry.COLUMN_EMAIL, useremail);
                            cv.put(doctorEntry.COLUMN_PASSWORD, userpassword);
                            cv.put(doctorEntry.COLUMN_TITLE, title);
                            cv.put(doctorEntry.COLUMN_INSTITUTE, doctorInstitute);
                            cv.put(doctorEntry.COLUMN_IMAGE, (byte[]) null);
                            cv.put(doctorEntry.COLUMN_INSTITUTE_ADDRESS, doctorInstituteAddress);

                            Uri uri = getContentResolver().insert(doctorEntry.CONTENT_URI, cv);
                            if(uri == null){
                                progressDialog.dismiss();
                                return;
                            }else {
                                loginSuccessfull(useremail, appleSnapshot.getKey());
                            }
                        }

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

    }

    private void loginSuccessfull(String doctorUsername, String pushId) {

        if(firebaseAuth.getCurrentUser().isEmailVerified()){
            final Boolean[] i = new Boolean[1];

            Query hekkQuery = mDatabaseReference.child(pushId).child(charUtility.filterString(doctorUsername));

            hekkQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    i[0] = (Boolean) dataSnapshot.child("doctorInfo").child("isVerified").getValue();

                    if(i[0]){

                    }else {
                        progressDialog.dismiss();
                        dataSnapshot.child("doctorInfo").child("isVerified").getRef().setValue(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    progressDialog.dismiss();
                }
            });

            doctorPreference.saveIsTapTargetShown(this, false);
            doctorPreference.saveUserPushId(this, null);
            doctorPreference.saveUsernameInSP(this, null);
            doctorPreference.saveBooleanInSP(this, false);

            doctorPreference.saveUsernameInSP(this, doctorUsername);
            doctorPreference.saveUserPushId(this, pushId);
            doctorPreference.saveBooleanInSP(this, true);

            progressDialog.dismiss();

            Toast.makeText(this, "Login Successfull", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, patientsListActivity.class);
            finish();
            startActivity(intent);

        }else {
            progressDialog.dismiss();
            Toast.makeText(this, "Please Verify Your Account", Toast.LENGTH_LONG).show();
            finishTheActivity();
        }
    }

    public void finishTheActivity(){
        doctorPreference.saveIsTapTargetShown(this, false);
        doctorPreference.saveBooleanInSP(this, false);
        doctorPreference.saveUsernameInSP(this, null);
        doctorPreference.saveUserPushId(this, null);
        firebaseAuth.signOut();
        finish();
    }
}
