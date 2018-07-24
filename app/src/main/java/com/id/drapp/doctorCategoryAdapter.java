package com.id.drapp;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Bhavya Arora on 6/25/2018.
 */

public class doctorCategoryAdapter extends FragmentPagerAdapter {
    private Context mContext;

    public doctorCategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new hospitalListFragment();
        } else {
            return new patientAppointmentsFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Hospitals";
        } else {
           return "Appointments";
        }
    }
}

