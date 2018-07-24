package com.id.drapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.id.drapp.models.doctorInfo;

import java.util.ArrayList;
import java.util.List;

public class hospitalListFragment extends Fragment{

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private ChildEventListener mChildEventListener;

    public FirebaseAuth.AuthStateListener mAuthStateChangeListener;

    private ListView hospitalList;
    private ImageView noInternet;
    private TextView noInternetTextView;
    public hospitalListAdapter hospitalListAdapter;

    private ConnectivityManager connectivityManager;
    private Boolean isVerified;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = getLayoutInflater().inflate(R.layout.activity_hospital_list, container,
                false);

        hospitalList = rootView.findViewById(R.id.hospitalList);
        noInternet = rootView.findViewById(R.id.noInternet);
        noInternetTextView = rootView.findViewById(R.id.noInternetTextView);

        progressDialog=new ProgressDialog(getActivity(),R.style.AppTheme_Dark_Dialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);

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

        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null){
            noInternetTextView.setVisibility(View.VISIBLE);
            hospitalList.setVisibility(View.GONE);
            noInternet.setVisibility(View.VISIBLE);
        }else {
            noInternetTextView.setVisibility(View.GONE);
            hospitalList.setVisibility(View.VISIBLE);
            noInternet.setVisibility(View.GONE);
        }

        List<doctorInfo> doctorInfos = new ArrayList<>();
        hospitalListAdapter = new hospitalListAdapter(getActivity(), R.layout.patients_list_item, doctorInfos);
        hospitalList.setAdapter(hospitalListAdapter);
        hospitalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] tags = (String[]) view.getTag();
                Intent intent = new Intent(getActivity(), hospitalDetailActivity.class);
                intent.putExtra("tag", tags);
                startActivity(intent);
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
        progressDialog.show();
        if(mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    if(dataSnapshot.hasChild("appointments")){
                        progressDialog.dismiss();
                    }else {
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            if((Boolean) snapshot.child("doctorInfo").child("isVerified").getValue()){
                                doctorInfo info = snapshot.child("doctorInfo").getValue(doctorInfo.class);
                                hospitalListAdapter.add(info);
                            }else {

                            }
                        }
                        progressDialog.dismiss();
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
        hospitalListAdapter.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(mAuthStateChangeListener);
    }
}
