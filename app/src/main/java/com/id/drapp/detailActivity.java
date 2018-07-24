package com.id.drapp;

import android.annotation.SuppressLint;
import android.content.CursorLoader;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.database.Cursor;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.id.drapp.doctorContract.patientEntry;

public class detailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final int DETAIL_LOADER = 1;
    public String patientUri;

    private TextView detailName;
    private TextView detailPhone;
    private TextView detailEmail;
    private TextView detailCalendar;
    private TextView detailLocation;
    private TextView detailGender;
    private Button detailDelete;
    private ImageView patientpic;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private static String user;
    private static String doctorPushId;

    private byte[] bmpByte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setTitle("Patient Detail");

        detailName = findViewById(R.id.detailName);
        detailPhone = findViewById(R.id.detailPhone);
        detailEmail = findViewById(R.id.detailEmail);
        detailCalendar = findViewById(R.id.detailCalendar);
        detailLocation = findViewById(R.id.detailLocation);
        detailGender = findViewById(R.id.detailGender);
        detailDelete = findViewById(R.id.detailDelete);
        patientpic = findViewById(R.id.patientpic);

        detailDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = Uri.parse(patientUri).getPathSegments().get(2);
                int rowAffected = getContentResolver().delete(Uri.parse(patientUri),null, null);
                if(rowAffected == 0){
                    Toast.makeText(detailActivity.this,"Deletion Failed", Toast.LENGTH_LONG).show();
                }
                else{
                    deleteFromFirebase(id);
                    Toast.makeText(detailActivity.this, "Deletion Successful", Toast.LENGTH_LONG).show();
                }
                finish();
            }
        });

        patientUri = getIntent().getStringExtra("detailUri");

        getLoaderManager().initLoader(DETAIL_LOADER, null, this);

        user = doctorPreference.getUsernameFromSP(this);
        doctorPushId = doctorPreference.getUserPushId(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child(doctorPushId).child(charUtility.filterString(user)).child("patientData");
        mDatabaseReference.keepSynced(true);

        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("doctor_app").child(doctorPushId).child(charUtility.filterString(user));

    }

    public void deleteFromFirebase(final String id){
        Query hekkQuery = mDatabaseReference;

        hekkQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.child(id).getRef().removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                deletePhotoFromStorage(id);
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        /////this is for firebase storage
        Task<Void> task = mStorageReference.child(String.valueOf(id).concat("_Image")).delete();
    }

    private void deletePhotoFromStorage(String id) {
        mStorageReference.child(id).delete();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.detailmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.edit:
                edit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void edit() {

        Intent intent = new Intent(this, addPatientActivity.class);
        intent.putExtra("patientUri", patientUri);
        startActivity(intent);

    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {patientEntry._ID, patientEntry.COLUMN_NAME,
                patientEntry.COLUMN_ADDRESS,
                patientEntry.COLUMN_PHONE_NUMBER,
                patientEntry.COLUMN_GENDER,
                patientEntry.COLUMN_EMAIL,
                patientEntry.COLUMN_IMAGE,
                patientEntry.COLUMN_DOB};
        return new CursorLoader(this, Uri.parse(patientUri), projection, null, null, null);
    }

    @SuppressLint("NewApi")
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(data == null || data.getCount() < 1){
            return;
        }

        if(data.moveToFirst()){
            String name = data.getString(data.getColumnIndex(patientEntry.COLUMN_NAME));
            String phone =  data.getString(data.getColumnIndex(patientEntry.COLUMN_PHONE_NUMBER));
            String email = data.getString(data.getColumnIndex(patientEntry.COLUMN_EMAIL));
            String calendar = data.getString(data.getColumnIndex(patientEntry.COLUMN_DOB));
            String location = data.getString(data.getColumnIndex(patientEntry.COLUMN_ADDRESS));
            int gender = data.getInt(data.getColumnIndex(patientEntry.COLUMN_GENDER));
            bmpByte = data.getBlob(data.getColumnIndex(patientEntry.COLUMN_IMAGE));

            if(bmpByte != null){
                Bitmap bmp  = DbBitmapUtility.getImage(bmpByte);
                patientpic.setImageBitmap(bmp);
            }

            detailName.setText(name);
            detailPhone.setText(String.valueOf(phone));
            detailEmail.setText(String.valueOf(email));
            detailCalendar.setText(calendar);
            detailLocation.setText(location);
            patientpic.setBackground(getResources().getDrawable(R.drawable.userplaceholder));
            detailGender.setText(doctorContract.checkGender(gender));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        detailName.setText("");
        detailPhone.setText("");
        detailEmail.setText("");
        detailCalendar.setText("");
        detailLocation.setText("");
        detailGender.setText("");
    }
}
