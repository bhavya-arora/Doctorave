package com.id.drapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.id.drapp.doctorContract.patientEntry;
import com.id.drapp.doctorContract.doctorEntry;

public class patientDbHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "patient.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_DOCTOR_TABLE = "CREATE TABLE " + doctorEntry.TABLE_NAME + " ("
            + doctorEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + doctorEntry.COLUMN_PUSHID + " TEXT NOT NULL, "
            + doctorEntry.COLUMN_NAME + " TEXT NOT NULL, "
            + doctorEntry.COLUMN_PHONE_NUMBER + " TEXT NOT NULL, "
            + doctorEntry.COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, "
            + doctorEntry.COLUMN_PASSWORD + " TEXT NOT NULL, "
            + doctorEntry.COLUMN_TITLE + " TEXT, "
            + doctorEntry.COLUMN_INSTITUTE + " TEXT, "
            + doctorEntry.COLUMN_IMAGE + " BLOB, "
            + doctorEntry.COLUMN_INSTITUTE_ADDRESS + " TEXT);";

    public patientDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DOCTOR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void createPatientDb(Context context, String username){
        String CREATE_PATIENT_TABLE = "CREATE TABLE " + (patientEntry.tableName(username)) +" ("
                + patientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + patientEntry.COLUMN_PUSH_ID + " TEXT NOT NULL, "
                + patientEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + patientEntry.COLUMN_PHONE_NUMBER + " TEXT NOT NULL, "
                + patientEntry.COLUMN_EMAIL + " TEXT, "
                + patientEntry.COLUMN_ADDRESS + " TEXT NOT NULL, "
                + patientEntry.COLUMN_GENDER + " INTEGER NOT NULL DEFAULT 0, "
                + patientEntry.COLUMN_IMAGE + " BLOB, "
                + patientEntry.COLUMN_DOB + " TEXT NOT NULL);";

        patientDbHelper helper = new patientDbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(CREATE_PATIENT_TABLE);
    }

}
