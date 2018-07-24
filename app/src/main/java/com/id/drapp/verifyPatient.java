package com.id.drapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;

import java.util.concurrent.TimeUnit;

public class verifyPatient extends AppCompatActivity {

    private EditText countryCodeView;
    private EditText phoneNumberView;
    private Button sendCode;

    private ConnectivityManager connectivityManager;

    private static String fullPhoneNo;

    private SmsVerifyCatcher smsVerifyCatcher;

    private static ProgressDialog progressDialog;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_patient);

        if (doctorPreference.getPhoneNumberFromSP(this) != null){
            Intent intent = new Intent(this, hospitalActivity.class);
            startActivity(intent);
        }

        progressDialog=new ProgressDialog(this,R.style.AppTheme_Dark_Dialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);

        countryCodeView = findViewById(R.id.countryCodeView);
        phoneNumberView = findViewById(R.id.phoneNumberView);
        sendCode = findViewById(R.id.sendCode);

        firebasePhoneVerification();


        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo == null){
                    Toast.makeText(verifyPatient.this, "Make Sure you are connected to Internet", Toast.LENGTH_LONG).show();
                }else {
                    if(TextUtils.isEmpty(countryCodeView.getText().toString().trim())){
                        countryCodeView.setError("Cannot be Empty");
                    }else {
                        if(TextUtils.isEmpty(phoneNumberView.getText().toString().trim())){
                            phoneNumberView.setError("Cannot be Empty");
                        }else{
                            progressDialog.show();
                            fullPhoneNo = countryCodeView.getText().toString().trim().concat(phoneNumberView.getText().toString().trim());
                            String numberToVerify = fullPhoneNo; //Should come from user input

                            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                    fullPhoneNo,        // Phone number to verify
                                    60,                 // Timeout duration
                                    TimeUnit.SECONDS,   // Unit of timeout
                                    verifyPatient.this,               // Activity (for callback binding)
                                    mCallbacks);
                        }
                    }
                }
            }
        });

        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
            }
        });

    }

    private void firebasePhoneVerification() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressDialog.dismiss();
                Toast.makeText(verifyPatient.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                progressDialog.dismiss();
                Toast.makeText(verifyPatient.this, "Code Sent ", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(verifyPatient.this, verifyPatient2.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("verificationId", s);
                intent.putExtra("phone", fullPhoneNo);
                startActivity(intent);
            }
        };
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

    /**
     * need for Android 6 real time permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
