package com.id.drapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.id.drapp.doctorContract.patientEntry;

public class patientAdapter extends CursorAdapter{
    Context context;

    public patientAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from((context)).inflate(R.layout.patients_list_item, parent, false);

    }

    @SuppressLint("NewApi")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView patientAvatar = view.findViewById(R.id.patientAvatar);
        TextView patientName = view.findViewById(R.id.patientName);
        TextView patientAddress = view.findViewById(R.id.patientAddress);
        TextView patientAge = view.findViewById(R.id.patientAge);

        String name = cursor.getString(cursor.getColumnIndex(patientEntry.COLUMN_NAME));
        String address = cursor.getString(cursor.getColumnIndex(patientEntry.COLUMN_ADDRESS));
        String phone = cursor.getString(cursor.getColumnIndex(patientEntry.COLUMN_PHONE_NUMBER));
        byte[] bmpByte = null;
        bmpByte = cursor.getBlob(cursor.getColumnIndex(patientEntry.COLUMN_IMAGE));
        String pushId = cursor.getString(cursor.getColumnIndex(patientEntry.COLUMN_PUSH_ID));

        view.setTag(pushId);

        if(bmpByte != null){
            Bitmap bmp  = DbBitmapUtility.getImage(bmpByte);
            Bitmap bmp1 = Bitmap.createScaledBitmap(bmp, 500 ,500 ,true);
            patientAvatar.setImageBitmap(bmp1);
        }else {
            patientAvatar.setBackground(context.getDrawable(R.drawable.userplaceholder));
        }

        patientName.setText(name);
        patientAddress.setText(address);
        patientAge.setText(phone);


    }
}
