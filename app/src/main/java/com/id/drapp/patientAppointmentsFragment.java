package com.id.drapp;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.id.drapp.models.patientsAppointmentModel;

import java.util.ArrayList;
import java.util.List;

public class patientAppointmentsFragment extends Fragment {

    private ListView appointmentsList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth firebaseAuth;

    private patientAppointmentsAdapter adapter;

    private FirebaseAuth.AuthStateListener mAuthStateChangeListener;
    private ChildEventListener mChildEventListener;
    private AlertDialog.Builder builder;

    private ConnectivityManager connectivityManager;

    private ImageView noInternet;
    private TextView noInternetTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = getLayoutInflater().inflate(R.layout.activity_patient_appointments, container,
                false);

        appointmentsList = rootView.findViewById(R.id.appointmentsList);

        noInternet = rootView.findViewById(R.id.noInternet);
        noInternetTextView = rootView.findViewById(R.id.noInternetTextView);

        connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null){
            noInternetTextView.setVisibility(View.VISIBLE);
            appointmentsList.setVisibility(View.GONE);
            noInternet.setVisibility(View.VISIBLE);
        }else {
            noInternetTextView.setVisibility(View.GONE);
            appointmentsList.setVisibility(View.VISIBLE);
            noInternet.setVisibility(View.GONE);
        }


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        mAuthStateChangeListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //user signed in
                    onSignedInInitialize(user.getDisplayName());
                }else {
                    onSignedOutCleanUp();
                }
            }
        };

        List<patientsAppointmentModel> list = new ArrayList<>();
        adapter = new patientAppointmentsAdapter(getActivity(), R.layout.appointment_list_item, list);
        appointmentsList.setAdapter(adapter);

        connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        appointmentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {

                String[] tags = (String[]) view.getTag();
                final String doctorPushId = tags[0];
                final String appointmentPushId = tags[1];

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getActivity());
                }

                builder.setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                                if(networkInfo == null){
                                    Toast.makeText(getActivity(), "No Internet", Toast.LENGTH_SHORT).show();
                                    return;
                                }


                                // continue with delete
                                deleteTheAppointment(doctorPushId, appointmentPushId, parent, view);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            }
        });

        return rootView;
    }

    private void deleteTheAppointment(String doctorPushId, final String appointmentPushId, final AdapterView<?> parent, final View view) {
        Query hekkQuery = mDatabaseReference.child(doctorPushId);

        hekkQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    snapshot.child("appointments").child(appointmentPushId).getRef().removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mDatabaseReference = mFirebaseDatabase.getReference();
                                    mDatabaseReference.child(doctorPreference.getPhoneNumberFromSP(getActivity()))
                                            .child("appointments").child(appointmentPushId).getRef().removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getActivity(), "Successfully deleted", Toast.LENGTH_LONG).show();
                                                    parent.removeViewInLayout(view);
                                                    appointmentsList.setAdapter(adapter);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void onSignedInInitialize(String username){
        attachDatabaseReadListener();
    }

    public void onSignedOutCleanUp(){
        detachDatabaseReadListener();
    }


    public void attachDatabaseReadListener(){
        if(mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if(dataSnapshot.getKey().equals(doctorPreference.getPhoneNumberFromSP(getActivity()))){
                        for (DataSnapshot snapshot: dataSnapshot.child("appointments").getChildren()){
                            adapter.add(snapshot.getValue(patientsAppointmentModel.class));
                        }
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            mDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    public void detachDatabaseReadListener(){
        if(mChildEventListener != null){
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAuthStateChangeListener != null){
            firebaseAuth.removeAuthStateListener(mAuthStateChangeListener);
        }
        detachDatabaseReadListener();
        adapter.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(mAuthStateChangeListener);
    }
}
