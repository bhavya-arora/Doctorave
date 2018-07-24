package com.id.drapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class executeBackgroundTask {

    private static final String CANCEL_SYNCING = "cancel_the_syncing";
    private static final String START_SYNCING = "start_the_syncing";

    private static volatile int FOREGROUND_ID = 1338;
    private static final int ACTION_CANCEL_SYNCING = 1290;
    private static final int CONTENT_ACTION_PENDING_INTENT = 1291;

    public static volatile boolean shouldContinue = true;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private Context context;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private ArrayList<String> sqliteIds;

    public executeBackgroundTask(Context context) {
        this.context = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference();

        sqliteIds = new ArrayList<>();
    }

    public void queryTheDatabase() {
        String[] projection = new String[]{doctorContract.patientEntry.COLUMN_PUSH_ID};

        Cursor cursor = context.getContentResolver().query(doctorContract.patientEntry.contentUri(doctorPreference.getUsernameFromSP(context)), projection, null, null, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {

            if (shouldContinue == false) {
                patientIntentService service = new patientIntentService();
                service.stopSelf();
                return;
            }

            sqliteIds.add(cursor.getString(cursor.getColumnIndex(doctorContract.patientEntry.COLUMN_PUSH_ID)));
            cursor.moveToNext();
        }
        queryTheFirebase();

    }

    private void queryTheFirebase() {
        Query hekkQuery = mDatabaseReference.child(doctorPreference.getUserPushId(context))
                .child(charUtility.filterString(doctorPreference.getUsernameFromSP(context)))
                .child("patientData");

        hekkQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (sqliteIds.contains(snapshot.getKey())) {

                    } else {
                        final String name = (String) snapshot.child("name").getValue();
                        final String phone = (String) snapshot.child("phone").getValue();
                        final String imageResourceId = (String) snapshot.child("imageResourceId").getValue();
                        final long gender = (long) snapshot.child("gender").getValue();
                        final String dob = (String) snapshot.child("dob").getValue();
                        final String email = (String) snapshot.child("email").getValue();
                        long age = (long) snapshot.child("age").getValue();
                        final String address = (String) snapshot.child("address").getValue();

                        if (imageResourceId == null) {

                            ContentValues cv = new ContentValues();
                            cv.put(doctorContract.patientEntry.COLUMN_NAME, name);
                            cv.put(doctorContract.patientEntry.COLUMN_PHONE_NUMBER, phone);
                            cv.put(doctorContract.patientEntry.COLUMN_EMAIL, email);
                            cv.put(doctorContract.patientEntry.COLUMN_DOB, dob);
                            cv.put(doctorContract.patientEntry.COLUMN_ADDRESS, address);
                            cv.put(doctorContract.patientEntry.COLUMN_GENDER, Math.toIntExact(gender));
                            cv.put(doctorContract.patientEntry.COLUMN_IMAGE, (byte[]) null);
                            cv.put(doctorContract.patientEntry.COLUMN_PUSH_ID, snapshot.getKey());

                            context.getContentResolver().insert(doctorContract.patientEntry.contentUri(doctorPreference.getUsernameFromSP(context)), cv);
                        } else {


                            mStorageReference = mFirebaseStorage.getReferenceFromUrl(imageResourceId);

                            final long ONE_MEGABYTE = 1024 * 1024;
                            mStorageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new
                              OnSuccessListener<byte[]>() {
                                  @Override
                                  public void onSuccess(byte[] bytes) {
                                      // Data for "images/island.jpg" is returns, use this as needed

                                      ContentValues cv = new ContentValues();
                                      cv.put(doctorContract.patientEntry.COLUMN_NAME, name);
                                      cv.put(doctorContract.patientEntry.COLUMN_PHONE_NUMBER, phone);
                                      cv.put(doctorContract.patientEntry.COLUMN_EMAIL, email);
                                      cv.put(doctorContract.patientEntry.COLUMN_DOB, dob);
                                      cv.put(doctorContract.patientEntry.COLUMN_ADDRESS, address);
                                      cv.put(doctorContract.patientEntry.COLUMN_GENDER, Math.toIntExact(gender));
                                      cv.put(doctorContract.patientEntry.COLUMN_IMAGE, bytes);
                                      cv.put(doctorContract.patientEntry.COLUMN_PUSH_ID, snapshot.getKey());

                                      context.getContentResolver().insert(doctorContract.patientEntry.contentUri(doctorPreference.getUsernameFromSP(context)), cv);
                                  }
                              }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });

                        }
                        patientsFragment.restartAppDialog(context);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
