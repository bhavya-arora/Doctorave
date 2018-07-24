package com.id.drapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class patientsListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Toolbar toolbar;
    private com.mancj.materialsearchbar.MaterialSearchBar searchBar;

    private List<String> suggestList;
    private ViewPager pager;
    private TabLayout tabsLayout;
    private ListView patientSearchList;

     private patientAdapter adapter1;
     private FirebaseAuth firebaseAuth;

     private MaterialDialog mMaterialDialog;



    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_patient);

        patientProvider.patientInitialize();

        toolbar = findViewById(R.id.toolbar);
        searchBar = findViewById(R.id.searchBar);
        patientSearchList = findViewById(R.id.patientSearchList);

        firebaseAuth = FirebaseAuth.getInstance();

        showAboutUsDialog();

        adapter1 = new patientAdapter(this, null);
        patientSearchList.setAdapter(adapter1);
        patientSearchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String pushId = (String) view.getTag();
                Intent intent = new Intent(patientsListActivity.this, detailActivity.class);
                intent.putExtra("detailUri", doctorContract.patientEntry.contentUri(doctorPreference.getUsernameFromSP(patientsListActivity.this)) + "/" + 1 + "/" + pushId);
                startActivity(intent);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(0);
        }
        setSupportActionBar(toolbar);

        tabsLayout=(TabLayout) findViewById(R.id.tabsLayout);
        ///////ViewPager comes handy when we swipe to left and right to fragments///////
        pager=(ViewPager) findViewById(R.id.pager);
        //////category Adapter for setting data for ViewPager///////////
        final CategoryAdapter adapter=new CategoryAdapter(this,getSupportFragmentManager());
        //////Pager will retrieve data from CategoryAdapter///////////
        pager.setAdapter(adapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabsLayout.setBackgroundColor(getColor(R.color.actionBar));
        }
        tabsLayout.setupWithViewPager(pager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //<This code is for Navigation--------------------------------------------------
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        TextView doctorname = headerView.findViewById(R.id.doctorname);
        ImageView imageView = headerView.findViewById(R.id.imageView);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        Cursor cursor = getContentResolver().query(Uri.parse(doctorContract.doctorEntry.CONTENT_URI + "/" +  doctorPreference.getUsernameFromSP(this)), null, null, null, null);
        cursor.moveToFirst();

        String name = cursor.getString(cursor.getColumnIndex(doctorContract.doctorEntry.COLUMN_EMAIL));
        byte[] byte1 = cursor.getBlob(cursor.getColumnIndex(doctorContract.doctorEntry.COLUMN_IMAGE));
        if(byte1 != null){
            Bitmap bmp  = DbBitmapUtility.getImage(byte1);
            Bitmap bmp1 = Bitmap.createScaledBitmap(bmp, 200 ,200 ,true);

            imageView.setImageBitmap(bmp1);
        }else {
            imageView.setBackground(getResources().getDrawable(R.drawable.userplaceholder));
        }

        doctorname.setText(name);
        //-------------------------------------------------------------------------------->

        searchBar.setHint("Search Patient Name");
        searchBar.setElevation(10);
        loadSuggestList();
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                List<String> suggest = new ArrayList<>();
                for(String search: suggestList){
                    if(search.toLowerCase().contains(searchBar.getText().toLowerCase())){
                        suggest.add(search);
                    }
                }
                searchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if(!enabled){
                    toolbar.setVisibility(View.VISIBLE);
                    pager.setVisibility(View.VISIBLE);
                    tabsLayout.setVisibility(View.VISIBLE);
                    searchBar.setVisibility(View.GONE);
                    patientSearchList.setVisibility(View.GONE);
                    adapter1.swapCursor(null);

                }else {
                    pager.setVisibility(View.GONE);
                    tabsLayout.setVisibility(View.GONE);
                    patientSearchList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearching(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            finishTheActivity();
        }else if(id == R.id.nav_sync){
            patientsFragment.initialize(this);
        }else if (id == R.id.settings){
            Intent intent = new Intent(this, settingsActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_about){
            mMaterialDialog.show();
        }
        return true;
    }

    private void showAboutUsDialog() {
        TextView textView = new TextView(this);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "Doctorave is a Complete app for Doctors and Patients. Developed by Bhavya Arora. More Features Coming soon... <br><br>&emsp;<a href='https://github.com/bhavya-arora'> Github </a> &emsp;" +
                "  <a href='https://in.linkedin.com/in/bhavya-arora-716b37145'> Linkedin </a> &emsp;  <a href='http://bhavya-arora.me/'> Website </a>" +
                "<br><br><strong>Privacy and Policy / Open Source Licenses:</strong> <a href='http://bhavya-arora.me/doctorave-privacy-policy'> Check Here. </a><br><br><Strong>Fork us on Github: </Strong><a href='https://github.com/bhavya-arora/Doctorave'>Here. </a><br><br> If any Issue/Bug contact us here: <a href=\"mailto:gobhavyaarora15@gmail.com?Subject=Hello%20again\" target=\"_top\">here</a>";
        textView.setText(Html.fromHtml(text));


        mMaterialDialog = new MaterialDialog(this)
                .setTitle("Doctorave")
                .setMessage("Doctorave is a Complete App for Doctors and Patient. Developed by Bhavya Arora.")
                .setContentView(R.layout.aboutusdialog)
                .setContentView(textView)
                .setPositiveButton("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mMaterialDialog.dismiss();
                    }
                });
    }

    public void finishTheActivity(){
        doctorPreference.saveBooleanInSP(this, false);
        doctorPreference.saveUsernameInSP(this, null);
        doctorPreference.saveUserPushId(this, null);
        firebaseAuth.signOut();
        finish();
    }

    public void startSearching(String text){
        Cursor cursor = getContentResolver().query(Uri.parse(doctorContract.patientEntry.contentUri(doctorPreference.getUsernameFromSP(this)) + "/" + text), null, null,null, null);
        if(cursor != null){
            adapter1.swapCursor(cursor);
        }else {
            Toast.makeText(this, "Cannot Found " + text, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSuggestList() {

        suggestList = getNames();
        searchBar.setLastSuggestions(suggestList);
        searchBar.hideSuggestionsList();

    }

    public List<String> getNames(){
        String[] projection = new String[]{
                doctorContract.patientEntry.COLUMN_NAME};
        Cursor cursor = getContentResolver().query(doctorContract.patientEntry.contentUri(doctorPreference.getUsernameFromSP(this)), projection, null, null, null);

        List<String> list = new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                list.add(cursor.getString(cursor.getColumnIndex(doctorContract.patientEntry.COLUMN_NAME)));
            }while (cursor.moveToNext());
        }
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu, menu);

        if(doctorPreference.getIsTapTargetShown(this)){

        }else {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    final View view = findViewById(R.id.logout);

                    new MaterialTapTargetPrompt.Builder(patientsListActivity.this)
                            .setTarget(view)
                            .setBackgroundColour(getResources().getColor(R.color.actionBar))
                            .setPrimaryText("Search Patients")
                            .setSecondaryText("You can Search Patients in your Offline Database.")
                            .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener()
                            {
                                @Override
                                public void onHidePrompt(MotionEvent event, boolean tappedTarget)
                                {
                                    //TODO: Store in SharedPrefs so you don't show this prompt again.
                                    Activity patientsListActivity = patientsListActivity.this;
                                    patientsFragment.showTapTarget(patientsListActivity.this, patientsListActivity);
                                }

                                @Override
                                public void onHidePromptComplete()
                                {
                                }
                            })
                            .show();

                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.logout:
                toolbar.setVisibility(View.GONE);
                searchBar.setVisibility(View.VISIBLE);
                searchBar.enableSearch();
                searchBar.hideSuggestionsList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void logout(){
        doctorPreference.saveBooleanInSP(this, false);
        doctorPreference.saveUsernameInSP(this, null);
        doctorPreference.saveIsTapTargetShown(this, false);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
