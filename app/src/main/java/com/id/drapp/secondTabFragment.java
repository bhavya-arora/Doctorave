package com.id.drapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.id.drapp.models.doctorAppointmentsModel;

import java.util.ArrayList;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;

public class secondTabFragment extends Fragment {

    private ListView appointmentsList;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth firebaseAuth;

    private doctorAppointmentAdapter adapter;

    private FirebaseAuth.AuthStateListener mAuthStateChangeListener;
    private ChildEventListener mChildEventListener;
    private AlertDialog.Builder builder;

    private ConnectivityManager connectivityManager;
    private ImageView noInternet;
    private TextView noInternetTextView;

    private MaterialDialog materialDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = getLayoutInflater().inflate(R.layout.activity_second_tab, container,
                false);

        appointmentsList = rootView.findViewById(R.id.doctorAppointmentList);
        noInternet = rootView.findViewById(R.id.noInternet);
        noInternetTextView = rootView.findViewById(R.id.noInternetTextView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        firebaseAuth = FirebaseAuth.getInstance();

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

        List<doctorAppointmentsModel> list = new ArrayList<>();
        adapter = new doctorAppointmentAdapter(getActivity(), R.layout.appointment_list_item, list);
        appointmentsList.setAdapter(adapter);

        appointmentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                final String[] tags = (String[]) view.getTag();


                materialDialog = new MaterialDialog(getActivity())
                        .setTitle("Add Patient Info")
                        .setMessage("Do you want to Add Patient in your Database?")
                        .setPositiveButton("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String fullname = tags[0];
                                String phoneno = tags[1];
                                String email = tags[2];
                                String dob = tags[3];
                                String address = tags[4];

                                Intent intent = new Intent(getActivity(), addPatientActivity.class);
                                intent.putExtra("fullname", fullname);
                                intent.putExtra("phoneno", phoneno);
                                intent.putExtra("email", email);
                                intent.putExtra("dob", dob);
                                intent.putExtra("address", address);
                                startActivity(intent);
                                materialDialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                materialDialog.dismiss();
                            }
                        });
                materialDialog.show();
            }
        });

        return rootView;
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
                    for(DataSnapshot snapshot: dataSnapshot
                            .child(charUtility.filterString(doctorPreference.getUsernameFromSP(getActivity())))
                            .child("appointments").getChildren()){
                        if(dataSnapshot.hasChild(charUtility.filterString(doctorPreference.getUsernameFromSP(getActivity())))){
                            adapter.add(snapshot.getValue(doctorAppointmentsModel.class));
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
