package com.id.drapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.id.drapp.models.doctorInfo;

import java.util.List;

public class hospitalListAdapter extends ArrayAdapter<doctorInfo>{

    public hospitalListAdapter(@NonNull Context context, int resource, @NonNull List<doctorInfo> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.patients_list_item, parent, false);
        }

        TextView doctorNameView = convertView.findViewById(R.id.patientName);
        TextView hospitalNameView = convertView.findViewById(R.id.patientAddress);
        TextView doctorTitleView = convertView.findViewById(R.id.patientAge);
        ImageView doctorAvatarView = convertView.findViewById(R.id.patientAvatar);

        doctorInfo info = getItem(position);

        String doctorFirstName = info.getFirstName();
        String doctorLastName = info.getLastName();
        String hospitalName = info.getInstituteName();
        String doctorTitle = info.getTitle();
        String doctorPushId = info.getPushId();
        String doctorUsername = info.getEmail();

        hospitalName = hospitalName.substring(0,1).toUpperCase() + hospitalName.substring(1);

        if(info.getImageLink() == null){
            doctorNameView.setText(doctorFirstName.concat(" ").concat(doctorLastName));
            hospitalNameView.setText(hospitalName);
            doctorTitleView.setText(doctorTitle);
        }else {
            doctorNameView.setText(doctorFirstName.concat(" ").concat(doctorLastName));
            hospitalNameView.setText(hospitalName);
            doctorTitleView.setText(doctorTitle);
            Glide.with(doctorAvatarView.getContext())
                    .load(info.getImageLink())
                    .into(doctorAvatarView);
        }
        String[] tags = new String[2];
        tags[0] = doctorPushId;
        tags[1] = doctorUsername;
        convertView.setTag(tags);
        return convertView;

    }
}
