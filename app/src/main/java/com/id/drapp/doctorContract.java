package com.id.drapp;

import android.net.Uri;
import android.provider.BaseColumns;

public final class doctorContract {

    public static final String CONTENT_AUTHORITY = "com.id.drapp";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static String PATH_DOCTORS = "doctors";

    public static abstract class patientEntry implements BaseColumns{

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "patient_name";
        public static final String COLUMN_PHONE_NUMBER = "patient_ph_no";
        public static final String COLUMN_EMAIL = "email_id";
        public static final String COLUMN_DOB = "patient_dob";
        public static final String COLUMN_ADDRESS = "patient_address";
        public static final String COLUMN_GENDER = "patient_gender";
        public static final String COLUMN_IMAGE = "patient_image";
        public static final String COLUMN_PUSH_ID = "push_id";

        public static Uri contentUri(String username){
            String pathPatient = "[" + "patients" + username + "]";
            Uri uri = Uri.withAppendedPath(BASE_CONTENT_URI, pathPatient);
            return uri;
        }

        public static String tableName(String username){
            String tablename = "[" + "patients" + username + "]";
            return tablename;
        }


    }

    public static abstract class doctorEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DOCTORS);
        public static final String TABLE_NAME = "doctors";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PUSHID = "doctor_push_id";
        public static final String COLUMN_NAME = "doctor_name";
        public static final String COLUMN_PHONE_NUMBER = "doctor_ph_no";
        public static final String COLUMN_EMAIL = "email_id";
        public static final String COLUMN_PASSWORD = "doctor_password";
        public static final String COLUMN_TITLE = "doctor_title";
        public static final String COLUMN_INSTITUTE = "doctor_institute";
        public static final String COLUMN_IMAGE = "doctor_image";
        public static final String COLUMN_INSTITUTE_ADDRESS = "doctor_institute_address";
    }

    public static String checkGender(int gender){
        if(gender == 0){
            return "Male";
        }else {
            return "Female";
        }
    }
}
