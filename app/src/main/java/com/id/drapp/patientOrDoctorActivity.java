package com.id.drapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class patientOrDoctorActivity extends AppCompatActivity {

    private Button areYouDoctor;
    private Button areYouPatient;
    private TextView privacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_or_doctor);

        if(doctorPreference.getBooleanFromSP(this)){
            if(doctorPreference.getUsernameFromSP(this) != null){
                Intent intent = new Intent(this, patientsListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else if (doctorPreference.getPhoneNumberFromSP(this) != null){
                Intent intent = new Intent(this, hospitalActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

        }

        areYouDoctor = findViewById(R.id.areYouDoctor);
        areYouPatient = findViewById(R.id.areYouPatient);
        privacyPolicy = findViewById(R.id.privacyPolicy);

        privacyPolicy.setClickable(true);
        privacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        String text = "<a href='http://bhavya-arora.me/doctorave-privacy-policy'> Privacy Policy </a>";
        privacyPolicy.setText(Html.fromHtml(text));

        areYouDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(patientOrDoctorActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        areYouPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(patientOrDoctorActivity.this, verifyPatient.class);
                startActivity(intent);
            }
        });
    }
}
