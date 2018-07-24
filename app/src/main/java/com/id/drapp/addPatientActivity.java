package com.id.drapp;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;
import com.id.drapp.doctorContract.patientEntry;
import com.id.drapp.models.patientsInfo;

public class addPatientActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText patientName;
    private EditText patientphno;
    private EditText patientEmail;
    private EditText patientDob;
    private EditText patientAdd;
    private ImageView patientPic;

    public static int GENDER = 0;

    private static final int RC_PHOTO_PICKER = 1;
    private byte[] bmpByte;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private static String user;
    private static String patientUri;
    private static String doctorPushId;

    ConnectivityManager connectivityManager;

    private static final int EDIT_LOADER = 1;

    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);
        setTitle("Add Patient");

        patientName = findViewById(R.id.patientName);
        patientphno = findViewById(R.id.patientphno);
        patientEmail =  findViewById(R.id.patientEmail);
        patientDob= findViewById(R.id.patientDob);
        patientAdd = findViewById(R.id.patientAdd);
        patientPic = findViewById(R.id.patientPic);
        radioGroup = findViewById(R.id.radioGroup);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        patientPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Checking the Network State
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo == null){
                    Toast.makeText(addPatientActivity.this, "Please Connect to Internet to use this feature", Toast.LENGTH_SHORT).show();
                }else {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, RC_PHOTO_PICKER);
                    }
                }
            }
        });

        user = doctorPreference.getUsernameFromSP(this);
        doctorPushId = doctorPreference.getUserPushId(this);

        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("doctor_app").child(doctorPushId).child(charUtility.filterString(user));

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child(doctorPushId).child(charUtility.filterString(user)).child("patientData");

        patientUri = getIntent().getStringExtra("patientUri");

        String fullname = getIntent().getStringExtra("fullname");
        String phoneno = getIntent().getStringExtra("phoneno");
        String dob = getIntent().getStringExtra("dob");
        String email = getIntent().getStringExtra("email");
        String address = getIntent().getStringExtra("address");

        if(fullname != null){
            addPatientFromAppointment(fullname, phoneno, dob, email, address);
        }


        if(patientUri != null){
            getLoaderManager().initLoader(EDIT_LOADER, null, this);
        }
    }

    private void addPatientFromAppointment(String fullname, String phoneno, String dob, String email, String address) {
        patientName.setText(fullname);
        patientphno.setText(String.valueOf(phoneno));
        patientEmail.setText(String.valueOf(email));
        patientDob.setText(dob);
        patientAdd.setText(address);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK && data != null) {

            switch (requestCode){
                case RC_PHOTO_PICKER:
                    Uri selectedImage = data.getData();
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    Bitmap bitmap = Bitmap.createScaledBitmap(imageBitmap, 300, 300, true);
                    Bitmap bmp = ImageHelper.getRoundedCornerBitmap(bitmap, 200);
                    bmpByte = DbBitmapUtility.getBytes(bmp);
                    patientPic.setImageBitmap(bmp);
                    break;
            }

        }else {
            Toast.makeText(addPatientActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();

        }

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
                patientPic.setImageBitmap(bmp);
            }

            patientName.setText(name);
            patientphno.setText(String.valueOf(phone));
            patientEmail.setText(String.valueOf(email));
            patientDob.setText(calendar);
            patientAdd.setText(location);
            patientPic.setBackground(getResources().getDrawable(R.drawable.userplaceholder));
            if(gender == 0){
                radioGroup.check(R.id.maleRadio);
            }
            else {
                radioGroup.check(R.id.femaleRadio);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        patientName.setText("");
        patientphno.setText("");
        patientEmail.setText("");
        patientDob.setText("");
        patientAdd.setText("");
        radioGroup.check(-1);
    }

    public void onSaveClick(View view){
        stopService(new Intent(this, patientIntentService.class));
        final String name = patientName.getText().toString();
        final String phoneNumber = patientphno.getText().toString();
        final String email = patientEmail.getText().toString();
        final String dob = patientDob.getText().toString();
        final String address = patientAdd.getText().toString();

        if(TextUtils.isEmpty(name)){
            patientName.setError("Cannot be Empty");
        }else {
            if(TextUtils.isEmpty(phoneNumber)){
                patientphno.setError("Cannot be Empty");
            }else {
                if(TextUtils.isEmpty(email)){
                    patientEmail.setError("Cannot be Empty");
                }else {
                    if(TextUtils.isEmpty(dob)){
                        patientDob.setError("Cannot be Empty");
                    }else {
                        if(TextUtils.isEmpty(address)){
                            patientAdd.setError("Cannot be Empty");
                        }else {
                            if(patientUri == null){

                                ContentValues cv = new ContentValues();
                                cv.put(patientEntry.COLUMN_NAME, name);
                                cv.put(patientEntry.COLUMN_PHONE_NUMBER, phoneNumber);
                                cv.put(patientEntry.COLUMN_EMAIL, email);
                                cv.put(patientEntry.COLUMN_DOB, dob);
                                cv.put(patientEntry.COLUMN_ADDRESS, address);
                                cv.put(patientEntry.COLUMN_GENDER, GENDER);
                                cv.put(patientEntry.COLUMN_IMAGE, bmpByte);

                                final Uri uri;
                                mDatabaseReference = mDatabaseReference.push();
                                String pushId = mDatabaseReference.getKey();

                                //Checking the Network State
                                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                                if(networkInfo == null){
                                    if(bmpByte == null){
                                        cv.put(patientEntry.COLUMN_PUSH_ID, pushId);
                                        uri = Uri.parse(getContentResolver().insert(patientEntry.contentUri(user), cv) + "/" + pushId);
                                    }else {
                                        Toast.makeText(addPatientActivity.this, "Please Connect to Internet to upload image", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }else {
                                    cv.put(patientEntry.COLUMN_PUSH_ID, pushId);
                                    uri = Uri.parse(getContentResolver().insert(patientEntry.contentUri(user), cv) + "/" + pushId);
                                }

                                if(uri == null){
                                    Toast.makeText(this, "Insertion Failed", Toast.LENGTH_LONG).show();
                                }else{

                                    ////////////////////////////Firebase/////////////////////////////

                                    if(bmpByte == null){
                                        mDatabaseReference.setValue(new patientsInfo(Long.parseLong(uri.getPathSegments().get(1)), name, phoneNumber, email, dob, address, GENDER, null))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                    }
                                                })
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                    }
                                                });

                                        Toast.makeText(this, "New Patient Added Successfully", Toast.LENGTH_LONG).show();
                                        finish();
                                        Intent intent = new Intent(this, detailActivity.class);
                                        intent.putExtra("detailUri", uri.toString());
                                        startActivity(intent);

                                    }else {
                                        final StorageReference photoReference = mStorageReference.child(pushId);

                                        //Upload file to storage
                                        final UploadTask uploadTask = photoReference.putBytes(bmpByte);

                                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                            @Override
                                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                if (!task.isSuccessful()) {
                                                    throw task.getException();
                                                }

                                                return photoReference.getDownloadUrl();
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                if (task.isSuccessful()) {
                                                    Uri downloadUri = task.getResult();
                                                    // Continue with the task to get the download URL
                                                } else {
                                                    Toast.makeText(addPatientActivity.this, "Image Failed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri1) {

                                                mDatabaseReference.setValue(new patientsInfo(Long.parseLong(uri.getPathSegments().get(1)), name, phoneNumber, email, dob, address, GENDER, uri1.toString()))
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                            }
                                                        })
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                            }
                                                        });

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(addPatientActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    /////////////////////////////////////////Firebase///////////////////////////////////


                                    Toast.makeText(this, "New Patient Added Successfully", Toast.LENGTH_LONG).show();
                                    finish();
                                    Intent intent = new Intent(this, detailActivity.class);
                                    intent.putExtra("detailUri", uri.toString());
                                    startActivity(intent);
                                }

                            }else {
                                ContentValues cv = new ContentValues();
                                cv.put(patientEntry.COLUMN_NAME, name);
                                cv.put(patientEntry.COLUMN_PHONE_NUMBER, phoneNumber);
                                cv.put(patientEntry.COLUMN_EMAIL, email);
                                cv.put(patientEntry.COLUMN_DOB, dob);
                                cv.put(patientEntry.COLUMN_ADDRESS, address);
                                cv.put(patientEntry.COLUMN_GENDER, GENDER);
                                cv.put(patientEntry.COLUMN_IMAGE, bmpByte);

                                int updatedRows;

                                //Checking the Network State
                                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                                if(networkInfo == null){
                                    if(bmpByte == null){
                                        updatedRows = getContentResolver().update(Uri.parse(patientUri), cv, null, null);
                                    }else {
                                        Toast.makeText(addPatientActivity.this, "Please Connect to Internet to upload image", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }else {
                                    updatedRows = getContentResolver().update(Uri.parse(patientUri), cv, null, null);
                                }

                                if(updatedRows == 0){
                                    Toast.makeText(this, "Updation Failed", Toast.LENGTH_LONG).show();
                                }else{

                                    if(bmpByte == null){
                                        updateInFirebase(Uri.parse(patientUri).getPathSegments().get(2), new patientsInfo(Long.parseLong(Uri.parse(patientUri).getPathSegments().get(1)), name, phoneNumber, email, dob, address, GENDER, null));

                                        Toast.makeText(this, "Patient Updated Successfull", Toast.LENGTH_LONG).show();
                                        finish();
                                        Intent intent = new Intent(this, detailActivity.class);
                                        intent.putExtra("detailUri", patientUri);
                                        startActivity(intent);
                                    }else {
                                        final StorageReference photoReference = mStorageReference.child(Uri.parse(patientUri).getPathSegments().get(2));

                                        //Upload file to storage
                                        photoReference.putBytes(bmpByte);

                                        final UploadTask uploadTask = photoReference.putBytes(bmpByte);

                                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                            @Override
                                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                if (!task.isSuccessful()) {
                                                    throw task.getException();
                                                }

                                                return photoReference.getDownloadUrl();
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                if (task.isSuccessful()) {
                                                    Uri downloadUri = task.getResult();
                                                    // Continue with the task to get the download URL
                                                } else {
                                                    Toast.makeText(addPatientActivity.this, "Image Failed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri1) {

                                                updateInFirebase(Uri.parse(patientUri).getPathSegments().get(2), new patientsInfo(Long.parseLong(Uri.parse(patientUri).getPathSegments().get(1)), name, phoneNumber, email, dob, address, GENDER, uri1.toString()));

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(addPatientActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }

                                    Toast.makeText(this, "Patient Updated Successfull", Toast.LENGTH_LONG).show();
                                    finish();
                                    Intent intent = new Intent(this, detailActivity.class);
                                    intent.putExtra("detailUri", patientUri);
                                    startActivity(intent);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateInFirebase(final String id, final patientsInfo info){
        Query hekkQuery = mDatabaseReference;

        hekkQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.child(id).getRef().setValue(info);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }



    public void onRadioClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()){
            case R.id.maleRadio:
                if(checked){
                    Toast.makeText(this, "Male", Toast.LENGTH_LONG).show();
                    GENDER = 0;
                }
                break;
            case R.id.femaleRadio:
                if(checked){
                    Toast.makeText(this, "Female", Toast.LENGTH_LONG).show();
                    GENDER = 1;
                }
                break;
        }
    }
}
