package com.id.drapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class settingsActivity extends AppCompatActivity {

    private ImageView doctorPic;
    private EditText firstNameView;
    private EditText lastNameView;
    private EditText userTitle;
    private EditText institute;
    private EditText instituteAddress;

    private String fullName;
    private String title;
    private String hospitalName;
    private String hospitalAddress;
    private byte[] byte1;
    private String firstName;
    private String lastname;
    private String pushId;
    private String email;
    private String password;
    private String phoneNumber;

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle("Settings");

        doctorPic = findViewById(R.id.doctorPic);
        firstNameView = findViewById(R.id.firstName);
        lastNameView = findViewById(R.id.lastName);
        userTitle = findViewById(R.id.userTitle);
        institute = findViewById(R.id.institute);
        instituteAddress = findViewById(R.id.instituteAddress);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        doctorPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(settingsActivity.this, "Feature is Coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        Cursor cursor = getContentResolver().query(Uri.parse(doctorContract.doctorEntry.CONTENT_URI + "/" +  doctorPreference.getUsernameFromSP(this)), null, null, null, null);
        cursor.moveToFirst();

        fullName = cursor.getString(cursor.getColumnIndex(doctorContract.doctorEntry.COLUMN_NAME));
        firstName = fullName.split("@@@@")[0];
        lastname = fullName.split("@@@@")[1];
        title = cursor.getString(cursor.getColumnIndex(doctorContract.doctorEntry.COLUMN_TITLE));
        hospitalName = cursor.getString(cursor.getColumnIndex(doctorContract.doctorEntry.COLUMN_INSTITUTE));
        hospitalAddress = cursor.getString(cursor.getColumnIndex(doctorContract.doctorEntry.COLUMN_INSTITUTE_ADDRESS));
        pushId = cursor.getString(cursor.getColumnIndex(doctorContract.doctorEntry.COLUMN_PUSHID));
        email = cursor.getString(cursor.getColumnIndex(doctorContract.doctorEntry.COLUMN_EMAIL));
        password = cursor.getString(cursor.getColumnIndex(doctorContract.doctorEntry.COLUMN_PASSWORD));
        phoneNumber = cursor.getString(cursor.getColumnIndex(doctorContract.doctorEntry.COLUMN_PHONE_NUMBER));

        byte1 = cursor.getBlob(cursor.getColumnIndex(doctorContract.doctorEntry.COLUMN_IMAGE));
        if(byte1 != null){
            Bitmap bmp  = DbBitmapUtility.getImage(byte1);
            Bitmap bmp1 = Bitmap.createScaledBitmap(bmp, 200 ,200 ,true);

            doctorPic.setImageBitmap(bmp1);
        }else {
            doctorPic.setBackground(getResources().getDrawable(R.drawable.userplaceholder));
        }
        firstNameView.setText(firstName);
        lastNameView.setText(lastname);
        userTitle.setText(title);
        institute.setText(hospitalName);
        instituteAddress.setText(hospitalAddress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.settingmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.save:
                save();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null){
            Toast.makeText(this, "Please Connect to Internet", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference()
                .child(doctorPreference.getUserPushId(this)).child(charUtility.filterString(doctorPreference.getUsernameFromSP(this)))
                .child("doctorInfo");
        Query hekkQuery = mDatabaseReference;

        hekkQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.child("firstName").getRef().setValue(firstNameView.getText().toString().trim());
                dataSnapshot.child("lastName").getRef().setValue(lastNameView.getText().toString().trim());
                dataSnapshot.child("title").getRef().setValue(userTitle.getText().toString().trim());
                dataSnapshot.child("instituteName").getRef().setValue(institute.getText().toString().trim());
                dataSnapshot.child("instituteAddress").getRef().setValue(instituteAddress.getText().toString().trim())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                final ContentValues cv = new ContentValues();
                                cv.put(doctorContract.doctorEntry.COLUMN_NAME, firstNameView.getText().toString().trim().concat("@@@@").concat(lastNameView.getText().toString().trim()));
                                cv.put(doctorContract.doctorEntry.COLUMN_PHONE_NUMBER, phoneNumber);
                                cv.put(doctorContract.doctorEntry.COLUMN_EMAIL, email);
                                cv.put(doctorContract.doctorEntry.COLUMN_PASSWORD, password);
                                cv.put(doctorContract.doctorEntry.COLUMN_TITLE, userTitle.getText().toString().trim());
                                cv.put(doctorContract.doctorEntry.COLUMN_INSTITUTE, institute.getText().toString().trim());
                                cv.put(doctorContract.doctorEntry.COLUMN_IMAGE, byte1);
                                cv.put(doctorContract.doctorEntry.COLUMN_INSTITUTE_ADDRESS, instituteAddress.getText().toString().trim());

                                int updatedRows = getContentResolver().update(Uri.parse(doctorContract.doctorEntry.CONTENT_URI + "/" + email), cv, null, null);
                                if(updatedRows == 0){
                                    Toast.makeText(settingsActivity.this, "Updation Failed", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(settingsActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(settingsActivity.this, "Updation Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

}
