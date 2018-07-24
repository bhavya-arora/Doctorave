package com.id.drapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.id.drapp.doctorContract.patientEntry;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class patientsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    ListView patientsRecycler;
    public static int PATIENT_ADAPTER = 0;
    private patientAdapter adapter;
    private static String user;
    private static String pushId;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth firebaseAuth;

    private static AlertDialog.Builder builder1;

    private Uri uri;

    private static ConnectivityManager connectivityManager;

    private AlertDialog.Builder builder;

    private FirebaseAuth.AuthStateListener mAuthStateChangeListener;

    private ProgressDialog progressDialog;

    private static FloatingActionButton fab;

    private static Activity activity;

    public patientsFragment(){
        //empty COnstructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = getLayoutInflater().inflate(R.layout.activity_patients, container,
                false);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), addPatientActivity.class);
                startActivity(intent);
            }
        });

        activity = getActivity();

        pushId = doctorPreference.getUserPushId(getActivity());

        adapter = new patientAdapter(getActivity(), null);
        patientsRecycler = rootView.findViewById(R.id.patientsRecycler);
        patientsRecycler.setAdapter(adapter);

        mFirebaseDatabase = FirebaseDatabase.getInstance();


        progressDialog=new ProgressDialog(getActivity(),R.style.AppTheme_Dark_Dialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);


        connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        user = doctorPreference.getUsernameFromSP(getActivity());
        mDatabaseReference = mFirebaseDatabase.getReference().child(pushId).child(charUtility.filterString(user)).child("patientData");

        patientsRecycler.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String pushId = (String) view.getTag();
                Intent intent = new Intent(getActivity(), detailActivity.class);
                intent.putExtra("detailUri", patientEntry.contentUri(doctorPreference.getUsernameFromSP(getActivity())) + "/" + 1 + "/" + pushId);
                startActivity(intent);
            }
        });

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

        getLoaderManager().initLoader(PATIENT_ADAPTER, null, this);
        return rootView;
    }

    private void onSignedOutCleanUp() {
    }

    private void onSignedInInitialize(String displayName) {
        if(!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
            Toast.makeText(getActivity(), "You are not verified", Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(mAuthStateChangeListener);
    }


    @Override
    public void onPause() {
        super.onPause();
        if(mAuthStateChangeListener != null){
            firebaseAuth.removeAuthStateListener(mAuthStateChangeListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FirebaseMessaging.getInstance().unsubscribeFromTopic("pushNotifications");
    }


    @SuppressLint("NewApi")
    public static void showTapTarget(final Context context, Activity activity) {

        new MaterialTapTargetPrompt.Builder(activity)
                .setTarget(fab)
                .setBackgroundColour(context.getColor(R.color.actionBar))
                .setPrimaryText("Add Patients")
                .setSecondaryText("Start Adding Patients to Offline and it will automatically Sync to cloud.")
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener()
                {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget)
                    {
                        //TODO: Store in SharedPrefs so you don't show this prompt again.
                        doctorPreference.saveIsTapTargetShown(context, true);
                    }

                    @Override
                    public void onHidePromptComplete()
                    {
                    }
                })
                .show();

    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{patientEntry._ID,
                patientEntry.COLUMN_NAME,
                patientEntry.COLUMN_ADDRESS,
                patientEntry.COLUMN_PUSH_ID,
                patientEntry.COLUMN_PHONE_NUMBER,
                patientEntry.COLUMN_IMAGE,
                patientEntry.COLUMN_DOB};
        return new CursorLoader(getActivity(), patientEntry.contentUri(doctorPreference.getUsernameFromSP(getActivity())), projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        adapter.swapCursor(data);

        if(data.getCount() == 0){
            Query hekkQuery = mDatabaseReference;

            hekkQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(doctorPreference.getWantToRestoreData(getActivity())){
                        if(dataSnapshot.getChildrenCount() == 0){

                        }else {
                            showAlertToRestoreData(dataSnapshot.getChildrenCount());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public static void restartAppDialog(final Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder1 = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
            builder1.setCancelable(false);
        } else {
            builder1 = new AlertDialog.Builder(activity);
            builder1.setCancelable(false);
        }

        builder1.setTitle("Warning..")
                .setMessage("Please Restart Whole App or Weird things happen with your data")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    private void showAlertToRestoreData(long enteries) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getActivity());
        }

        builder.setTitle("Restore Data")
                .setMessage("You Have " + enteries + " Enteries in Backup. Do you want to Restore?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                        if(networkInfo == null){
                            Toast.makeText(getActivity(), "No Internet", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        initialize(getActivity());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        doctorPreference.saveWantToRestoreData(getActivity(), false);
                    }
                })
                .setIcon(android.R.drawable.sym_def_app_icon).show();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public static void startImmediateSync(@NonNull final Context context) {
        Intent intentToSyncImmediately = new Intent(context, patientIntentService.class);
        context.startService(intentToSyncImmediately);
    }

    synchronized public static void initialize(@NonNull final Context context) {
        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo == null){
                    Toast.makeText(context, "No Internet", Toast.LENGTH_LONG).show();
                    return;
                }

                startImmediateSync(context);
            }
        });
        checkForEmpty.start();
    }

}
