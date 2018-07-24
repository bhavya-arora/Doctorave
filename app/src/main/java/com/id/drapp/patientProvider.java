package com.id.drapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.id.drapp.doctorContract.patientEntry;
import com.id.drapp.doctorContract.doctorEntry;

import java.util.List;

public class patientProvider extends ContentProvider{

    public static final int PATIENTS = 100;
    public static final int PATIENT_ID = 101;
    public static final int PATIENT_NAME = 102;
    private static final int DOCTOR = 200;
    private static final int DOCTOR_ID = 201;
    private static final int SPECIFIC_DOCTOR = 301;

    public static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private patientDbHelper mPatientHelper;

    static{
        sUriMatcher.addURI(doctorContract.CONTENT_AUTHORITY, doctorContract.PATH_DOCTORS, DOCTOR);
        sUriMatcher.addURI(doctorContract.CONTENT_AUTHORITY, doctorContract.PATH_DOCTORS + "/*/*", DOCTOR_ID);
        sUriMatcher.addURI(doctorContract.CONTENT_AUTHORITY, doctorContract.PATH_DOCTORS + "/*", SPECIFIC_DOCTOR);
    }

    @Override
    public boolean onCreate() {
        mPatientHelper = new patientDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mPatientHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor = null;

        switch (match){
            case PATIENTS:
                cursor = db.query(patientEntry.tableName(doctorPreference.getUsernameFromSP(MyApplication.getAppContext())), projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PATIENT_ID:
                List list4 = uri.getPathSegments();
                selection = patientEntry.COLUMN_PUSH_ID + "=?";
                selectionArgs = new String[]{String.valueOf(list4.get(2))};
                cursor = db.query(patientEntry.tableName(doctorPreference.getUsernameFromSP(MyApplication.getAppContext())), projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PATIENT_NAME:
                List list2 = uri.getPathSegments();
                String name = (String) list2.get(1);

                selection = patientEntry.COLUMN_NAME + " LIKE ?";
                selectionArgs = new String[]{"%" + name + "%"};

                cursor = db.query(patientEntry.tableName(doctorPreference.getUsernameFromSP(MyApplication.getAppContext())), projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case DOCTOR_ID:
                List list = uri.getPathSegments();
                String username = (String) list.get(1);
                String password = (String) list.get(2);

                selection = doctorEntry.COLUMN_EMAIL + "=?";
                selectionArgs = new String[]{username};

                Cursor cursor1 = db.query(doctorEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                if(cursor1.getCount() == 0){
                    return cursor1;
                    //Do Nothing
                }else {
                    selection = doctorEntry.COLUMN_PASSWORD + "=?";
                    selectionArgs = new String[]{password};

                    cursor1.moveToFirst();

                    Cursor cursor2 = db.query(doctorEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                    if(cursor2.getCount() == 0){
                        return cursor2;
                    }else {
                        cursor2.moveToFirst();
                        return cursor1;
                    }
                }
            case SPECIFIC_DOCTOR:
                selection = doctorEntry.COLUMN_EMAIL + "=?";

                List list1 = uri.getPathSegments();
                String username1 = (String) list1.get(1);

                selection = doctorEntry.COLUMN_EMAIL + "=?";
                selectionArgs = new String[]{username1};

                cursor = db.query(doctorEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException();
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        Uri rUri = null;

        switch (match){
            case PATIENTS:
                rUri = insertPatient(uri, values);
                break;
            case DOCTOR:
                rUri = insertDoctor(uri, values);
                break;
            default:
                new IllegalArgumentException();
        }
        return rUri;
    }

    public Uri insertPatient(Uri uri, ContentValues values){
        SQLiteDatabase db = mPatientHelper.getWritableDatabase();

        Long returnedId = db.insert(patientEntry.tableName(doctorPreference.getUsernameFromSP(MyApplication.getAppContext())), null, values);
        if(returnedId == -1){
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, returnedId);
    }

    public Uri insertDoctor(Uri uri, ContentValues values){
        SQLiteDatabase db = mPatientHelper.getWritableDatabase();

        Long returnedId = db.insert(doctorEntry.TABLE_NAME, null, values);
        if(returnedId == -1){
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, returnedId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mPatientHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PATIENTS:
                // Delete all rows that match the selection and selection args
                getContext().getContentResolver().notifyChange(uri, null);
                return database.delete(patientEntry.tableName(doctorPreference.getUsernameFromSP(MyApplication.getAppContext())), selection, selectionArgs);
            case PATIENT_ID:
                // Delete a single row given by the ID in the URI
                selection = patientEntry.COLUMN_PUSH_ID + "=?";
                selectionArgs = new String[] { uri.getPathSegments().get(2) };
                getContext().getContentResolver().notifyChange(uri, null);
                return database.delete(patientEntry.tableName(doctorPreference.getUsernameFromSP(MyApplication.getAppContext())), selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PATIENT_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = patientEntry.COLUMN_PUSH_ID + "=?";
                selectionArgs = new String[] { uri.getPathSegments().get(2) };
                SQLiteDatabase database = mPatientHelper.getWritableDatabase();

                getContext().getContentResolver().notifyChange(uri, null);

                // Returns the number of database rows affected by the update statement
                return database.update(patientEntry.tableName(doctorPreference.getUsernameFromSP(MyApplication.getAppContext())), values, selection, selectionArgs);

            case SPECIFIC_DOCTOR:
                List list1 = uri.getPathSegments();
                String username1 = (String) list1.get(1);

                selection = doctorEntry.COLUMN_EMAIL + "=?";
                selectionArgs = new String[]{username1};
                SQLiteDatabase database1 = mPatientHelper.getWritableDatabase();

                getContext().getContentResolver().notifyChange(uri, null);

                return database1.update(doctorEntry.TABLE_NAME, values, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }


    public static void patientInitialize(){
        patientProvider.sUriMatcher.addURI(doctorContract.CONTENT_AUTHORITY, doctorContract.patientEntry.tableName(doctorPreference.getUsernameFromSP(MyApplication.getAppContext())), PATIENTS);
        patientProvider.sUriMatcher.addURI(doctorContract.CONTENT_AUTHORITY, doctorContract.patientEntry.tableName(doctorPreference.getUsernameFromSP(MyApplication.getAppContext())) + "/#/*", PATIENT_ID);
        patientProvider.sUriMatcher.addURI(doctorContract.CONTENT_AUTHORITY, doctorContract.patientEntry.tableName(doctorPreference.getUsernameFromSP(MyApplication.getAppContext())) + "/*", PATIENT_NAME);
    }
}
