package com.id.drapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.id.drapp.models.doctorAppointmentsModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class doctorAppointmentAdapter extends ArrayAdapter<doctorAppointmentsModel>{

    public doctorAppointmentAdapter(@NonNull Context context, int resource, @NonNull List<doctorAppointmentsModel> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.appointment_list_item, parent, false);
        }

        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        Calendar calendar = Calendar.getInstance();

        TextView fullName = convertView.findViewById(R.id.fullName);
        TextView appointmentTime  = convertView.findViewById(R.id.appointmentTime);
        TextView phoneNo = convertView.findViewById(R.id.hospitalName);
        TextView Problem = convertView.findViewById(R.id.problem);

        doctorAppointmentsModel model = getItem(position);

        String fullname = model.getFullName();
        String phoneno = model.getPhoneno();
        String email = model.getEmail();
        String dob = model.getDob();
        String address = model.getAddress();
        String problem = model.getProblem();
        long appointmenttime = model.getAppointmentTime();

        calendar.setTimeInMillis(appointmenttime);

        fullName.setText(fullname);
        phoneNo.setText(phoneno);
        Problem.setText("Reason: ".concat(problem));
        appointmentTime.setText(formatter.format(calendar.getTime()));

        String[] tags = new String[5];
        tags[0] = fullname;
        tags[1] = phoneno;
        tags[2] = email;
        tags[3] = dob;
        tags[4] = address;

        convertView.setTag(tags);

        return convertView;


    }
}
