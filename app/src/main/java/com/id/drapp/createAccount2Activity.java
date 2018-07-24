package com.id.drapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.id.drapp.doctorContract.doctorEntry;
import com.id.drapp.models.doctorInfo;

import java.io.IOException;

public class createAccount2Activity extends AppCompatActivity {

    private Button createAccount2Login;
    private TextView doctorName;
    private EditText userTitle;
    private EditText institute;
    private EditText instituteAddress;
    private ImageView userPic;
    private FrameLayout frame;

    private String firstname;
    private String lastname;
    private String userphone;
    private String useremail;
    private String password;

    private byte[] bmpByte;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private ProgressDialog progressDialog;

    private ConnectivityManager connectivityManager;

    private static final int RC_PHOTO_PICKER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account2);
        setTitle("");
        getSupportActionBar().setElevation(0);

        if(doctorPreference.getBooleanFromSP(this)){
            Intent intent = new Intent(this, patientsListActivity.class);
            startActivity(intent);
        }

        progressDialog=new ProgressDialog(this,R.style.AppTheme_Dark_Dialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        createAccount2Login = findViewById(R.id.createAcoount2Login);
        doctorName = findViewById(R.id.doctorName);
        userTitle = findViewById(R.id.userTitle);
        institute = findViewById(R.id.institute);
        instituteAddress = findViewById(R.id.instituteAddress);
        userPic = findViewById(R.id.userPic);
        frame = findViewById(R.id.frame);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mStorageReference = mFirebaseStorage.getReference();

        Intent intent = getIntent();
        firstname = intent.getStringExtra("firstname");
        lastname = intent.getStringExtra("lastname");
        userphone = intent.getStringExtra("userphone");
        useremail = intent.getStringExtra("useremail");
        password = intent.getStringExtra("password");

        doctorName.setText(firstname + " " + lastname);

        createAccount2Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });

        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RC_PHOTO_PICKER);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK && data != null) {

            switch (requestCode){
                case RC_PHOTO_PICKER:
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage), 300, 300, true);
                        Bitmap bmp = ImageHelper.getRoundedCornerBitmap(bitmap, 200);
                        bmpByte = DbBitmapUtility.getBytes(bmp);
                        userPic.setImageBitmap(bmp);
                    } catch (IOException e) {
                    }
                    break;
            }

        }else {
            Toast.makeText(createAccount2Activity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();

        }

    }


    public void createUser(){
        final String title = userTitle.getText().toString().trim();
        final String doctorInstitute = institute.getText().toString().toLowerCase() .trim();
        final String doctorInstituteAddress = instituteAddress.getText().toString().trim();

        if(TextUtils.isEmpty(title)){
            userTitle.setError("Cannot be Empty");
        }else{
            if(TextUtils.isEmpty(doctorInstitute)){
                institute.setError("Cannot be Empty");
            }else {
                if (TextUtils.isEmpty(doctorInstituteAddress)){
                    instituteAddress.setError("Cannot be Empty");
                }else {
                    firstname = "Dr. " + firstname;
                    progressDialog.show();
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if(networkInfo == null){
                        Toast.makeText(this, "No Internet", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        return;
                    }else {
                        final ContentValues cv = new ContentValues();
                        cv.put(doctorEntry.COLUMN_NAME, firstname.concat("@@@@").concat(lastname));
                        cv.put(doctorEntry.COLUMN_PHONE_NUMBER, userphone);
                        cv.put(doctorEntry.COLUMN_EMAIL, useremail);
                        cv.put(doctorEntry.COLUMN_PASSWORD, password);
                        cv.put(doctorEntry.COLUMN_TITLE, title);
                        cv.put(doctorEntry.COLUMN_INSTITUTE, doctorInstitute);
                        cv.put(doctorEntry.COLUMN_IMAGE, bmpByte);
                        cv.put(doctorEntry.COLUMN_INSTITUTE_ADDRESS, doctorInstituteAddress);

                        final Uri[] uri = new Uri[1];

                        firebaseAuth.createUserWithEmailAndPassword(useremail, password)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {

                                        mDatabaseReference = mFirebaseDatabase.getReference().push();
                                        final String pushId = mDatabaseReference.getKey();

                                        cv.put(doctorEntry.COLUMN_PUSHID, pushId);

                                        Toast.makeText(createAccount2Activity.this, "Firebase Sucess", Toast.LENGTH_SHORT).show();
                                        uri[0] = getContentResolver().insert(doctorEntry.CONTENT_URI, cv);
                                        createUserInLocalDb(uri[0]);

                                        if(bmpByte == null){
                                            mDatabaseReference = mDatabaseReference.child(charUtility.filterString(useremail)).child("doctorInfo");
                                            mDatabaseReference.setValue(new doctorInfo(firstname, lastname, userphone, useremail, password, title, doctorInstitute, doctorInstituteAddress, false, pushId, null));
                                            progressDialog.dismiss();
                                        }else {
                                            mStorageReference = mFirebaseStorage.getReference().child(pushId);
                                            mStorageReference = mStorageReference.child(charUtility.filterString(useremail)).child("doctorInfo");
                                            UploadTask uploadTask = mStorageReference.putBytes(bmpByte);

                                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                @Override
                                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                    if (!task.isSuccessful()) {
                                                        throw task.getException();
                                                    }

                                                    // Continue with the task to get the download URL
                                                    return mStorageReference.getDownloadUrl();

                                                }
                                            })
                                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                            if (task.isSuccessful()) {
                                                                Uri downloadUri = task.getResult();

                                                                mDatabaseReference = mDatabaseReference.child(charUtility.filterString(useremail)).child("doctorInfo");
                                                                mDatabaseReference.setValue(new doctorInfo(firstname, lastname, userphone, useremail, password, title, doctorInstitute, doctorInstituteAddress, false, pushId, downloadUri.toString()));
                                                            } else {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(createAccount2Activity.this, "Error Uploading Image", Toast.LENGTH_LONG).show();
                                                            }

                                                        }
                                                    });
                                        }

                                        createUserInFirebase(useremail);
                                        uploadUserPicIfHas(pushId, useremail);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String message = e.getMessage();
                                        progressDialog.dismiss();
                                        Toast.makeText(createAccount2Activity.this, message, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                });
                    }
                }
            }
        }
    }

    private void uploadUserPicIfHas(String pushId, String useremail) {
    }

    private void createUserInFirebase(String useremail) {
        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
        Toast.makeText(this, "Email Verification Link has been Sent to: " + useremail, Toast.LENGTH_LONG).show();
    }

    private void createUserInLocalDb(Uri uri) {
        if(uri == null){
            Toast.makeText(this, "Username Already Exist", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "User Created Successfully" ,Toast.LENGTH_SHORT).show();

            patientDbHelper.createPatientDb(this, useremail);
            finish();

            Intent intent = new Intent(this, loginActivity.class);
            startActivity(intent);
        }
    }
}
