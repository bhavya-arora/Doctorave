package com.id.drapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.id.drapp.models.patientsAppointmentModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class patientAppointmentsAdapter extends ArrayAdapter<patientsAppointmentModel>{

    public patientAppointmentsAdapter(@NonNull Context context, int resource, @NonNull List<patientsAppointmentModel> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.appointment_list_item, parent, false);
        }

        /*// New date object from millis
        Date date = new Date(System.currentTimeMillis());
// formattter*/
        SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        Calendar calendar = Calendar.getInstance();

        TextView fullName = convertView.findViewById(R.id.fullName);
        TextView appointmentTime  = convertView.findViewById(R.id.appointmentTime);
        TextView hospitalName = convertView.findViewById(R.id.hospitalName);
        TextView Problem = convertView.findViewById(R.id.problem);

        patientsAppointmentModel model = getItem(position);

        String fullname = model.getDoctorname();
        long appointmenttime = model.getTime();
        String hospitalname = model.getHospitalName();
        String problem = model.getProblem();
        String doctorPushId = model.getDoctorPushId();
        String appointmentPushId = model.getAppointmentPushId();

        //////////
        calendar.setTimeInMillis(appointmenttime);

        fullName.setText(fullname);
        appointmentTime.setText(formatter.format(calendar.getTime()));
        hospitalName.setText(hospitalname);
        Problem.setText("Reason: ".concat(problem));

        String[] appointmentTag = new String[2];
        appointmentTag[0] = doctorPushId;
        appointmentTag[1] = appointmentPushId;

        convertView.setTag(appointmentTag);

        return convertView;
    }
}
