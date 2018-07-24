package com.id.drapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;

public class verifyPatient2 extends AppCompatActivity {

    private SmsVerifyCatcher smsVerifyCatcher;
    private String code;

    private EditText codeReceived;
    private Button verifyCode;

    private static FirebaseAuth firebaseAuth;

    private  String  phone;
    private  String verificationId;
    private  ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_patient2);

        if (doctorPreference.getPhoneNumberFromSP(this) != null){
            Intent intent = new Intent(this, hospitalActivity.class);
            startActivity(intent);
        }

        codeReceived = findViewById(R.id.codeReceived);
        verifyCode = findViewById(R.id.verifyCode);

        progressDialog=new ProgressDialog(this,R.style.AppTheme_Dark_Dialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);

        phone = getIntent().getStringExtra("phone");
        verificationId = getIntent().getStringExtra("verificationId");


        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                code = message.substring(0, 6);
                /*code = message.split(":")[1].trim().substring(1);*/
                codeReceived.setText(code);
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, codeReceived.getText().toString().trim());
                signInWithPhoneAuthCredential(credential);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();


        verifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, codeReceived.getText().toString().trim());
                signInWithPhoneAuthCredential(credential);
            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = task.getResult().getUser();
                            // ...

                            putInformationInFb(verifyPatient2.this);

                            Intent intent1 = new Intent(verifyPatient2.this, hospitalActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent1.putExtra("phone", phone);
                            startActivity(intent1);
                        } else {
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        smsVerifyCatcher.onStop();
    }



    private  void putInformationInFb(final Context context) {

        // TODO: Send messages on click
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference();
        Query hekkQuery = mDatabaseReference;

        hekkQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean alreadyHas = false;
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(phone)){
                        alreadyHas = true;
                        saveUserInSP(context);
                    }
                }
                if(!alreadyHas){
                    mDatabaseReference.child(phone).setValue("").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            saveUserInSP(context);
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void saveUserInSP(Context context) {

        doctorPreference.saveIsTapTargetShown(context, false);
        doctorPreference.saveBooleanInSP(context, false);
        doctorPreference.savePhoneNumberInSP(context, null);
        doctorPreference.saveBooleanInSP(context, true);
        doctorPreference.savePhoneNumberInSP(context, phone);
    }

    /**
     * need for Android 6 real time permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
